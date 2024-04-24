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
import java.util.concurrent.Callable;


@CommandLine.Command(name = "translate", aliases = {"T"},
        mixinStandardHelpOptions = true,
        description = "Translate phenopackets and output prompts")
public class GptTranslateCommand implements Callable<Integer> {
    Logger LOGGER = LoggerFactory.getLogger(GptTranslateCommand.class);


    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";

    @CommandLine.Option(names = {"-p", "--ppkt"}, description = "Path to JSON phenopacket file", required = true)
    private String ppkt;


    @Override
    public Integer call() throws Exception {
        File hpJsonFile = new File(hpoJsonPath);
        if (! hpJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hp.json at " + hpJsonFile.getAbsolutePath());
        }
        Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
        LOGGER.info("HPO version {}", hpo.version().orElse("n/a"));
        System.out.println(hpo.version().orElse("n/a"));
        PromptGenerator generator = PromptGenerator.english(hpo);
        PpktIndividual individual = new PpktIndividual(new File(ppkt));
        String prompt = generator.createPrompt(individual);
        System.out.println(prompt);

        return 0;
    }
}
