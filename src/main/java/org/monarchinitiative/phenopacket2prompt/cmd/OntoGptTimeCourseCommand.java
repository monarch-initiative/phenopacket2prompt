package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.llm.NejmCaseReportFromPdfFilterer;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptImporter;
import org.monarchinitiative.phenopacket2prompt.phenopacket.TimeBasedFactory;
import picocli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@CommandLine.Command(name = "gpt-time", aliases = {"T"},
        mixinStandardHelpOptions = true,
        description = "Create GPT time-course prompt")
public class OntoGptTimeCourseCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-g", "--gpt"},
            required = true,
            description = "path to directory with data for chatGPT etc")
    private String gptDirectoryPath;

    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";

    @CommandLine.Option(names = {"-o", "--out"},
            description = "path to output dir (created if necessary)")
    private String outDir = "gptOut";

    @CommandLine.Option(names = {"-c", "--case"},
            description = "case ID (just analyze this case)" )
    private String targetCase = null;

    /**
     * The case reports are not valid differential diagnostic exercises
     * 34496178: Discussing HIV prophylaxis
     *  33730458: primarily imaging, not enough text in initial presentation to be a fair comparison
     *  PMID:33913642: scant information in case presented by first discussant. Imaging plays prominent role in case.
     *  PMID:34587390: scant information in case presented by first discussant. Imaging plays prominent role in case.
     *  PMID:34670047: scant information in case presented by first discussant. Imaging plays prominent role in case.
     */
    private final Set<String> INVALID_CASE_REPORTS = Set.of("PMID:34496178", "PMID:33730458", "PMID:33913642",
            "PMID:34587390", "PMID:34670047");


    @Override
    public Integer call() {
        // key: identifier of PMID; value - lines of text
        Map<String, List<String>> id2lines = new HashMap<>();
        // raw text from PDF parsiong of the NEJM cases
        Set<String> nejmCaseReportFiles = listNejmCaseReportFiles(gptDirectoryPath);
        for (String fname : nejmCaseReportFiles) {
            File fpath = new File(gptDirectoryPath + File.separator + fname);
            ChatGptImporter importer = new ChatGptImporter(fpath);
            List<String> lines = importer.getLines();
            String caseName = getCaseName(fname);
            // we skip five of the 80 raw files for reasons listed above
            if (INVALID_CASE_REPORTS.contains(caseName)) {
                continue;
            }
            // If run with the --targetCase argument, just the targetCase is processed.
            // if targetCase == null, that means we are processing all files
            if (targetCase != null) {
                if (!caseName.contains(targetCase)) {
                    continue;
                } else {
                    System.out.printf("[INFO] Parsing targetCase %s.\n", targetCase);
                }
            }
            id2lines.put(caseName, lines);
        }
        System.out.printf("[INFO] Parsed %d cases.\n", id2lines.size());
        int validParsedCases = 0;
        Ontology hpo = OntologyLoader.loadOntology(new File(hpoJsonPath));
        Map<String, TimeBasedFactory> id2factory = new HashMap<>();
        final TermMiner miner = TermMiner.defaultNonFuzzyMapper(hpo);

        for (var entry: id2lines.entrySet()) {
            System.out.printf("[INFO] Creating prompt for %s.\n",entry.getKey());
            try {
                NejmCaseReportFromPdfFilterer filterer = new NejmCaseReportFromPdfFilterer(entry.getKey(), entry.getValue());
                if (!filterer.validParse()) {
                    System.out.printf("ChatGptFilterer -- %s: Not Valid.\n", entry.getKey());
                    continue;
                }
                TimeBasedFactory factory = new TimeBasedFactory(filterer, entry.getKey(), miner, hpo);
                id2factory.put(entry.getKey(), factory);
            } catch (Exception e) {
                System.out.printf("Exception with %s: %s.\n", entry.getKey(), e.getMessage());
                System.exit(1);
            }
            validParsedCases++;
        }
        System.out.printf("[INFO] Factory map has %d cases.\n", id2factory.size());
        System.out.printf("We parsed %d cases, of which %d were valid.\n", id2lines.entrySet().size(), validParsedCases);
        File outdirfile = new File(outDir);
        if (! outdirfile.isDirectory()) {
            boolean dirCreated = outdirfile.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create outdirfile directory");
            }
        }


        File phenopacket_query_dir = new File(outDir + File.separator + "phenopacket_time_based_queries");
        if (! phenopacket_query_dir.isDirectory()) {
            boolean dirCreated = phenopacket_query_dir.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create phenopacket_query_dir directory");
            }
        }




        int n_output = 0;
        for (var entry : id2factory.entrySet()) {
            String pmid = entry.getKey().replace(":", "_");
            TimeBasedFactory factory = entry.getValue();
            // output phenopacket-based text query
            String outpath = phenopacket_query_dir + File.separator + pmid + "-phenopacket-time-based_query.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
                writer.write(factory.getPhenopacketBasedQuery());
                System.out.println(factory.getPhenopacketBasedQuery());
            } catch (IOException e) {
                throw new PhenolRuntimeException(e.getMessage());
            }


            n_output++;
        }
        System.out.printf("We output %d cases from %d valid cases.\n", n_output, validParsedCases);
        return 0;
    }

    private String getCaseName(String filePath) {
        File f = new File(filePath);
        String bname = f.getName();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(filePath);
        if (m.find()) {
            return "PMID:" + m.group();
        }
        return bname;
    }

    /**
     * This function reads the txt files from a directory in which we put
     * texts parsed from the PDF files representing the NEJM case reports.
     * @param dir directory with txt files
     * @return list of file paths
     */
    public Set<String> listNejmCaseReportFiles(String dir) {
        File dirFile = new File(dir);
        if (dirFile.isDirectory()) {
            return Stream.of(dirFile.listFiles())
                    .filter(file -> !file.isDirectory())
                    .filter(file -> file.getAbsolutePath().endsWith(".txt"))
                    .map(File::getName)
                    .collect(Collectors.toSet());
        } else {
            throw new PhenolRuntimeException("input directory did not point to valid directory");
        }
    }

}
