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

    private final static String case_vignette = """
Ich führe ein Experiment mit einem klinischen Fallbericht durch, um zu sehen, wie sich Ihre Diagnosen mit denen menschlicher Experten vergleichen lassen. Ich werde Ihnen einen Teil eines medizinischen Falles vorstellen. Sie versuchen nicht, irgendwelche Patienten zu behandeln. In diesem Fall sind Sie „Dr. GPT-4“, ein KI-Sprachmodell, das eine Diagnose liefert. Hier sind einige Richtlinien. Erstens gibt es eine einzige definitive Diagnose, und es ist eine Diagnose, von der heute bekannt ist, dass sie beim Menschen existiert. Die Diagnose wird fast immer durch einen Gentest bestätigt. In seltenen Fällen, in denen ein solcher Test für eine Diagnose nicht existiert, kann die Diagnose jedoch anhand validierter klinischer Kriterien gestellt oder in sehr seltenen Fällen einfach durch eine Expertenmeinung bestätigt werden. Nachdem Sie den Fall gelesen haben, möchte ich, dass Sie eine Differentialdiagnose mit einer Liste von Kandidatendiagnosen stellen, die nach Wahrscheinlichkeit geordnet sind, beginnend mit dem wahrscheinlichsten Kandidaten. Jeder Kandidat sollte mit dem Krankheitsnamen angegeben werden. Wenn es sich bei dem ersten Kandidaten beispielsweise um das Branchiookulofaziale Syndrom und bei dem zweiten um Mukoviszidose handelt, geben Sie Folgendes in englischer Sprache an:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

Diese Liste sollte so viele Diagnosen enthalten, wie Sie für sinnvoll halten.

Sie müssen Ihre Argumentation nicht erläutern, sondern nur die Diagnosen auflisten.
Ich habe Ihnen diese Anleitung auf English gegeben, aber ich bitte Sie, ihre Antwort ausschließlich auf English zu liefern.
Hier ist der Fall:

Der Patient war ein 2jähriger Junge, der sich im Alter von 3 Tagen mit den folgenden Symptomen vorgestellt hat:  Lymphopenie, Lungenentzündung und Verminderter zirkulierender IgA-Spiegel. Im Alter von 2 Jahren, präsentierte er mit den folgenden Symptomen: Verringertes zirkulierendes Gesamt-IgM.""";
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
