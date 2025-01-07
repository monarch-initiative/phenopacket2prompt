package org.monarchinitiative.phenopacket2prompt.output.impl.japanese;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class PpktPhenotypicfeatureJapanese implements PpktPhenotypicFeatureGenerator {

    private final HpInternational japanese;
    private Set<String> missingTranslations;


    public PpktPhenotypicfeatureJapanese(HpInternational international) {
        if (international == null) { // should never happen
            throw new PhenolRuntimeException("Null international object for Japanese");
        }
        japanese = international;
        missingTranslations = new HashSet<>();
    }


    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = japanese.getLabel(term.getTid());
            if (opt.isPresent()) {
                labels.add(opt.get());
            } else {
                String missing = String.format(" %s (%s)", term.getLabel(), term.getTid().getValue());
                missingTranslations.add(missing);
            }
        }
        return labels;
    }



    private String getCommaList(List<String> items) {
        if (items.isEmpty()) {
            return ""; // this will be filtered out later
        }
        if (items.size() == 1) {
            return items.getFirst();
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            return String.join(" アンド ", items);
        }
        // if we have more than two, join all but the very last item with a comma
        String penultimate = items.stream()
                .limit(items.size() - 1)
                .collect(Collectors.joining(", "));
        String ultimate = items.get(items.size() - 1);
        return penultimate + " アンド " + ultimate;
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observedTerms = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excludedTerms = getExcludedFeatures(ontologyTerms);
        List<String> observedLabels = getTranslations(observedTerms);
        List<String> excludedLabels = getTranslations(excludedTerms);
        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            return "表現型異常なし"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            return String.format("%s 除外された.", getCommaList(excludedLabels));
        } else {
            String exclusion = String.format("一方、以下を除外した。: %s.", getCommaList(excludedLabels));
            return getCommaList(observedLabels) + ". " +  exclusion;
        }
    }

    public Set<String> getMissingTranslations() {
        return missingTranslations;
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
            return String.format("%s 発表 %s 以下の症状を伴う: %s. 一方、以下の症状は除外された。: %s.",
                    ageString,
                    personString,
                    observedStr,
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s 発表 %s 以下の症状を伴う: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s 一方、以下の症状は除外された。: %s.",
                    ageString, excludedStr);
        } else {
            throw new PhenolRuntimeException("その時点の特徴は見つからない " + ageString); // should never happen
        }
    }

    @Override
    public String featuresAtOnset(String personString, List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observed = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excluded = getExcludedFeatures(ontologyTerms);
        List<String> observedGerman = getTranslations(observed);
        List<String> excludedGerman = getTranslations(excluded);
        var observedStr = getCommaList(observedGerman);
        var excludedStr = getCommaList(excludedGerman);

        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s 以下の症状を呈した。: %s. 一方、以下の症状は除外された。: %s.",
                    personString,
                    observedStr,
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s 以下の症状を呈した。: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("発症時に以下の症状は除外された。: %s.",
                    excludedStr);
        } else {
            return "発症時の表現型異常は明示されていない.";
        }
    }



}
