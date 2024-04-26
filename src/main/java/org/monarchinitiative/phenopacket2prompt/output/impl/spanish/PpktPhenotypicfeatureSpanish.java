package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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


    private final Set<Character> vowels = Set.of('A', 'E', 'I', 'O', 'U', 'Y');

    private String getOxfordCommaList(List<String> items) {
        if (items.size() == 1) {
            return items.get(0);
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            return String.join(" and ", items);
        }
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            String end = symList.substring(jj+2);
            if (vowels.contains(end.charAt(0))) {
                symList = symList.substring(0, jj) + " i " + end;
            } else {
                symList = symList.substring(0, jj) + " y " + end;
            }
        }
        return symList;
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observedTerms = ontologyTerms.stream()
                .filter(Predicate.not(OntologyTerm::isExcluded))
                .toList();
        List<String> observedLabels = getTranslations(observedTerms);
        List<OntologyTerm> excludedTerms = ontologyTerms.stream()
                .filter(OntologyTerm::isExcluded).toList();
        List<String> excludedLabels = getTranslations(excludedTerms);
        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            return "no phenotypic abnormalities"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getOxfordCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("por lo que se excluyeron %s.", getOxfordCommaList(excludedLabels));
            } else {
                return String.format("por lo que %s fue excluido.",excludedLabels.get(0));
            }
        } else {
            String exclusion;
            if (excludedLabels.size() == 1) {
                exclusion = String.format(" y se excluyó %s.", getOxfordCommaList(excludedLabels));
            } else {
                exclusion =  String.format(" y se excluyeron %s.", getOxfordCommaList(excludedLabels));
            }
            return getOxfordCommaList(observedLabels) +  exclusion;
        }
    }
}
