package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.EnglishPromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.spanish.*;

public interface PromptGenerator {




    String queryHeader();
    String getIndividualInformation(PpktIndividual ppktIndividual);
    String getPhenotypicFeatures(PpktIndividual ppktIndividual);



    public static PromptGenerator english(Ontology ontology){

        return new EnglishPromptGenerator(ontology);
    }

    static PromptGenerator spanish(Ontology hpo, HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureSpanish(international);
        return new SpanishPromptGenerator(hpo, pfgen);
    }

    default String createPrompt(PpktIndividual individual) {
        String sb = queryHeader() +
                getIndividualInformation(individual) +
                getPhenotypicFeatures(individual);
        return sb;
    }






}
