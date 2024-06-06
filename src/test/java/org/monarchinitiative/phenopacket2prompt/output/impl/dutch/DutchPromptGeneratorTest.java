package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

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

public class DutchPromptGeneratorTest {

    private final static String case_vignette = """
Ik voer een experiment uit met behulp van een klinisch casusrapport om te zien hoe jouw diagnoses zich verhouden tot die
van menselijke experts. Ik zal je kennis laten maken met een deel van een medische casus. Ze proberen niet zomaar
patiënten te behandelen. In dit geval ben je “Dr. GPT-4”, een AI-taalmodel dat een diagnose stelt.
Hier zijn enkele richtlijnen. Ten eerste bestaat er één definitieve diagnose, en het is een diagnose waarvan nu bekend
is dat deze ook bij mensen voorkomt. De diagnose wordt vrijwel altijd bevestigd door een genetische test.
In zeldzame gevallen waarin een dergelijke diagnosetest niet bestaat, kan de diagnose echter worden gesteld aan de hand
van gevalideerde klinische criteria of, in zeer zeldzame gevallen, eenvoudigweg worden bevestigd door het advies van een
deskundige. Na het lezen van de casus zou ik graag willen dat u een differentiële diagnose stelt met een lijst met
kandidaat-diagnoses, gerangschikt op waarschijnlijkheid, te beginnen met de meest waarschijnlijke kandidaat.
Elke kandidaat moet worden geïdentificeerd met de ziektenaam.
Als de eerste kandidaat bijvoorbeeld het branchioculofaciaal syndroom is en de tweede cystische fibrose,
geef dan het volgende in het Engels op:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

Deze lijst moet zoveel diagnoses bevatten als jij denkt dat nuttig is.

Je hoeft je redenering niet uit te leggen, vermeld gewoon de diagnoses.
Ik heb je deze instructies in het Nederlands gegeven, maar ik vraag jou je antwoord alleen in het Engels te geven.
Hier is het geval:

De patiënt was een 2-jarige jongen die zich op de leeftijd van 3 dagen presenteerde met de volgende symptomen:
lymfopenie, longontsteking en verlaagde circulerende IgA-waarden.
Op de leeftijd van 2 jaar kreeg hij de volgende symptomen: Verminderd circulerend totaal IgM.""";
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
        PromptGenerator german = PromptGenerator.german(internationalMap.get("de"));
        String prompt = german.createPrompt(twoYears());
        assertEquals(case_vignette, prompt.trim());
    }








}
