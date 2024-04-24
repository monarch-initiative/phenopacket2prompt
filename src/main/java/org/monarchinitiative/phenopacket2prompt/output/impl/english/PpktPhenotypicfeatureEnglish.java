package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.List;
import java.util.function.Predicate;

public class PpktPhenotypicfeatureEnglish implements PpktPhenotypicFeatureGenerator  {
    @Override
    public String featureList(List<OntologyTerm> ontologyTerms) {
        List<String> labels = ontologyTerms.stream()
                .filter(Predicate.not(OntologyTerm::isExcluded))
                .map(OntologyTerm::getLabel).toList();
        return getOxfordCommaList(labels, "and");
    }

    @Override
    public String excludedFeatureList(List<OntologyTerm> ontologyTerms) {
        List<String> labels = ontologyTerms.stream()
                .filter(OntologyTerm::isExcluded)
                .map(OntologyTerm::getLabel).toList();
        return getOxfordCommaList(labels, "and");
    }
}
