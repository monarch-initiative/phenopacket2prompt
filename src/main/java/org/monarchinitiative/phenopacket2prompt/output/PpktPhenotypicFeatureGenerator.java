package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;

import java.util.List;

public interface PpktPhenotypicFeatureGenerator {


    String formatFeatures( List<OntologyTerm> ontologyTerms);


}
