package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.impl.english.EnglishPromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.german.GermanPromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.german.PpktPhenotypicfeatureGerman;
import org.monarchinitiative.phenopacket2prompt.output.impl.spanish.*;
import org.monarchinitiative.phenopacket2prompt.output.impl.dutch.*;
import org.monarchinitiative.phenopacket2prompt.output.impl.italian.*;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PromptGenerator {




    String queryHeader();
    String getIndividualInformation(PpktIndividual ppktIndividual);

    String formatFeatures( List<OntologyTerm> ontologyTerms);

    String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms);

    static PromptGenerator english(){
        return new EnglishPromptGenerator();
    }

    static PromptGenerator spanish(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureSpanish(international);
        return new SpanishPromptGenerator(pfgen);
    }


    static PromptGenerator dutch(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureDutch(international);
        return new DutchPromptGenerator(pfgen);
    }

    static PromptGenerator german(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureGerman(international);
        return new GermanPromptGenerator(pfgen);
    }
    static PromptGenerator italian(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureItalian(international);
        return new ItalianPromptGenerator(pfgen);
    }


    /**
     * The following structure should work for most other languages, but the function
     * can be overridden if necessary.
     * @param individual The individual for whom we are creating the prompt
     * @return the prompt text
     */
    default String createPrompt(PpktIndividual individual) {
        String individualInfo = getIndividualInformation(individual);
        List<OntologyTerm> onsetTerms = individual.getPhenotypicFeaturesAtOnset();
        List<OntologyTerm> unspecifiedAgeTerms = individual.getPhenotypicFeaturesAtOnsetWithoutSpecifiedAge();
        Map<PhenopacketAge, List<OntologyTerm>> pfMap = individual.extractSpecifiedAgePhenotypicFeatures();
        // For creating the prompt, we first report the onset and the unspecified terms together, and then
        // report the rest
        onsetTerms.addAll(unspecifiedAgeTerms);
        String onsetFeatures = formatFeatures(onsetTerms);
        StringBuilder sb = new StringBuilder();
        sb.append(queryHeader());
        sb.append(individualInfo).append(" ").append(onsetFeatures);
        for (var entry: pfMap.entrySet()) {
            String vignette = getVignetteAtAge(entry.getKey(), individual.getSex(), entry.getValue());
            sb.append(vignette).append(" ");
        }
        return sb.toString();
    }


    default Set<String> getMissingTranslations() {
        return Set.of();
    }





}
