package org.monarchinitiative.phenopacket2prompt.output.impl.chinese;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChinesePromptGenerator implements PromptGenerator {

    private final PPKtIndividualInfoGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator = new PhenopacketTextGenerator() {};

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public ChinesePromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        ppktAgeSexGenerator = new PpktIndividualChinese();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }




    @Override
    public String queryHeader() {
        return ppktTextGenerator.LLM_PROMPT_HEADER("chinese");
    }

    @Override
    public String queryFooter() {
        return ppktTextGenerator.LLM_PROMPT_FOOTER("chinese");
    }
    @Override
    public String getIndividualInformation(PpktIndividual ppktIndividual) {
        return this.ppktAgeSexGenerator.getIndividualDescription(ppktIndividual);
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        return ppktPhenotypicFeatureGenerator.formatFeatures(ontologyTerms);
    }

    @Override
    public String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms) {
        String ageString = this.ppktAgeSexGenerator.atAgeForVignette(page);
        String person = switch (psex) {
            case MALE -> "他";
            case FEMALE -> "她";
            default -> "患者";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtEncounter(person, ageString, terms);
    }

    @Override
    public  String getVignetteAtOnset(PpktIndividual individual){
        String person = switch (individual.getSex()) {
            case MALE -> "他";
            case FEMALE -> "她";
            default -> "患者";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtOnset(person, individual.getPhenotypicFeaturesAtOnset());
    }



    @Override
    public Set<String> getMissingTranslations() {
        return this.ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }


}
