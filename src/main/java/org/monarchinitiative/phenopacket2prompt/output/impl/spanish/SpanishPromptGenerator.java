package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.*;

import java.util.*;

public class SpanishPromptGenerator implements PromptGenerator {

    private final Ontology hpo;


    private final PhenopacketAgeSexGenerator ppktAgeSexGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public SpanishPromptGenerator(Ontology hpo, PpktPhenotypicFeatureGenerator pfgen) {
        this.hpo = hpo;
        ppktAgeSexGenerator = new PpktAgeSexSpanish();
        ppktTextGenerator = new PpktTextSpanish();
        this.ppktPhenotypicFeatureGenerator = pfgen;
    }

    @Override
    public String queryHeader() {
        return ppktTextGenerator.QUERY_HEADER();
    }

    @Override
    public String getIndividualInformation(PpktIndividual ppktIndividual) {
        StringBuilder sb = new StringBuilder();
       /* String sex = sexGenerator.ppktSex(ppktIndividual);
        Optional<PhenopacketAge> lastAgeOpt = ppktIndividual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = ppktIndividual.getAgeAtOnset();
        if (lastAgeOpt.isPresent()) {
            PhenopacketAge lastExamAge = lastAgeOpt.get();
            String examAge = ppktAgeSexGenerator.age(lastExamAge);
            sb.append("El probando era un ").append(examAge).append( " ").append(sex).append(". ");
        } else {
            sb.append("El probando era un ").append(sex).append(". ");
        }
        if (onsetOpt.isPresent()) {
            PhenopacketAge onsetAge = onsetOpt.get();
            String onset = ppktAgeSexGenerator.age(onsetAge);
            sb.append("Las manifestaciones iniciales de la enfermedad aparecieron cuando el probando era ").append(onset).append(". ");
        }*/
        return sb.toString();
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        return "";
    }

    @Override
    public String getVignetteAtAge(PhenopacketAge page, PhenopacketSex psex, List<OntologyTerm> terms) {
        return "";
    }




    @Override
    public String createPrompt(PpktIndividual individual) {
        return "";
    }


}
