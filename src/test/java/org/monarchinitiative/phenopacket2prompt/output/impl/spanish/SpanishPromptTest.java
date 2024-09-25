package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.international.HpInternationalOboParser;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualBase.*;

/**
 * Test only works with local hpo-international.obo
 */
@Disabled
public class SpanishPromptTest {

    private static final String case_vignette = """
Estoy realizando un experimento con el informe de un caso clínico para comparar sus diagnósticos con los de expertos humanos. Les voy a dar parte de un caso médico. No estás intentando tratar a ningún paciente. En este caso, usted es el “Dr. GPT-4”, un modelo de lenguaje de IA que proporciona un diagnóstico. Aquí hay algunas pautas. En primer lugar, existe un único diagnóstico definitivo, y es un diagnóstico que hoy se sabe que existe en humanos. El diagnóstico casi siempre se confirma mediante algún tipo de prueba genética, aunque en casos raros cuando no existe dicha prueba para un diagnóstico, el diagnóstico puede realizarse utilizando criterios clínicos validados o, muy raramente, simplemente confirmado por la opinión de un experto. Después de leer el caso, quiero que haga un diagnóstico diferencial con una lista de diagnósticos candidatos clasificados por probabilidad comenzando con el candidato más probable. Cada candidato debe especificarse con el nombre de la enfermedad. Por ejemplo, si el primer candidato es el síndrome branquiooculofacial y el segundo es la fibrosis quística, proporcione lo siguiente, en Inglés:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

Esta lista debe proporcionar tantos diagnósticos como considere razonables.

No es necesario que explique su razonamiento, simplemente enumere los diagnósticos.
Te estoy dando estas instrucciones en Español pero quiero que proveas todas tus respuestas en Inglés.
Este es el caso:

El paciente era un niño de 2 años que se presentó a la edad de 3 dias con Linfopenia, Neumonía y Déficit de IgA. A la edad de 2 años, el presentó Déficit de IgM.""";

    @Test
    public void test() {
        var ppktI = PMID_9312167_A();
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
        PromptGenerator german = PromptGenerator.spanish(internationalMap.get("es"));
        String prompt = german.createPrompt(twoYears());
        assertEquals(case_vignette, prompt.trim());
    }


    @Test
    public void testNoObservedAtOnset() {
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
        PromptGenerator spanish = PromptGenerator.spanish(internationalMap.get("es"));
        String prompt = spanish.createPrompt(onlyExcludedAtPresentation());
        assertEquals(case_vignette, prompt.trim());
    }


}
