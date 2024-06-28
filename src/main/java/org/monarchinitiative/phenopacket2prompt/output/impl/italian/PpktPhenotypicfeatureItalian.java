package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PpktPhenotypicfeatureItalian implements PpktPhenotypicFeatureGenerator {

    private final HpInternational italian;

    public PpktPhenotypicfeatureItalian(HpInternational international) {
        italian = international;
        missingTranslations = new HashSet<>();
    }

    private Set<String> missingTranslations;

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

    String getConnector(String nextWord) {
        if (nextWord.length() < 2) {
            return " e "; // should never happen but do not want to crash
        }
        char letter = nextWord.charAt(0);
        if (vowels.contains(letter)) {
            return " ed ";
        }

        return " e ";

    }

    private String getCommaList(List<String> items) {
        if (items.isEmpty()) return ""; // will be filtered out
        if (items.size() == 1) {
            return items.getFirst();
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            String connector = getConnector(items.get(1));
            return String.join(connector, items);
        }
        // if we have more than two, join all but the very last item with a comma
        String penultimate = items.stream()
                .limit(items.size() - 1)
                .collect(Collectors.joining(","));
        String ultimate = items.get(items.size() - 1);
        return penultimate + getConnector(ultimate) + ultimate;
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
            return "Nessuna anomalia fenotipica"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("si escluse la presenza di %s.", getCommaList(excludedLabels));
            } else {
                return String.format("si escluse la presenza di %s.",excludedLabels.getFirst());
            }
        } else {
            String exclusion;
            if (excludedLabels.size() == 1) {
                exclusion = String.format(". Al contrario, si escluse la presenza di %s.", getCommaList(excludedLabels));
            } else {
                exclusion =  String.format(". Al contrario, si escluse la presenza di %s.", getCommaList(excludedLabels));
            }
            return getCommaList(observedLabels) +  exclusion;
        }
    }
    public Set<String> getMissingTranslations() {
        return missingTranslations;
    }

    @Override
    public String featuresAtOnset(String personString, List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observed = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excluded = getExcludedFeatures(ontologyTerms);
        List<String> observedSpanish = getTranslations(observed);
        List<String> excludedSpanish = getTranslations(excluded);
        var observedStr = getCommaList(observedSpanish);
        var excludedStr = getCommaList(excludedSpanish);
        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s presentò i sequenti sintomi: %s. Al contrario, si %s: %s.",
                    personString,
                    observedStr,
                    excluded.size()>1? "esclusero i sequenti sintomi":"escluse il sequente sintomo",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s presentò i sequenti sintomi: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("All'inizio della malattia, si %s: %s.",
                    excluded.size()>1? "esclusero i seguenti sintomi":"escluse il seguente sintomo", excludedStr);
        } else {
            return "Non vennero descritte esplicitamente anomalie fenotipiche al principio de della malattia";
        }
    }
    @Override
    public String featuresAtEncounter(String personString, String ageString, List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observed = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excluded = getExcludedFeatures(ontologyTerms);
        List<String> observedGerman = getTranslations(observed);
        List<String> excludedGerman = getTranslations(excluded);
        var observedStr = getCommaList(observedGerman);
        var excludedStr = getCommaList(excludedGerman);
        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s presentava %s i seguenti sintomi: %s. Al contrario, si %s i seguenti sintomi: %s.",
                    personString,
                    ageString,
                    observedStr,
                    excluded.size()>1? "esclusero":"escluse",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s presentava %s i seguenti sintomi: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s si %s i seguenti sintomi: %s.",
                    ageString,
                    excluded.size()>1? "esclusero":"escluse", excludedStr);
        } else {
            throw new PhenolRuntimeException("No features found for time point " + ageString); // should never happen
        }
    }
}
