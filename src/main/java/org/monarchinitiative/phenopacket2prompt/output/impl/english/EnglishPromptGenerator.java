package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.*;

import java.util.*;

public class EnglishPromptGenerator implements PromptGenerator  {


    private final PPKtIndividualInfoGenerator ppktAgeGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;


    public EnglishPromptGenerator(){
        ppktAgeGenerator = new PpktIndividualEnglish();
        ppktTextGenerator = new PpktTextEnglish();
        this.ppktPhenotypicFeatureGenerator = new PpktPhenotypicFeatureEnglish();
    }


    @Override
    public String queryHeader() {
        return ppktTextGenerator.QUERY_HEADER();
    }

    @Override
    public String getIndividualInformation(PpktIndividual ppktIndividual) {
        return this.ppktAgeGenerator.getIndividualDescription(ppktIndividual);
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        return ppktPhenotypicFeatureGenerator.formatFeatures(ontologyTerms);
    }

    @Override
    public String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms) {
        String ageString = this.ppktAgeGenerator.atAge(page);
        String features = formatFeatures(terms);
        return String.format("%s, %s presented with %s", ageString, ppktAgeGenerator.heSheIndividual(psex), features);
    }

}
