package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PpktPhenotypicfeatureSpanish implements PpktPhenotypicFeatureGenerator {

    private final HpInternational spanish;

    public PpktPhenotypicfeatureSpanish(HpInternational international) {
        spanish = international;
    }



    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = spanish.getLabel(term.getTid());
            if (opt.isPresent()) {
                labels.add(opt.get());
            } else {
                System.err.printf("[ERROR] Could not find %s translation for %s (%s).\n", spanish.getLanguageAcronym(), term.getLabel(), term.getTid().getValue());
            }
        }
        return labels;
    }



    public String featureList(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> terms = ontologyTerms.stream()
                .filter(Predicate.not(OntologyTerm::isExcluded)).toList();
        List<String> labels = getTranslations(terms);
        return ""; //;//getOxfordCommaList(labels, "y");
    }


    public String excludedFeatureList(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> terms = ontologyTerms.stream()
                .filter(OntologyTerm::isExcluded).toList();
        List<String> labels = getTranslations(terms);
        return ""; //;//getOxfordCommaList(labels, "y");
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        return "";
    }
}
