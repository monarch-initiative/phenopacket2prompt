package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface PpktPhenotypicFeatureGenerator {

    String formatFeatures(List<OntologyTerm> ontologyTerms);

    default String featuresAtEncounter(List<OntologyTerm> ontologyTerms) {
        return ""; //TODO
    }



    default List<String> getObservedFeaturesAsStr(List<OntologyTerm> oterms) {
        return   oterms.stream()
                .filter(Predicate.not(OntologyTerm::isExcluded))
                .map(OntologyTerm::getLabel)
                .toList();
    }

    default List<String> getExcludedFeaturesAsStr(List<OntologyTerm> oterms) {
        return   oterms.stream()
                .filter(OntologyTerm::isExcluded)
                .map(OntologyTerm::getLabel)
                .toList();
    }

    default List<OntologyTerm> getObservedFeatures(List<OntologyTerm> oterms) {
        return   oterms.stream()
                .filter(Predicate.not(OntologyTerm::isExcluded))
                .toList();
    }

    default List<OntologyTerm> getExcludedFeatures(List<OntologyTerm> oterms) {
        return   oterms.stream()
                .filter(OntologyTerm::isExcluded)

                .toList();
    }

    default Set<String> getMissingTranslations() {
        return Set.of();
    }



}
