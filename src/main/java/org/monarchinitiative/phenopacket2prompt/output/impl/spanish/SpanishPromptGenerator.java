package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.*;

import java.util.*;

public class SpanishPromptGenerator implements PromptGenerator {


    private final PPKtIndividualInfoGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public SpanishPromptGenerator(PpktPhenotypicFeatureGenerator pfgen) {
        ppktAgeSexGenerator = new PpktIndividualSpanish();
        ppktTextGenerator = new PpktTextSpanish();
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


    /**
     * @param individual The individual for whom we are creating the prompt
     * @return the prompt text (lacking the LLM-specific header)
     */

    @Override
    public  String createPromptWithoutHeader(PpktIndividual individual) {
        String individualInfo = getIndividualInformation(individual);
        // For creating the prompt, we first report the onset and the unspecified terms together, and then
        String onsetDescription = getVignetteAtOnset(individual);
        Map<PhenopacketAge, List<OntologyTerm>> pfMap = individual.extractSpecifiedAgePhenotypicFeatures();
        // We then report the rest, one for each specified time
        //String onsetFeatures = formatFeatures(onsetTerms);
        StringBuilder sb = new StringBuilder();
        sb.append(individualInfo).append("\n").append(onsetDescription).append("\n");
        for (var entry: pfMap.entrySet()) {
            String vignette = getVignetteAtAge(entry.getKey(), individual.getSex(), entry.getValue());
            sb.append(vignette).append("\n");
        }
        return sb.toString();
    }



}
