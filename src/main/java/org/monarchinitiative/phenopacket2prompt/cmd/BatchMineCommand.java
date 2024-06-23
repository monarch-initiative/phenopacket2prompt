package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.mining.CaseBundle;
import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.CorrectResult;
import org.monarchinitiative.phenopacket2prompt.output.PpktCopy;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "batchmine", aliases = {"B2"},
        mixinStandardHelpOptions = true,
        description = "Batch Text mine, Translate, and Output phenopacket and prompt")
public class BatchMineCommand implements Callable<Integer> {
    @CommandLine.Option(names={"-d","--data"}, description ="directory to download data (default: ${DEFAULT-VALUE})" )
    public String datadir="data";

    @CommandLine.Option(names={"-i","--inputdir"}, description ="input files (directory)" )
    public String input = "docs/cases/"; // provide path for testing

    @CommandLine.Option(names = { "-o", "--output"}, description = "Path to output file dir(default: ${DEFAULT-VALUE})")
    private String output = "mined_out";

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
        File translationsFile = new File(translationsPath);
        if (! translationsFile.isFile()) {
            System.err.printf("Could not find translations file at %s. Try download command", translationsPath);
            return 1;
        }
        Utility utility = new Utility(translationsFile);
        List<PpktIndividual> individualList =  getIndividualsFromTextMining(inDirectory,hpoJsonFile);
        PromptGenerator spanish = utility.spanish();
        Utility.outputPromptsInternationalMining(individualList,"es", spanish);
        // Dutch
        PromptGenerator dutch = utility.dutch();
        Utility.outputPromptsInternationalMining(individualList,"nl", dutch);
        // GERMAN
        PromptGenerator german = utility.german();
        Utility.outputPromptsInternationalMining(individualList,"de", german);
        // ITALIAN
        PromptGenerator italian = utility.italian();
        Utility.outputPromptsInternationalMining(individualList,"it", italian);

        // output file with correct diagnosis list
        List<CorrectResult>  correctResultList =Utility.outputPromptsEnglishFromIndividuals(individualList);
        Utility.outputCorrectTextmined(correctResultList);
        return 0;
    }

    /**
     * Get all of the individual objects by text mining the input files
     * @param inDirectory Input directory. Should hold input files formatted for this project (demonstration)
     * @param hpoJsonFile File representing hp.json
     * @return list of individuals
     */
    protected List<PpktIndividual> getIndividualsFromTextMining(File inDirectory, File hpoJsonFile) {
        FenominalParser parser = new FenominalParser(hpoJsonFile, useExactMatching);
        List<CaseBundle> caseBundleList = Utility.getAllCaseBundlesFromDirectory(inDirectory, parser);
        return caseBundleList.stream().map(CaseBundle::individual).toList();
    }



    private void outputTextmined(FenominalParser parser) {

        List<CaseBundle> caseBundleList = Utility.getCaseBundleList(input, parser);
        if (caseBundleList.isEmpty()) {
            System.err.println("Could not extract cases from " + input);
        }
        // for now, just output one case
        Utility.outputPromptFromCaseBundle(caseBundleList.getFirst().individual(), output);
    }
}
