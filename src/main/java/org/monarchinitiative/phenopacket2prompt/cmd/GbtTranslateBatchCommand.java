package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.monarchinitiative.phenopacket2prompt.output.CorrectResult;
import org.monarchinitiative.phenopacket2prompt.output.PpktCopy;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
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

    @CommandLine.Option(names = {"-o", "--outdir"},
            description = "path to outdir")
    private String outdirname = Utility.PROMPT_DIR;

    @CommandLine.Option(names = {"-d", "--dir"}, description = "Path to directory with JSON phenopacket files", required = true)
    private String ppktDir;

    private String currentLanguageCode = null;
    private int currentCount;

    @Override
    public Integer call() throws Exception {
        File hpJsonFile = new File(hpoJsonPath);
        boolean useExactMatching = true;
        if (! hpJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hp.json at " + hpJsonFile.getAbsolutePath());
        }
        Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
        LOGGER.info("HPO version {}", hpo.version().orElse("n/a"));
        FenominalParser parser = new FenominalParser(hpJsonFile, useExactMatching);
        File translationsFile = new File(translationsPath);
        if (! translationsFile.isFile()) {
            System.err.printf("Could not find translations file at %s. Try download command", translationsPath);
            return 1;
        }
        Utility utility = new Utility(translationsFile);
        // parse something

        List<File> ppktFiles = Utility.getAllPhenopacketJsonFiles(ppktDir);
        Utility.createDir(outdirname);
        List<CorrectResult>  correctResultList = Utility.outputPromptsEnglish(ppktFiles);
        // output all non-English languages here
        // SPANISH
        PromptGenerator spanish = utility.spanish();
        Utility.outputPromptsInternational(ppktFiles,"es", spanish);
        // Czech
        PromptGenerator czech = utility.czech();
        Utility.outputPromptsInternational(ppktFiles, "cs", czech);
        // Dutch
        PromptGenerator dutch = utility.dutch();
        Utility.outputPromptsInternational(ppktFiles,"nl", dutch);
        // GERMAN
        PromptGenerator german = utility.german();
        Utility.outputPromptsInternational(ppktFiles,"de", german);
        // ITALIAN
        PromptGenerator italian = utility.italian();
        Utility.outputPromptsInternational(ppktFiles,"it", italian);
        //Turkish
        PromptGenerator turkish = utility.turkish();
        Utility.outputPromptsInternational(ppktFiles,"tr", turkish);
        // chinese
        PromptGenerator chinese = utility.chinese();
        Utility.outputPromptsInternational(ppktFiles,"zh", chinese);

        // output original phenopackets
        PpktCopy pcopy = new PpktCopy(new File(outdirname));
        for (var file : ppktFiles) {
            pcopy.copyFile(file);
        }
        // output file with correct diagnosis list
        Utility.outputCorrectPPKt(correctResultList);
        return 0;
    }







}
