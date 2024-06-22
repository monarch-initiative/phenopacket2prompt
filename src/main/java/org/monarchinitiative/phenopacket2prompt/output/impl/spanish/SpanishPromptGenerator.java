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



}
