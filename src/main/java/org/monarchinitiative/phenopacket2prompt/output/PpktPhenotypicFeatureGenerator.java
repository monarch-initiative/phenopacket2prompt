package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;

import java.util.List;
import java.util.function.Predicate;

public interface PpktPhenotypicFeatureGenerator {


    String featureList( List<OntologyTerm> ontologyTerms);

    String excludedFeatureList( List<OntologyTerm> ontologyTerms);


    default boolean hasObservedFeatures( List<OntologyTerm> ontologyTerms) {
        return ontologyTerms.stream().anyMatch(Predicate.not(OntologyTerm::isExcluded));
    }

    default boolean hasExcludedFeatures( List<OntologyTerm> ontologyTerms) {
        return ontologyTerms.stream().anyMatch(OntologyTerm::isExcluded);
    }

    default String getOxfordCommaList(List<String> items, String andWord) {
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            String andWithSpace = String.format(" %s ", andWord);
            return String.join(andWithSpace, items) + ".";
        }
        StringBuilder sb = new StringBuilder();
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            String andWithSpaceAndComma = String.format(", %s ", andWord);
            symList = symList.substring(0, jj) + andWithSpaceAndComma + symList.substring(jj+2);
        }
        sb.append(symList);
        return sb.toString();
    }


}
