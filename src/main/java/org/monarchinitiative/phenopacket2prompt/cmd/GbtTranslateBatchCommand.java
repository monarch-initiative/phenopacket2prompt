package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.international.HpInternationalOboParser;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketDisease;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.CorrectResult;
import org.monarchinitiative.phenopacket2prompt.output.PpktCopy;
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
import java.util.Set;
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

    @CommandLine.Option(names = {"-o", "--outdir"},
            description = "path to outdir")
    private String outdirname = "prompts";

    @CommandLine.Option(names = {"-d", "--dir"}, description = "Path to directory with JSON phenopacket files", required = true)
    private String ppktDir;

    private String currentLanguageCode = null;
    private int currentCount;

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
        createDir(outdirname);
        List<CorrectResult>  correctResultList = outputPromptsEnglish(ppktFiles, hpo);
        // output all non-English languages here

        // SPANISH
        PromptGenerator spanish = PromptGenerator.spanish(internationalMap.get("es"));
        resetOutput("es");
        outputPromptsInternational(ppktFiles, hpo, "es", spanish);

        resetOutput("nl");
        PromptGenerator dutch = PromptGenerator.dutch(internationalMap.get("nl"));
        outputPromptsInternational(ppktFiles, hpo, "nl", dutch);
        // GERMAN
        resetOutput("de");
        PromptGenerator german = PromptGenerator.german(internationalMap.get("de"));
        outputPromptsInternational(ppktFiles, hpo, "de", german);
       
        // ITALIAN
        resetOutput("it");
        PromptGenerator italian = PromptGenerator.italian(internationalMap.get("it"));
        outputPromptsInternational(ppktFiles, hpo, "it", italian);
        resetOutput("finished");
        // output original phenopackets
        PpktCopy pcopy = new PpktCopy(new File(outdirname));
        for (var file : ppktFiles) {
            pcopy.copyFile(file);
        }

        // output file with correct diagnosis list
        outputCorrectResults(correctResultList);
        return 0;
    }

    private void resetOutput(String es) {
        if (currentLanguageCode != null) {
            System.out.printf("Finished writing %d phenopackets in %s\n", currentCount, currentLanguageCode);
        }
        currentLanguageCode = es;
        currentCount = 0;
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
        System.out.printf("[INFO] Output a total of %d prompts in en, es, nl, de, and it.\n", correctResultList.size());
    }


    private String getFileName(String phenopacketID, String languageCode) {
        return phenopacketID.replaceAll("[^\\w]","_") + "_" + languageCode + "-prompt.txt";
    }



    private void outputPromptsInternational(List<File> ppktFiles, Ontology hpo, String languageCode, PromptGenerator generator) {
        String dirpath = String.format("prompts/%s", languageCode);
        createDir(dirpath);
        List<String> diagnosisList = new ArrayList<>();
        for (var f: ppktFiles) {
            PpktIndividual individual = PpktIndividual.fromFile(f);
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                String errmsg = String.format("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId());
                throw new PhenolRuntimeException(errmsg);
            }
            PhenopacketDisease pdisease = diseaseList.getFirst();
            String promptFileName = getFileName( individual.getPhenopacketId(), languageCode);
            String diagnosisLine = String.format("%s\t%s\t%s\t%s", pdisease.getDiseaseId(), pdisease.getLabel(), promptFileName, f.getAbsolutePath());
            try {
                diagnosisList.add(diagnosisLine);
                String prompt = generator.createPrompt(individual);
                outputPrompt(prompt, promptFileName, dirpath);
            } catch (Exception e) {
                System.err.printf("[ERROR] Could not process %s: %s\n", promptFileName, e.getMessage());
                //e.printStackTrace();
            }
        }
        Set<String> missing = generator.getMissingTranslations();
        if (! missing.isEmpty()) {
            for (var m : missing) {
                System.out.printf("[%s] Missing: %s\n", languageCode, m);
            }
        }
    }


    private List<CorrectResult> outputPromptsEnglish(List<File> ppktFiles, Ontology hpo) {
        createDir("prompts/en");
        List<CorrectResult> correctResultList = new ArrayList<>();
        PromptGenerator generator = PromptGenerator.english();

        for (var f: ppktFiles) {
            PpktIndividual individual =  PpktIndividual.fromFile(f);
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                System.err.printf("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId());
                continue;
            }
            PhenopacketDisease pdisease = diseaseList.getFirst();
            String promptFileName = getFileName( individual.getPhenopacketId(), "en");
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
        System.out.printf("%s      %d.\r", currentLanguageCode, currentCount);
        currentCount++;
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
            else if (item.isFile() && item.getName().endsWith(".json")) {
                ppktFiles.add(item);
            }
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
