package org.monarchinitiative.phenopacket2prompt.cmd;

import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "mine", aliases = {"M"},
        mixinStandardHelpOptions = true,
        description = "Text mine and output phenopacket and prompt")
public class TextMineCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TextMineCommand.class);

    @CommandLine.Option(names={"-d","--data"}, description ="directory to download data (default: ${DEFAULT-VALUE})" )
    public String datadir="data";

    @CommandLine.Option(names={"-i","--input"}, description ="input file (text)" )
    public String input = "docs/cases/PMID_8755636.txt"; // provide path for testing TODO REMOVE ME

    @CommandLine.Option(names = { "-o", "--output"}, description = "Path to output file (default: ${DEFAULT-VALUE})")
    private String output = "fenominal-mined.txt";

    @CommandLine.Option(names = {"-e", "--exact"}, description = "Use exact matching algorithm")
    private boolean useExactMatching = false;

    @CommandLine.Option(names = {"--verbose"}, description = "show results in shell (default is to just write to file)")
    private boolean verbose;


    @Override
    public Integer call() throws Exception {




        LOGGER.info("TextMine command, input = {}", input);
        File hpoJsonFile = new File(datadir + File.separator + "hp.json");
        if (! hpoJsonFile.isFile()) {
            System.out.printf("[ERROR] Could not find hp.json file at %s\nRun download command first\n", hpoJsonFile.getAbsolutePath());
        }
        FenominalParser parser = new FenominalParser(hpoJsonFile, input, output, useExactMatching);
        Phenopacket ppkt = parser.parse(verbose);
        System.out.println(ppkt);
        return 0;


    }
}
