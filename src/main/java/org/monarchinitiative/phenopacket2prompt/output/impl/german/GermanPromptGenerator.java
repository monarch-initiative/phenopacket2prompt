package org.monarchinitiative.phenopacket2prompt.output.impl.german;

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

public class GermanPromptGenerator implements PromptGenerator {

    private final PPKtIndividualInfoGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public GermanPromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        ppktAgeSexGenerator = new PpktIndividualGerman();
        ppktTextGenerator = new PpktTextGerman();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }




    @Override
    public String queryHeader() {
        return ppktTextGenerator.LLM_PROMPT_HEADER();
    }

    @Override
    public String queryFooter() {
        return ppktTextGenerator.LLM_PROMPT_FOOTER();
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
            case MALE -> "er";
            case FEMALE -> "sie";
            default -> "die betroffene Person";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtEncounter(person, ageString, terms);
    }

    @Override
    public  String getVignetteAtOnset(PpktIndividual individual){
        String person = switch (individual.getSex()) {
            case MALE -> "Er";
            case FEMALE -> "Sie";
            default -> "Die betroffene Person";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtOnset(person, individual.getPhenotypicFeaturesAtOnset());
    }



    @Override
    public Set<String> getMissingTranslations() {
        return this.ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }


}
