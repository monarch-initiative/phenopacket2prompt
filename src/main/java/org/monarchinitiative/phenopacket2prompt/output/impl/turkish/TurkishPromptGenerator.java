package org.monarchinitiative.phenopacket2prompt.output.impl.turkish;

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

public class TurkishPromptGenerator implements PromptGenerator {

    private final PPKtIndividualInfoGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public TurkishPromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        ppktAgeSexGenerator = new PpktIndividualTurkish();
        ppktTextGenerator = new PpktTextTurkish();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }




    @Override
    public String queryHeader() {
        return ppktTextGenerator.GPT_PROMPT_HEADER();
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
            case MALE -> "o";
            case FEMALE -> "o";
            default -> "etkilenen kişi";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtEncounter(person, ageString, terms);
    }

    @Override
    public  String getVignetteAtOnset(PpktIndividual individual){
        String person = switch (individual.getSex()) {
            case MALE -> "O";
            case FEMALE -> "O";
            default -> "Etkilenen kişi";
        };
        return this.ppktPhenotypicFeatureGenerator.featuresAtOnset(person, individual.getPhenotypicFeaturesAtOnset());
    }



    @Override
    public Set<String> getMissingTranslations() {
        return this.ppktPhenotypicFeatureGenerator.getMissingTranslations();
    }

    /**
     * The following structure should work for most other languages, but the function
     * can be overridden if necessary.
     * @param individual The individual for whom we are creating the prompt
     * @return the prompt text
     */
    @Override
    public  String createPrompt(PpktIndividual individual) {
        String individualInfo = getIndividualInformation(individual);
        // For creating the prompt, we first report the onset and the unspecified terms together, and then
        String onsetDescription = getVignetteAtOnset(individual);
        Map<PhenopacketAge, List<OntologyTerm>> pfMap = individual.extractSpecifiedAgePhenotypicFeatures();
        // We then report the rest, one for each specified time
        //String onsetFeatures = formatFeatures(onsetTerms);
        StringBuilder sb = new StringBuilder();
        sb.append(queryHeader());
        sb.append(individualInfo).append("\n").append(onsetDescription).append("\n");
        for (var entry: pfMap.entrySet()) {
            String vignette = getVignetteAtAge(entry.getKey(), individual.getSex(), entry.getValue());
            sb.append(vignette).append("\n");
        }
        return sb.toString();
    }



}
