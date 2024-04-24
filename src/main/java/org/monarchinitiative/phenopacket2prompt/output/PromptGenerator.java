package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.impl.EnglishPromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.PhenopacketSexEnglish;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.PpktAgeEnglish;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.PpktPhenotypicfeatureEnglish;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.PpktTextEnglish;

public interface PromptGenerator {


    String queryHeader();
    String getIndividualInformation(PpktIndividual ppktIndividual);
    String getPhenotypicFeatures(PpktIndividual ppktIndividual);



    public static PromptGenerator english(Ontology ontology){
        PhenopacketSexGenerator sgen = new PhenopacketSexEnglish();
        PhenopacketAgeGenerator page = new PpktAgeEnglish();
        PhenopacketTextGenerator ptext = new PpktTextEnglish();
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureEnglish();
        return new EnglishPromptGenerator(ontology, sgen, page, ptext, pfgen);
    }



    default String createPrompt(PpktIndividual individual) {
        StringBuilder sb = new StringBuilder();
        sb.append(queryHeader());
        sb.append(getIndividualInformation(individual));
        sb.append(getPhenotypicFeatures(individual));

        return sb.toString();

    }

}
