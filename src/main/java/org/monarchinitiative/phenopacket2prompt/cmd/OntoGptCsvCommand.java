package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenopacket2prompt.phenopacket.PhenopacketFactory;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptFilterer;
import org.monarchinitiative.phenopacket2prompt.llm.ChatGptImporter;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(name = "csv-no-time", aliases = {"GPT"},
        mixinStandardHelpOptions = true,
        description = "convert CSV to phenopackets (no time)")
public class OntoGptCsvCommand implements Callable<Integer> {
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
     */
    private final Set<String> INVALID_CASE_REPORTS = Set.of("34496178");


    @Override
    public Integer call() {
        Map<String, List<String>> id2lines = new HashMap<>();
        Set<String> gptFiles = listGptFiles(gptDirectoryPath);
        for (String fname : gptFiles) {
            File fpath = new File(gptDirectoryPath + File.separator + fname);
            ChatGptImporter importer = new ChatGptImporter(fpath);
            List<String> lines = importer.getLines();
            String caseName = getCaseName(fname);
            if (targetCase != null && ! caseName.contains(targetCase)) {
                continue;
            }
            id2lines.put(caseName, lines);
            //System.out.println(fname);
        }
        int validParsedCases = 0;
        Ontology hpo = OntologyLoader.loadOntology(new File(hpoJsonPath));
        Map<String, PhenopacketFactory> id2factory = new HashMap<>();
        final TermMiner miner = TermMiner.defaultNonFuzzyMapper(hpo);
        for (var entry: id2lines.entrySet()) {
            boolean skipEntry = false;
            for (var invalid : INVALID_CASE_REPORTS) {
                if (entry.getKey().contains(invalid)) {
                    skipEntry = true;
                }
            }

            if (skipEntry) continue;
            try {
                ChatGptFilterer filterer = new ChatGptFilterer(entry.getKey(), entry.getValue());
                System.out.println(filterer.getSex());
                System.out.println(filterer.getAge());
                for (var l : filterer.getCaseLines()) {
                    System.out.println(l);
                }

                if (!filterer.validParse()) {
                    System.out.printf("[ERROR(OntoGptCsvCommand)]%s: Valid? %b.\n", entry.getKey(), filterer.validParse());
                    System.exit(0);
                }
                PhenopacketFactory factory = new PhenopacketFactory(filterer, entry.getKey(), miner, hpo);
                id2factory.put(entry.getKey(), factory);
            } catch (Exception e) {
                System.out.printf("Excecption with %s: %s.\n", entry.getKey(), e.getMessage());
                System.exit(1);
            }
            validParsedCases++;
        }
        System.out.printf("We parsed %d cases, of which %d were valid.\n", id2lines.entrySet().size(), validParsedCases);
        File outdirfile = new File(outDir);
        if (! outdirfile.isDirectory()) {
            boolean dirCreated = outdirfile.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create outdirfile directory");
            }
        }

        File phenopacket_dir = new File(outDir + File.separator + "phenopackets");
        if (! phenopacket_dir.isDirectory()) {
            boolean dirCreated = phenopacket_dir.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create phenopackets directory");
            }
        }
        File phenopacket_query_dir = new File(outDir + File.separator + "phenopacket_based_queries");
        if (! phenopacket_query_dir.isDirectory()) {
            boolean dirCreated = phenopacket_query_dir.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create phenopacket_query_dir directory");
            }
        }
        File txt_with_differential = new File(outDir + File.separator + "txt_with_differential");
        if (! txt_with_differential.isDirectory()) {
            boolean dirCreated = txt_with_differential.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create txt_with_differential directory");
            }
        }

        File txt_without_discussion = new File(outDir + File.separator + "txt_without_discussion");
        if (! txt_without_discussion.isDirectory()) {
            boolean dirCreated = txt_without_discussion.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create txt_without_discussion directory");
            }
        }

        int n_output = 0;
        Map<String, String> caseIdToDxMap = new HashMap<>();
        for (var entry : id2factory.entrySet()) {
            String pmid = entry.getKey().replace(":", "_");
            PhenopacketFactory factory = entry.getValue();
            // output phenopacket
            String outpath = phenopacket_dir + File.separator + pmid + "-phenopacket.json";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
                writer.write(factory.getPhenopacketJsonString());
            } catch (IOException e) {
                throw new PhenolRuntimeException(e.getMessage());
            }
            // output phenopacket-based text query
            outpath = phenopacket_query_dir + File.separator + pmid + "-phenopacket-based_query.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
                writer.write(factory.getPhenopacketBasedQuery());
            } catch (IOException e) {
                throw new PhenolRuntimeException(e.getMessage());
            }
            // output text case with differential
            outpath = txt_with_differential + File.separator + pmid + "-case-with-ddx.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
                writer.write(factory.getCaseWithDifferentialTxt());
            } catch (IOException e) {
                throw new PhenolRuntimeException(e.getMessage());
            }
            // outpout case prior to discussion with other doctors
            outpath = txt_without_discussion + File.separator + pmid + "-case-prior-to-discussion.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
                writer.write(factory.getCasePriorToDiscussionTxt());
            } catch (IOException e) {
                throw new PhenolRuntimeException(e.getMessage());
            }
            caseIdToDxMap.put(pmid, factory.getDiagnosis());
            n_output++;
        }
        System.out.printf("We output %d cases from %d valid cases.\n", n_output, validParsedCases);
        File dxlist = new File(outDir + File.separator + "diagnosis_list.tsv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dxlist))) {
            for (var e : caseIdToDxMap.entrySet()) {
                writer.write(String.format("%s\t%s\n", e.getKey(), e.getValue()));
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException(e.getMessage());
        }
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
