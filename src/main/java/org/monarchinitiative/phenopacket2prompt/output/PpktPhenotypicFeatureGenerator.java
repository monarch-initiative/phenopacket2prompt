package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;

import java.util.List;

public interface PpktPhenotypicFeatureGenerator {

   // Please let's use the two functions below instead!
    //@Deprecated(forRemoval = true)
    String formatFeatures( List<OntologyTerm> ontologyTerms);

    default String formatObservedFeatures(List<OntologyTerm> oterms) {
        return ""; // TODO implement in each implementing class, using defualt for now so as not to break code
    }

    default String formatExcludedFeatures(List<OntologyTerm> oterms) {
        return ""; // TODO implement in each implementing class, using defualt for now so as not to break code
    }



}
