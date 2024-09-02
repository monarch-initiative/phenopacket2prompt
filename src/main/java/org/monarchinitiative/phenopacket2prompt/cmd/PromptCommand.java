package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "prompt", aliases = {"P"},
        mixinStandardHelpOptions = true,
        description = "Create a prompt from one phenopacket")
public class PromptCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(PromptCommand.class);


    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";


    @CommandLine.Option(names = {"-p", "--ppkt"}, description = "Path to JSON phenopacket file", required = true)
    private String ppkt;

    @CommandLine.Option(names = {"-o", "--out"}, description = "Name of output prompt file")
    private String outfile = null;



    @Override
    public Integer call() throws Exception {
        File hpJsonFile = new File(hpoJsonPath);
        if (! hpJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hp.json at " + hpJsonFile.getAbsolutePath());
        }
        Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
        LOGGER.info("HPO version {}", hpo.version().orElse("n/a"));
        PromptGenerator generator = PromptGenerator.english();
        PpktIndividual individual = PpktIndividual.fromFile(new File(ppkt));
        String prompt = generator.createPrompt(individual);
        System.out.println(prompt);
        if (outfile != null) {
            LOGGER.trace("Writing prompt to {}", outfile);
            Path path = Path.of(outfile);
            Files.writeString(path, prompt);
        }
        return 0;
    }
}
