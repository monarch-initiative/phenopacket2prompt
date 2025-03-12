package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.*;

import java.util.*;

public class EnglishPromptGenerator implements PromptGenerator  {


    private final PPKtIndividualInfoGenerator individualInfoGenerator;

    private final PhenopacketTextGenerator promptTextBlockGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;


    public EnglishPromptGenerator(){
        individualInfoGenerator = new PpktIndividualEnglish();
        promptTextBlockGenerator = new PpktTextEnglish();
        this.ppktPhenotypicFeatureGenerator = new PpktPhenotypicFeatureEnglish();
    }


    @Override
    public String queryHeader() {
        return promptTextBlockGenerator.GPT_PROMPT_HEADER();
    }

    @Override
    public String getIndividualInformation(PpktIndividual ppktIndividual) {
        return this.individualInfoGenerator.getIndividualDescription(ppktIndividual);
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        return ppktPhenotypicFeatureGenerator.formatFeatures(ontologyTerms);
    }

    @Override
    public String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms) {
        String ageString = this.individualInfoGenerator.atAgeForVignette(page);
        String features = formatFeatures(terms);
        return String.format("%s, %s presented with %s", ageString, individualInfoGenerator.heSheIndividual(psex), features);
    }

    /**
     * Provide a listing of observed and excluded features at disease onset. In English, we have provided the
     * age at disease onset in the individual description and will thus not repeat the age here. We
     * keep the PhenopacketAge in the interface in case it is useful for another language.
     * @param individual The subject of the phenopacket
     * @return Sentences describing terms that were observed/excluded at onset
     */
    @Override
    public String getVignetteAtOnset(PpktIndividual individual) {
        List<OntologyTerm> terms = individual.getPhenotypicFeaturesAtOnset();
        String person = switch (individual.getSex()) {
            case MALE -> "He";
            case FEMALE -> "She";
            default -> "The individual";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtOnset(person, terms);

    }

}
