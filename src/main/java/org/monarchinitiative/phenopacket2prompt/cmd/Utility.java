package org.monarchinitiative.phenopacket2prompt.cmd;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.international.HpInternationalOboParser;
import org.monarchinitiative.phenopacket2prompt.mining.Case;
import org.monarchinitiative.phenopacket2prompt.mining.CaseBundle;
import org.monarchinitiative.phenopacket2prompt.mining.CaseParser;
import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketDisease;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.CorrectResult;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class provides several utility functions.
 */
public class Utility {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utility.class);

    public static final String PROMPT_DIR = "prompts";
    public static final String TEXT_MINED_DIR = "text_mined";


    private final Map<String, HpInternational> internationalMap ;

    public Utility(File translationsFile) {
        HpInternationalOboParser oboParser = new HpInternationalOboParser(translationsFile);
        this.internationalMap = oboParser.getLanguageToInternationalMap();
        LOGGER.info("Got {} translations", internationalMap.size());
    }

    public PromptGenerator german() {
        return PromptGenerator.german(internationalMap.get("de"));
    }

    public PromptGenerator spanish() {
        return PromptGenerator.spanish(internationalMap.get("es"));
    }

    public PromptGenerator dutch() {
        return PromptGenerator.dutch(internationalMap.get("nl"));
    }

    public PromptGenerator turkish() {
        return PromptGenerator.turkish(internationalMap.get("tr"));
    }

    public PromptGenerator italian() {
        return PromptGenerator.italian(internationalMap.get("it"));
    }

    public PromptGenerator chinese() {
        return PromptGenerator.chinese(internationalMap.get("zh"));
    }


    public static String getFileName(String phenopacketID, String languageCode) {
        return phenopacketID.replaceAll("[^\\w]","_") + "_" + languageCode + "-prompt.txt";
    }

    public static  void createDir(String path) {
        File pathAsFile = new File(path);
        if (!Files.exists(Paths.get(path))) {
            pathAsFile.mkdir();
        }
    }


    public static void outputCorrectTextmined(List<CorrectResult> correctResultList) {
        outputCorrectResults(correctResultList, TEXT_MINED_DIR);
    }
    public static void outputCorrectPPKt(List<CorrectResult> correctResultList) {
        outputCorrectResults(correctResultList, PROMPT_DIR);
    }

    public static  void outputCorrectResults(List<CorrectResult> correctResultList, String basename) {
        File outfile = new File(basename + File.separator + "correct_results.tsv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {
            for (var cres : correctResultList) {
                bw.write(String.format("%s\t%s\t%s\n", cres.diseaseLabel(), cres.diseaseId().getValue(), cres.promptFileName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("[INFO] Output a total of %d prompts in en, es, nl, de, and it.\n", correctResultList.size());
    }


    public static void outputPromptFromCaseBundle(String prompt, String promptFileName, String dir) {
        File outpath = new File(dir + File.separator + promptFileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outpath))) {
            bw.write(prompt);
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not output file to " + promptFileName);
        }
    }




    public static List<File> getAllPhenopacketJsonFiles(String ppktDir) {
        List<String> ppktDirectories = new ArrayList<>();
        List<File> ppktFiles = new ArrayList<>();
        File[] items = new File(ppktDir).listFiles();
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


    public static void outputPromptsInternationalFromIndividualList(List<PpktIndividual> individualList,
                                                                    String languageCode,
                                                                    String baseDir,
                                                                    PromptGenerator generator) {
        String dirpath = String.format("%s%s%s", baseDir, File.separator, languageCode);
        Utility.createDir(dirpath);
        List<String> diagnosisList = new ArrayList<>();
        for (PpktIndividual individual : individualList) {
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                String errmsg = String.format("[ERROR] Got %d diseases for \"%s\".\n", diseaseList.size(),
                        individual.getPhenopacketId());
                throw new PhenolRuntimeException(errmsg);
            }
            PhenopacketDisease pdisease = diseaseList.getFirst();
            String promptFileName = Utility.getFileName( individual.getPhenopacketId(), languageCode);
            String diagnosisLine = String.format("%s\t%s\t%s\t%s", pdisease.getDiseaseId(), pdisease.getLabel(), promptFileName, individual.getPhenopacketId());
            try {
                diagnosisList.add(diagnosisLine);
                String prompt = generator.createPrompt(individual);
                Utility.outputPromptFromCaseBundle(prompt, promptFileName, dirpath);
            } catch (Exception e) {
                String errmsg = String.format("[ERROR] Could not process %s: %s\n", promptFileName, e.getMessage());
                System.err.println(errmsg);
                LOGGER.error(errmsg);
            }
        }

    }



    public static void outputPromptsInternational(List<File> ppktFiles, String languageCode, PromptGenerator generator) {
        List<PpktIndividual> individualList = new ArrayList<>();
        for (var f: ppktFiles) {
            PpktIndividual individual = PpktIndividual.fromFile(f);
            individualList.add(individual);
        }
        outputPromptsInternationalFromIndividualList(individualList,
                languageCode,
                PROMPT_DIR,
                generator);
    }

    public static void outputPromptsInternationalMining(List<PpktIndividual> individualList,
                                                        String languageCode,
                                                        PromptGenerator generator) {
        outputPromptsInternationalFromIndividualList(individualList,
                TEXT_MINED_DIR,
                languageCode,
                generator);
    }



    public static List<CorrectResult> outputPromptsEnglish(List<File> ppktFiles) {
        Utility.createDir("prompts/en");
        List<CorrectResult> correctResultList = new ArrayList<>();
        PromptGenerator generator = PromptGenerator.english();
        int currentCount = 0;
        for (var f: ppktFiles) {
            PpktIndividual individual =  PpktIndividual.fromFile(f);
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                System.err.printf("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId());
                continue;
            }
            PhenopacketDisease pdisease = diseaseList.getFirst();
            String promptFileName = Utility.getFileName( individual.getPhenopacketId(), "en");
            try {
                String prompt = generator.createPrompt(individual);
                Utility.outputPromptFromCaseBundle(prompt, promptFileName, "prompts/en");
                System.out.printf("en      %d.\r", currentCount);
                currentCount++;
                var cres = new CorrectResult(promptFileName, pdisease.getDiseaseId(), pdisease.getLabel());
                correctResultList.add(cres);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return correctResultList;
    }


    public static List<CorrectResult> outputPromptsEnglishFromIndividuals(List<PpktIndividual> individualList, String outputDir) {
        String outd = outputDir + File.separator + "en";
        Utility.createDir(outd);
        List<CorrectResult> correctResultList = new ArrayList<>();
        PromptGenerator generator = PromptGenerator.english();
        int currentCount = 0;
        for (PpktIndividual individual: individualList) {
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                System.err.printf("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId());
                continue;
            }
            PhenopacketDisease pdisease = diseaseList.getFirst();
            String promptFileName = Utility.getFileName( individual.getPhenopacketId(), "en");
            try {
                String prompt = generator.createPrompt(individual);
                Utility.outputPromptFromCaseBundle(prompt, promptFileName, outd);
                System.out.printf("en      %d.\r", currentCount);
                currentCount++;
                var cres = new CorrectResult(promptFileName, pdisease.getDiseaseId(), pdisease.getLabel());
                correctResultList.add(cres);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return correctResultList;
    }


    public static void outputPromptFromCaseBundle(PpktIndividual individual, String output) {

        PromptGenerator generator = PromptGenerator.english();
        String prompt = generator.createPrompt(individual);
        try  {
            Path path  = Path.of(output);
            Files.writeString(path, prompt);
        } catch (IOException e) {
            LOGGER.error("Could not write prompt: {}", e.getMessage());
            throw new PhenolRuntimeException(e);
        }
    }

    public static List<CaseBundle> getCaseBundleList(String inputFile, FenominalParser fenominalParser) {
        List<CaseBundle> caseBundleList = new ArrayList<>();
        CaseParser caseParser = new CaseParser(Path.of(inputFile));
        List<Case> caseList = caseParser.getCaseList();
        for (Case cs : caseList) {
            Phenopacket ppkt = fenominalParser.parse(cs);
            PpktIndividual individual = new PpktIndividual(ppkt);
            caseBundleList.add(new CaseBundle(cs, ppkt, individual));
        }
        System.out.printf("Got %d cases.\n", caseBundleList.size());
        return caseBundleList;
    }


    public static List<CaseBundle> getAllCaseBundlesFromDirectory(File indir, FenominalParser fenominalParser) {
        if (! indir.isDirectory()) {
            throw new PhenolRuntimeException("Could not find text mining input directory at " + indir.getAbsolutePath());
        }
        List<CaseBundle> caseBundleList = new ArrayList<>();
        File[] files = indir.listFiles();
        for (File file : files) {
            if (! file.getAbsolutePath().contains("PMID") && file.getAbsolutePath().endsWith("txt")) continue;
            List<CaseBundle> bundles = getCaseBundleList(file.getAbsolutePath(), fenominalParser);
            caseBundleList.addAll(bundles);
        }
        return caseBundleList;
    }


}
