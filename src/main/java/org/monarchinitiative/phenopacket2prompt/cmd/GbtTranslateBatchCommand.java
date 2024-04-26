package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketDisease;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
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
        List<File> ppktFiles = getAllPhenopacketJsonFiles();
        createDir("prompts");
        outputPromptsEnglish(ppktFiles, hpo);
        return 0;
    }



    private String getFileName(String phenopacketID) {
        return phenopacketID.replaceAll("[^\\w]", phenopacketID).replaceAll("/","_") + "-prompt.txt";
    }


    private void outputPromptsEnglish(List<File> ppktFiles, Ontology hpo) {
        createDir("prompts/en");
        PromptGenerator generator = PromptGenerator.english(hpo);
        List<String> diagnosisList = new ArrayList<>();
        for (var f: ppktFiles) {
            PpktIndividual individual = new PpktIndividual(f);
            List<PhenopacketDisease> diseaseList = individual.getDiseases();
            if (diseaseList.size() != 1) {
                System.err.println(String.format("[ERROR] Got %d diseases for %s.\n", diseaseList.size(), individual.getPhenopacketId()));
                continue;
            }
            PhenopacketDisease pdisease = diseaseList.get(0);
            String promptFileName = getFileName( individual.getPhenopacketId());
            String diagnosisLine = String.format("%s\t%s\t%s\t%s", pdisease.getDiseaseId(), pdisease.getLabel(), promptFileName, f.getAbsolutePath());
            try {
                diagnosisList.add(diagnosisLine);
                String prompt = generator.createPrompt(individual);
                outputPrompt(prompt, promptFileName, "prompts/en");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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