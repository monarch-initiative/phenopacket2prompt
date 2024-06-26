package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.mining.CaseBundle;
import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.CorrectResult;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.EnglishPromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "batchmine",
        mixinStandardHelpOptions = true,
        description = "Batch Text mine, Translate, and Output phenopacket and prompt")
public class BatchMineCommand implements Callable<Integer> {
   private static final Logger LOGGER = LoggerFactory.getLogger(BatchMineCommand.class);

    @CommandLine.Option(names={"-d","--data"}, description ="directory to download data (default: ${DEFAULT-VALUE})" )
    public String datadir="data";

    @CommandLine.Option(names={"-i","--inputdir"}, description ="input files (directory)" )
    public String input = "docs/cases/"; // provide path for testing

    @CommandLine.Option(names = { "-o", "--output"}, description = "Path to output file dir(default: ${DEFAULT-VALUE})")
    private String output = Utility.TEXT_MINED_DIR;

    @CommandLine.Option(names = {"-e", "--exact"}, description = "Use exact matching algorithm")
    private boolean useExactMatching = false;

    @CommandLine.Option(names = {"--translations"},
            description = "path to translations file")
    private String translationsPath = "data/hp-international.obo";

    @CommandLine.Option(names = {"--verbose"}, description = "show results in shell (default is to just write to file)")
    private boolean verbose;



    @Override
    public Integer call() throws Exception {
        File inDirectory = new File(input);
        if (!inDirectory.isDirectory()) {
            throw new PhenolRuntimeException("Could not find directory at " + input);
        }
        File hpoJsonFile = new File(datadir + File.separator + "hp.json");
        if (! hpoJsonFile.isFile()) {
            System.out.printf("[ERROR] Could not find hp.json file at %s\nRun download command first\n", hpoJsonFile.getAbsolutePath());
        }
         // get Individuals from text mining
        FenominalParser parser = new FenominalParser(hpoJsonFile, useExactMatching);
        List<CaseBundle> caseBundleList = Utility.getAllCaseBundlesFromDirectory(inDirectory, parser);
        List<PpktIndividual> individualList = caseBundleList.stream().
                map(CaseBundle::individual).
                toList();

        Utility.createDir(output);
        List<CorrectResult>  correctResultList = Utility.outputPromptsEnglishFromIndividuals(individualList, output);
        // OUtput prompts that use the original texts.
        EnglishPromptGenerator generator = new EnglishPromptGenerator();
        String queryHeader = generator.queryHeader();
        outputOriginalTextsAsPrompts(queryHeader, caseBundleList);

        // output file with correct diagnosis list
        Utility.outputCorrectTextmined(correctResultList);
        return 0;
    }

    private void outputOriginalTextsAsPrompts(String queryHeader, List<CaseBundle> caseBundleList) {
        String textMinedDir = Utility.TEXT_MINED_DIR;
        String subdir = "original";
        Path path = Paths.get(textMinedDir, subdir);
        Utility.createDir(path.toString());
        for (var cbundle : caseBundleList) {
            String prompt = String.format("%s%s", queryHeader, cbundle.caseReport().caseText());
            String fname = String.format("%s%s%s.txt",
                    path.toAbsolutePath().toString(),
                    File.separator,
                    cbundle.caseReport().pmid().replace(":", "_"));
            try  (BufferedWriter bw = new BufferedWriter(new FileWriter(fname))){
                bw.write(prompt);


            } catch (IOException e) {
                LOGGER.error("Could not write prompt: {}", e.getMessage());
                throw new PhenolRuntimeException(e);
            }
        }

    }


    /*




    private void outputTextmined(FenominalParser parser) {

        List<CaseBundle> caseBundleList = Utility.getCaseBundleList(input, parser);
        if (caseBundleList.isEmpty()) {
            System.err.println("Could not extract cases from " + input);
        }
        // for now, just output one case
        Utility.outputPromptFromCaseBundle(caseBundleList.getFirst().individual(), output);
    }

     */
}
