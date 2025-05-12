package org.monarchinitiative.phenopacket2prompt.output.impl.french;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PpktPhenotypicfeatureFrench implements PpktPhenotypicFeatureGenerator {

    private final HpInternational spanish;

    public PpktPhenotypicfeatureFrench(HpInternational international) {
        spanish = international;
        missingTranslations = new HashSet<>();
    }

    private final Set<String> missingTranslations;

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
       return  " et ";

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
                .collect(Collectors.joining(", "));
        String ultimate = items.getLast();
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
        if(isFullTranslationsEnabled()) {
            if (observedTerms.size() != observedLabels.size() ||
                    excludedTerms.size() != excludedLabels.size()) {
                throw new PhenolRuntimeException("Missing translation, function formatFeatures().");
            }
        }

        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            throw new PhenolRuntimeException("No phenotypic abnormalities"); // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ".";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("ont été écartées %s.", getCommaList(excludedLabels));
            } else {
                return String.format("ont été écartées %s.",excludedLabels.getFirst());
            }
        } else {
            String exclusion;
            if (excludedLabels.size() == 1) {
                exclusion = String.format(". Au lieu de cela, il a été exclu %s.", getCommaList(excludedLabels));
            } else {
                exclusion =  String.format(". En revanche, les éléments suivants ont été écartés %s.", getCommaList(excludedLabels));
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
        if(isFullTranslationsEnabled()) {
            if (observed.size() != observedSpanish.size() ||
                    excluded.size() != excludedSpanish.size()) {
                throw new PhenolRuntimeException("Missing translation, function featuresAtOnset().");
            }
        }

        var observedStr = getCommaList(observedSpanish);
        var excludedStr = getCommaList(excludedSpanish);
        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s présentait les symptômes suivants: %s. Au contraire, %s: %s.",
                    personString,
                    observedStr,
                    excluded.size()>1? "les symptômes suivants étaient exclus :":"le symptôme suivant a été exclu :",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s a présenté les symptômes suivants: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("Au début de la maladie, se %s: %s.",
                    excluded.size()>1? "les symptômes suivants étaient exclus :":"le symptôme suivant a été exclu :", excludedStr);
        } else {
            return "Aucune anomalie phénotypique n'a été explicitement décrite à l'apparition de la maladie.";
        }
    }
    @Override
    public String featuresAtEncounter(String personString, String ageString, List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observed = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excluded = getExcludedFeatures(ontologyTerms);
        List<String> observedSpanish = getTranslations(observed);
        List<String> excludedSpanish = getTranslations(excluded);
        if(isFullTranslationsEnabled()) {
            if (observed.size() != observedSpanish.size() ||
                    excluded.size() != excludedSpanish.size()) {
                throw new PhenolRuntimeException("Missing translation, function featuresAtEncounter().");
            }
        }
        var observedStr = getCommaList(observedSpanish);
        var excludedStr = getCommaList(excludedSpanish);
        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s présentait %s les symptômes suivants : %s. D'autre part, se %s los siguientes síntomas: %s.",
                    personString,
                    ageString,
                    observedStr,
                    excluded.size()>1? "excluait":"excluait",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s présentait %s les symptômes suivants: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s se %s les symptômes suivants: %s.",
                    ageString,
                    excluded.size()>1? "excluait":"excluait", excludedStr);
        } else {
            throw new PhenolRuntimeException("No features found for time point " + ageString); // should never happen
        }
    }
}
