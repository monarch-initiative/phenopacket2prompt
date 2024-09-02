package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextGerman implements PhenopacketTextGenerator {

    @Override
    public String GPT_PROMPT_HEADER() {
        return  """
Ich führe ein Experiment mit einem klinischen Fallbericht durch, um zu sehen, wie sich Ihre Diagnosen mit denen menschlicher Experten vergleichen lassen. Ich werde Ihnen einen Teil eines medizinischen Falles vorstellen. Sie versuchen nicht, irgendwelche Patienten zu behandeln. In diesem Fall sind Sie „Dr. GPT-4“, ein KI-Sprachmodell, das eine Diagnose liefert. Hier sind einige Richtlinien. Erstens gibt es eine einzige definitive Diagnose, und es ist eine Diagnose, von der heute bekannt ist, dass sie beim Menschen existiert. Die Diagnose wird fast immer durch einen Gentest bestätigt. In seltenen Fällen, in denen ein solcher Test für eine Diagnose nicht existiert, kann die Diagnose jedoch anhand validierter klinischer Kriterien gestellt oder in sehr seltenen Fällen einfach durch eine Expertenmeinung bestätigt werden. Nachdem Sie den Fall gelesen haben, möchte ich, dass Sie eine Differentialdiagnose mit einer Liste von Kandidatendiagnosen stellen, die nach Wahrscheinlichkeit geordnet sind, beginnend mit dem wahrscheinlichsten Kandidaten. Jeder Kandidat sollte mit dem Krankheitsnamen angegeben werden. Wenn es sich bei dem ersten Kandidaten beispielsweise um das Branchiookulofaziale Syndrom und bei dem zweiten um Mukoviszidose handelt, geben Sie Folgendes in englischer Sprache an:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

Diese Liste sollte so viele Diagnosen enthalten, wie Sie für sinnvoll halten.

Sie müssen Ihre Argumentation nicht erläutern, sondern nur die Diagnosen auflisten. 
Ich habe Ihnen diese Anleitung auf Deutsch gegeben, aber ich bitte Sie, Ihre Antwort ausschließlich auf English zu liefern.
Hier ist der Fall:
             
""";
    }

}
