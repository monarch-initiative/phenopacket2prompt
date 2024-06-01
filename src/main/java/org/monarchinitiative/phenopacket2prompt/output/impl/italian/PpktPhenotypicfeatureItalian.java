package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.function.Predicate;

public class PpktPhenotypicfeatureItalian implements PpktPhenotypicFeatureGenerator {

    private final HpInternational italian;

    private Set<String> missingTranslations;



    public PpktPhenotypicfeatureItalian(HpInternational international) {
        italian = international;
        missingTranslations = new HashSet<>();
    }



    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = italian.getLabel(term.getTid());
            if (opt.isPresent()) {
                labels.add(opt.get());
            } else {
                String missing = String.format(" %s (%s)", term.getLabel(), term.getTid().getValue());
                missingTranslations.add(missing);
            }
        }
        return labels;
    }


    private final Set<Character> vowels = Set.of('A', 'E', 'I', 'O', 'U');

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
                symList = symList.substring(0, jj) + " e " + end;
            } else {
                symList = symList.substring(0, jj) + " e " + end;
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
            return "nessuna anomalia fenotipica"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getOxfordCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("E' stata esclusa la presenza dei seguenti sintomi: %s.", getOxfordCommaList(excludedLabels));
            } else {
                return String.format("E' stata esclusa la presenza del seguente sintomo: %s.",excludedLabels.get(0));
            }
        } else {
            String exclusion;
            if (excludedLabels.size() == 1) {
                exclusion = String.format(" ed è stata esclusa la presenza di %s.", getOxfordCommaList(excludedLabels));
            } else {
                exclusion =  String.format(" ed è stata esclusa la presenza di %s.", getOxfordCommaList(excludedLabels));
            }
            return getOxfordCommaList(observedLabels) +  exclusion;
        }
    }
    public Set<String> getMissingTranslations() {
        return missingTranslations;
    }
}
