package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.function.Predicate;

public class PpktPhenotypicfeatureSpanish implements PpktPhenotypicFeatureGenerator {

    private final HpInternational spanish;

    public PpktPhenotypicfeatureSpanish(HpInternational international) {
        spanish = international;
        missingTranslations = new HashSet<>();
    }

    private Set<String> missingTranslations;

    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = spanish.getLabel(term.getTid());
            if (opt.isPresent()) {
                labels.add(opt.get());
            } else {
                String missing = String.format(" %s (%s)", term.getLabel(), term.getTid().getValue());
                missingTranslations.add(missing);
            }
        }
        return labels;
    }


    private final Set<Character> vowels = Set.of('A', 'E', 'I', 'O', 'U', 'Y');

    String getConnector(String nextWord) {
        if (nextWord.length() < 2) {
            return "y"; // should never happen but do not want to crash
        }
        char letter = nextWord.charAt(0);
        if (vowels.contains(letter)) {
            return " i ";
        }
        Character letter2 = nextWord.charAt(1);
        if (letter == 'H' && vowels.contains(letter2)) {
            return " i ";
        }
        return " y ";

    }


    private String getOxfordCommaList(List<String> items) {
        if (items.size() == 1) {
            return items.getFirst();
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            String connector = getConnector(items.get(1));
            return String.join(connector, items);
        }
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            String end = symList.substring(jj+2);
            String connector = getConnector(end);
            symList = symList.substring(0, jj) + connector + end;
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
            throw new PhenolRuntimeException("No phenotypic abnormalities"); // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getOxfordCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("se descartaron %s.", getOxfordCommaList(excludedLabels));
            } else {
                return String.format("se descartó %s.",excludedLabels.getFirst());
            }
        } else {
            String exclusion;
            if (excludedLabels.size() == 1) {
                exclusion = String.format(". En cambio, se descartó %s.", getOxfordCommaList(excludedLabels));
            } else {
                exclusion =  String.format(". En cambio, se descartaron %s.", getOxfordCommaList(excludedLabels));
            }
            return getOxfordCommaList(observedLabels) +  exclusion;
        }
    }

    public Set<String> getMissingTranslations() {
        return missingTranslations;
    }

}
