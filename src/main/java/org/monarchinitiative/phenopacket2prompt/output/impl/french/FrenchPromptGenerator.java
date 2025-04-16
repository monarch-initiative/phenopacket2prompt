package org.monarchinitiative.phenopacket2prompt.output.impl.french;

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

public class FrenchPromptGenerator implements PromptGenerator {


    private final PPKtIndividualInfoGenerator ppktAgeSexGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator = new PhenopacketTextGenerator() {};

    public FrenchPromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        ppktAgeSexGenerator = new PpktIndividualFrench();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }

    @Override
    public String queryHeader() {
        return ppktTextGenerator.LLM_PROMPT_HEADER("french");
    }

    @Override
    public String queryFooter() {
        return ppktTextGenerator.LLM_PROMPT_FOOTER("french");
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
        String features = formatFeatures(terms);
        return String.format("%s, %s presentó %s", ageString, ppktAgeSexGenerator.heSheIndividual(psex), features);
    }



    @Override
    public Set<String> getMissingTranslations() {
        return this.ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }


    @Override
    public String getVignetteAtOnset(PpktIndividual individual){
        String person = switch (individual.getSex()) {
            case MALE -> "Él";
            case FEMALE -> "Ella";
            default -> "La persona afectada";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtOnset(person, individual.getPhenotypicFeaturesAtOnset());
    }

}
