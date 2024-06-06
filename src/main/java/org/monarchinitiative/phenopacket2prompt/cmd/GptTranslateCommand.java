package org.monarchinitiative.phenopacket2prompt.cmd;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.international.HpInternationalOboParser;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;


@CommandLine.Command(name = "translate", aliases = {"T"},
        mixinStandardHelpOptions = true,
        description = "Translate phenopackets and output prompts")
public class GptTranslateCommand implements Callable<Integer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GptTranslateCommand.class);


    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";

    @CommandLine.Option(names = {"--translations"},
            description = "path to translations file")
    private String translationsPath = "data/hp-international.obo";

    @CommandLine.Option(names = {"-p", "--ppkt"}, description = "Path to JSON phenopacket file", required = true)
    private String ppkt;

    @CommandLine.Option(names = {"-l", "--language"}, description = "Language code", defaultValue = "de")
    private String languageCode;


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


        System.out.println(hpo.version().orElse("n/a"));
        PromptGenerator generator = PromptGenerator.english();
        PpktIndividual individual = PpktIndividual.fromFile(new File(ppkt));
        String prompt = generator.createPrompt(individual);
        System.out.println(prompt);
        switch (languageCode) {
            case "de" -> {
                PromptGenerator german = PromptGenerator.german(internationalMap.get("de"));
                prompt = german.createPrompt(individual);
            }
            case "es" -> {
                PromptGenerator spanish = PromptGenerator.spanish(hpo, internationalMap.get("es"));
                prompt = spanish.createPrompt(individual);
            }
            case "nl" -> {
                PromptGenerator dutch = PromptGenerator.dutch(hpo, internationalMap.get("nl"));
                prompt = dutch.createPrompt(individual);
            }
            default -> prompt = "did not recognize language code " + languageCode;
        }


        System.out.println(prompt);

        return 0;
    }
}
