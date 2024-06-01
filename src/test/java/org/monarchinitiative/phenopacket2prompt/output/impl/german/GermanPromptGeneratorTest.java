package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.international.HpInternationalOboParser;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase.twoYears;

public class GermanPromptGeneratorTest {


    @Test
    public void testCase() {
        PpktIndividual i = twoYears();
        File hpJsonFile = new File("data/hp.json");
        if (! hpJsonFile.isFile()) {
            throw new PhenolRuntimeException("Could not find hp.json at " + hpJsonFile.getAbsolutePath());
        }
        Ontology hpo = OntologyLoader.loadOntology(hpJsonFile);
        File translationsFile = new File("data/hp-international.obo");
        if (! translationsFile.isFile()) {
            System.err.printf("Could not find translations file at %s. Try download command", translationsFile.getAbsolutePath());
            return ;
        }
        HpInternationalOboParser oboParser = new HpInternationalOboParser(translationsFile);
        Map<String, HpInternational> internationalMap = oboParser.getLanguageToInternationalMap();
        PromptGenerator german = PromptGenerator.german(hpo, internationalMap.get("de"));
        String prompt = german.createPrompt(twoYears());


        System.out.println(prompt);
        assertEquals("asdf", prompt);
    }



}
