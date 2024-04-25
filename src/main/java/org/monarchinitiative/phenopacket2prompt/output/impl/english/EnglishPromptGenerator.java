package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.*;

import java.util.*;

public class EnglishPromptGenerator implements PromptGenerator  {

    private final Ontology hpo;


    private final PhenopacketAgeSexGenerator ppktAgeGenerator;

    private final PhenopacketTextGenerator ppktTextGenerator;

    private final PpktPhenotypicFeatureGenerator ppktPhenotypicFeatureGenerator;



    public EnglishPromptGenerator(Ontology hpo){
        this.hpo = hpo;
        ppktAgeGenerator = new PpktAgeSexEnglish();
        ppktTextGenerator = new PpktTextEnglish();
        this.ppktPhenotypicFeatureGenerator = new PpktPhenotypicfeatureEnglish();
    }




    @Override
    public String getIndividualInformation(PpktIndividual ppktIndividual) {
        StringBuilder sb = new StringBuilder();
        /*String sex = sexGenerator.ppktSex(ppktIndividual);
        Optional<PhenopacketAge> lastAgeOpt = ppktIndividual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = ppktIndividual.getAgeAtOnset();
        if (lastAgeOpt.isPresent()) {
            PhenopacketAge lastExamAge = lastAgeOpt.get();
            String examAge = ppktAgeGenerator.age(lastExamAge);
            sb.append("The proband was a ").append(examAge).append( " ").append(sex).append(". ");
        } else {
            sb.append("The proband was a ").append(sex).append(". ");
        }
        if (onsetOpt.isPresent()) {
            PhenopacketAge onsetAge = onsetOpt.get();
            String onset = ppktAgeGenerator.age(onsetAge);
            sb.append("Initial manifestations of disease appeared when the proband was ").append(onset).append(". ");
        }*/
        return sb.toString();
    }

    @Override
    public String getPhenotypicFeatures(PpktIndividual ppktIndividual) {
        StringBuilder sb = new StringBuilder();
        Map<PhenopacketAge, List<OntologyTerm>> termMap = ppktIndividual.getPhenotypicFeatures();
        List<PhenopacketAge> ageList = new ArrayList<>(termMap.keySet());
        Collections.sort(ageList,(a, b) -> Integer.compare(a.totalDays(), b.totalDays()));
        for (var age: ageList) {
            List<OntologyTerm> terms = termMap.get(age);
            if (! age.specified()) {
                if (termMap.size() > 1) {
                    // if size is greater than one, there was at least one specified time point
                    if (ppktPhenotypicFeatureGenerator.hasObservedFeatures(terms)) {
                        sb.append("Additional features included ").append(ppktPhenotypicFeatureGenerator.featureList(terms)).append(". ");
                    }
                    if (ppktPhenotypicFeatureGenerator.hasExcludedFeatures(terms)) {
                        sb.append("Additional excluded features were ").append(ppktPhenotypicFeatureGenerator.excludedFeatureList(terms)).append(". ");
                    }
                } else {
                    if (ppktPhenotypicFeatureGenerator.hasObservedFeatures(terms)) {
                        sb.append("The following clinical manifestations were observed: ").append(ppktPhenotypicFeatureGenerator.featureList(terms)).append(". ");
                    }
                    if (ppktPhenotypicFeatureGenerator.hasExcludedFeatures(terms)) {
                        sb.append("The following clinical manifestations were excluded: ").append(ppktPhenotypicFeatureGenerator.excludedFeatureList(terms)).append(". ");
                    }
                }
            } else {
                String ageString = "";//ppktAgeGenerator.age(age);

                if (ppktPhenotypicFeatureGenerator.hasObservedFeatures(terms)) {
                    sb.append(ageString).append(", the following clinical manifestations were observed: ").append(ppktPhenotypicFeatureGenerator.featureList(terms)).append(". ");
                }
                if (ppktPhenotypicFeatureGenerator.hasExcludedFeatures(terms)) {
                    sb.append(ageString).append(", the following clinical manifestations were excluded: ").append(ppktPhenotypicFeatureGenerator.excludedFeatureList(terms)).append(". ");
                }
            }
        }

        return sb.toString();
    }





    @Override
    public String queryHeader() {
        return ppktTextGenerator.QUERY_HEADER();
    }
}
