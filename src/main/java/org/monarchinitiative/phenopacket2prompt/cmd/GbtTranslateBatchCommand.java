package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.international.HpInternationalOboParser;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketDisease;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.CorrectResult;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "batch", aliases = {"B"},
        mixinStandardHelpOptions = true,
        description = "Translate batch of phenopackets and output prompts")
public class GbtTranslateBatchCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GbtTranslateBatchCommand.class);


    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";

    @CommandLine.Option(names = {"--translations"},
            description = "path to translations file")
    private String translationsPath = "data/hp-international.obo";

    @CommandLine.Option(names = {"-d", "--dir"}, description = "Path to directory with JSON phenopacket files", required = true)
    private String ppktDir;

    @Override
    public Integer call() throws Exception {
        File hpJsonFile = new File(hpoJsonPath);
        if (! hpJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hp.json at " + hpJsonFile.getAbsolutePath());
        }
        Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
        LOGGER.info("HPO version {}", hpo.version().orElse("n/a"));
        File translationsFile = new File(translationsPath);
        if (! translationsFile.isFile()) {
            System.err.printf("Could not find translations file at %s. Try download command", translationsPath);
            return 1;
        }
        HpInternationalOboParser oboParser = new HpInternationalOboParser(translationsFile);
        Map<String, HpInternational> internationalMap = oboParser.getLanguageToInternationalMap();
        LOGGER.info("Got {} translations", internationalMap.size());
        List<File> ppktFiles = getAllPhenopacketJsonFiles();
        createDir("prompts");
        List<CorrectResult>  correctResultList = outputPromptsEnglish(ppktFiles, hpo);
        // output all non-English languages here
        PromptGenerator spanish = PromptGenerator.spanish(hpo, internationalMap.get("es"));
        outputPromptsInternational(ppktFiles, hpo, "es", spanish);
        PromptGenerator german = PromptGenerator.german(hpo, internationalMap.get("de"));
        outputPromptsInternational(ppktFiles, hpo, "de", german);
        // output file with correct diagnosis list
        outputCorrectResults(correctResultList);
        return 0;
    }

    private void outputCorrectResults(List<CorrectResult> correctResultList) {
        File outfile = new File("prompts" + File.separator + "correct_results.tsv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            for (var cres : correctResultList) {
                bw.write(String.format("%s\t%s\t%s\n", cres.diseaseLabel(), cres.diseaseId().getValue(), cres.promptFileName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("[INFO] Output a total of %d prompts in en and es.\n", correctResultList.size());
    }


    private String getFileName(String phenopacketID) {
        return phenopacketID.replaceAll("[^\\w]", phenopacketID).replaceAll("/","_") + "-prompt.txt";
    }



    private void outputPromptsInternational(List<File> ppktFiles, Ontology hpo, String languageCode, PromptGenerator generator) {
        String dirpath = String.format("prompts/%s", languageCode);
        createDir(dirpath);
        List<String> diagnosisList = new ArrayList<>();
        for (var f: ppktFiles) {
            PpktIndividual individual = new PpktIndividual(f);
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                System.err.printf("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId());
                continue;
            }
            PhenopacketDisease pdisease = diseaseList.get(0);
            String promptFileName = getFileName( individual.getPhenopacketId());
            String diagnosisLine = String.format("%s\t%s\t%s\t%s", pdisease.getDiseaseId(), pdisease.getLabel(), promptFileName, f.getAbsolutePath());
            try {
                diagnosisList.add(diagnosisLine);
                String prompt = generator.createPrompt(individual);
                outputPrompt(prompt, promptFileName, dirpath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private List<CorrectResult> outputPromptsEnglish(List<File> ppktFiles, Ontology hpo) {
        createDir("prompts/en");
        List<CorrectResult> correctResultList = new ArrayList<>();
        PromptGenerator generator = PromptGenerator.english(hpo);

        for (var f: ppktFiles) {
            PpktIndividual individual = new PpktIndividual(f);
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                System.err.printf("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId());
                continue;
            }
            PhenopacketDisease pdisease = diseaseList.get(0);
            String promptFileName = getFileName( individual.getPhenopacketId());
            String diagnosisLine = String.format("%s\t%s\t%s\t%s", pdisease.getDiseaseId(), pdisease.getLabel(), promptFileName, f.getAbsolutePath());
            try {
                String prompt = generator.createPrompt(individual);
                outputPrompt(prompt, promptFileName, "prompts/en");
                var cres = new CorrectResult(promptFileName, pdisease.getDiseaseId(), pdisease.getLabel());
                correctResultList.add(cres);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return correctResultList;
    }



    private void outputPrompt(String prompt, String promptFileName, String dir) {
        File outpath = new File(dir + File.separator + promptFileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outpath))) {
            bw.write(prompt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(".");
    }



    private void createDir(String path) {
        File pathAsFile = new File(path);
        if (!Files.exists(Paths.get(path))) {
            pathAsFile.mkdir();
        }
    }





    private List<File> getAllPhenopacketJsonFiles() {
        List<String> ppktDirectories = new ArrayList<>();
        List<File> ppktFiles = new ArrayList<>();
        File[] items = new File(this.ppktDir).listFiles();
        // We know that all phenopackets are located in the subdirectories
        if (!ppktDir.substring(ppktDir.length() - 1).equals("/")) {
            ppktDir += "/";
        }
        for (File item : items) {
            if (item.isDirectory())
                ppktDirectories.add(ppktDir+item.getName());
        }
        for (var f: ppktDirectories) {
            File subdir = new File(f);
            File[] files = subdir.listFiles();
            for (var ff : files) {
                if (ff.isFile() && ff.getAbsolutePath().endsWith(".json")) {
                    ppktFiles.add(ff);
                }
            }
        }
        System.out.printf("Retrieved %d files.\n", ppktFiles.size());
        return ppktFiles;
    }

}
