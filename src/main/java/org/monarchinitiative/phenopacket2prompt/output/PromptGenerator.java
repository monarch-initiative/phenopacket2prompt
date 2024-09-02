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
import org.monarchinitiative.phenopacket2prompt.output.impl.turkish.PpktPhenotypicfeatureTurkish;
import org.monarchinitiative.phenopacket2prompt.output.impl.turkish.TurkishPromptGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.chinese.*;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PromptGenerator {




    String queryHeader();
    String getIndividualInformation(PpktIndividual ppktIndividual);

    String formatFeatures( List<OntologyTerm> ontologyTerms);

    String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms);

    default String getVignetteAtOnset(PpktIndividual individual){
        return ""; // TODO  -- NON English need to implement, then remove "default"
    }




    static PromptGenerator english(){
        return new EnglishPromptGenerator();
    }

    static PromptGenerator spanish(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureSpanish(international);
        return new SpanishPromptGenerator(pfgen);
    }


    static PromptGenerator dutch(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicFeatureDutch(international);
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

    static PromptGenerator turkish(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureTurkish(international);
        return new TurkishPromptGenerator(pfgen);
    }

    static PromptGenerator chinese(HpInternational international) {
        PpktPhenotypicFeatureGenerator pfgen = new PpktPhenotypicfeatureChinese(international);
        return new ChinesePromptGenerator(pfgen);
    }

    static PromptGenerator czech(HpInternational international) {
        throw new RuntimeException("Not yet implemented!");
    }


    /**
     * The following structure should work for most other languages, but the function
     * can be overridden if necessary.
     * @param individual The individual for whom we are creating the prompt
     * @return the prompt text
     */
    default String createPrompt(PpktIndividual individual) {
       return String.format("%s%s",
               getHeader(),
               createPromptWithoutHeader(individual));
    }
    // TODO IMPLEMENT EVERYWHERE. WE ALSO NEED VERSIONS FOR EACH LLM, CONSIDER ADDING ENUM
    default String createPromptWithoutHeader(PpktIndividual individual) {
        String individualInfo = getIndividualInformation(individual);
        // For creating the prompt, we first report the onset and the unspecified terms together, and then
        List<OntologyTerm> onsetTerms = individual.getPhenotypicFeaturesAtOnset();
        Map<PhenopacketAge, List<OntologyTerm>> pfMap = individual.extractSpecifiedAgePhenotypicFeatures();
        // We then report the rest, one for each specified time
        String onsetFeatures = formatFeatures(onsetTerms);
        StringBuilder sb = new StringBuilder();

        sb.append(individualInfo).append(" ").append(onsetFeatures);
        for (var entry: pfMap.entrySet()) {
            String vignette = getVignetteAtAge(entry.getKey(), individual.getSex(), entry.getValue());
            sb.append(vignette).append(" ");
        }
        return sb.toString();
    }

    // TODO IMPLEMENT EVERYWHERE. WE ALSO NEED VERSIONS FOR EACH LLM, CONSIDER ADDING ENUM
    default String getHeader() {
        return queryHeader();
    }


    default Set<String> getMissingTranslations() {
        return Set.of();
    }





}
