package org.monarchinitiative.phenopacket2prompt.output.impl.japanese;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;

import java.util.List;
import java.util.Set;

public class JapanesePromptGenerator implements PromptGenerator {

    private final PPKtIndividualInfoGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator = new PhenopacketTextGenerator() {};

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public JapanesePromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        ppktAgeSexGenerator = new PpktIndividualJapanese();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }




    @Override
    public String queryHeader() {
        return ppktTextGenerator.LLM_PROMPT_HEADER("japanese");
    }

    @Override
    public String queryFooter() {
        return ppktTextGenerator.LLM_PROMPT_FOOTER("japanese");
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
            case MALE -> "彼";
            case FEMALE -> "彼ら";
            default -> "とうにん";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtEncounter(person, ageString, terms);
    }

    @Override
    public  String getVignetteAtOnset(PpktIndividual individual){
        String person = switch (individual.getSex()) {
            case MALE -> "えー";
            case FEMALE -> "シエ";
            default -> "関係者";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtOnset(person, individual.getPhenotypicFeaturesAtOnset());
    }



    @Override
    public Set<String> getMissingTranslations() {
        return this.ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }


}
