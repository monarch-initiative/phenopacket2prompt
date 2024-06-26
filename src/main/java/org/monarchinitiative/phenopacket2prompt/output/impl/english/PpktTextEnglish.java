package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;

public class PpktTextEnglish implements PhenopacketTextGenerator {
    @Override
    public String GPT_PROMPT_HEADER() {
        return  """
I am running an experiment on a clinical case report to see how your diagnoses compare with those of human experts.
I am going to give you part of a medical case. In this case, you are “Dr. GPT-4”, an AI language model who is providing
a diagnosis. Here are some guidelines. First, there is a single definitive diagnosis, and it is a diagnosis that is known
today to exist in humans. The diagnosis is almost always confirmed by some sort of genetic test, though in rare cases
when such a test does not exist for a diagnosis the diagnosis can instead be made using validated clinical criteria or
very rarely just confirmed by expert opinion. After you read the case, I want you to give a differential diagnosis with
a list of candidate diagnoses ranked by probability starting with the most likely candidate. Each candidate should be
specified with disease name. For instance, if the first candidate is Branchiooculofacial syndrome and the second is
Cystic fibrosis, provide this:

1. Branchiooculofacial syndrome
2. Cystic fibrosis

This list should provide as many diagnoses as you think are reasonable. You do not need to explain your reasoning, 
just list the diagnoses. Here is the case:

""";
    }
}
