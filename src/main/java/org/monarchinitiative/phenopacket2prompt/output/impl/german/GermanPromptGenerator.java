package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketIndividualInformationGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketTextGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;

import java.util.List;

public class GermanPromptGenerator implements PromptGenerator {

    private final Ontology hpo;


    private final PhenopacketIndividualInformationGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public GermanPromptGenerator(Ontology hpo, PpktPhenotypicFeatureGenerator pfgen) {
        this.hpo = hpo;
        ppktAgeSexGenerator = new PpktIndividualGerman();
        ppktTextGenerator = new PpktTextGerman();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }

    @Override
    public String queryHeader() {
        return ppktTextGenerator.QUERY_HEADER();
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
        String ageString = this.ppktAgeSexGenerator.atAge(page);
        String features = formatFeatures(terms);
        return String.format("%s, %s presentó %s", ageString, ppktAgeSexGenerator.heSheIndividual(psex), features);
    }







}