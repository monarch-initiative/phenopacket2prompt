package org.monarchinitiative.phenopacket2prompt;


import org.monarchinitiative.phenopacket2prompt.cmd.*;
import picocli.CommandLine;

import java.util.concurrent.Callable;
@CommandLine.Command(name = "phenopacket2prompt", mixinStandardHelpOptions = true, version = "0.2.0",
        description = "Convert phenopacket to prompt for GPT")
public class Main implements Callable<Integer> {

    public static void main(String[] args){
        if (args.length == 0) {
            // if the user doesn't pass any command or option, add -h to show help
            args = new String[]{"-h"};
        }

        CommandLine cline = new CommandLine(new Main())
                .addSubcommand("batch", new GbtTranslateBatchCommand())
                .addSubcommand("download", new DownloadCommand())
                .addSubcommand("prompt", new PromptCommand())
                .addSubcommand("mine", new TextMineCommand())
                .addSubcommand("translate", new GptTranslateCommand())
                ;
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }


    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }




}
