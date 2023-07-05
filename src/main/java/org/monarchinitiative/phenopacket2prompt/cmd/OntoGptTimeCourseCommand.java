package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptFilterer;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptImporter;
import org.monarchinitiative.phenopacket2prompt.phenopacket.TimeBasedFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    Logger LOGGER = LoggerFactory.getLogger(OntoGptCsvCommand.class);
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

    @CommandLine.Option(names = {"--template"},
            required = true,
            description = "time periods template file")
    private String templateFile = null;


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
        Map<String, List<String>> id2lines = new HashMap<>();
        Set<String> gptFiles = listGptFiles(gptDirectoryPath);
        for (String fname : gptFiles) {
            File fpath = new File(gptDirectoryPath + File.separator + fname);
            ChatGptImporter importer = new ChatGptImporter(fpath);
            List<String> lines = importer.getLines();
            String caseName = getCaseName(fname);
            if (INVALID_CASE_REPORTS.contains(caseName)) {
                System.out.printf("[INFO] Skipping invalid case %s.\n", caseName);
            }
            if (targetCase != null && ! caseName.contains(targetCase)) {
                continue;
            } else {
               // System.out.printf("[INFO] Parsing targetCase %s.\n", targetCase);
            }
            id2lines.put(caseName, lines);
        }
        System.out.printf("[INFO] Parsed %d cases.\n", id2lines.size());
        int validParsedCases = 0;
        Ontology hpo = OntologyLoader.loadOntology(new File(hpoJsonPath));
        Map<String, TimeBasedFactory> id2factory = new HashMap<>();
        final TermMiner miner = TermMiner.defaultNonFuzzyMapper(hpo);

        for (var entry: id2lines.entrySet()) {
            List<String> timePoints;
            System.out.printf("[INFO] Creating prompt for %s.\n",entry.getKey());
            try {
                ChatGptFilterer filterer = new ChatGptFilterer(entry.getKey(), entry.getValue());
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

    private Map<String, List<String>> getCaseIdToTimePhraseMap(String templateFile) {
        Map<String, List<String>> casemap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(templateFile))) {
            String line = br.readLine();
            if (! line.startsWith("case")) {
                throw new PhenolRuntimeException("Malformed header line: " + line);
            }
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                String [] fields = line.split("\t");
                if (fields.length != 2) {
                    throw new PhenolRuntimeException("Malformed line: " + line);
                }
                String caseId = fields[0];
                String times = fields[1];
                if (! caseId.startsWith("PMID:")) {
                    throw new PhenolRuntimeException("Malformed case id in template: " + caseId);
                }
                if (times.equalsIgnoreCase("NA")) {
                    casemap.put(caseId, List.of());
                } else {
                    String [] timefields = times.split(";");
                    List<String> tf = new ArrayList<>();
                    for (var t : timefields) {
                        tf.add(t.strip());
                    }
                    casemap.put(caseId, tf);
                }
            }

        } catch (IOException e) {
            throw new PhenolRuntimeException(e.getMessage());
        }
        return casemap;
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

    public Set<String> listGptFiles(String dir) {
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
