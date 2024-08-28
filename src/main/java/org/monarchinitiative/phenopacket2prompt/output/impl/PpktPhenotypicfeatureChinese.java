package org.monarchinitiative.phenopacket2prompt.output.impl.chinese;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class PpktPhenotypicfeatureChinese implements PpktPhenotypicFeatureGenerator {

    private final HpInternational chinese;
    private Set<String> missingTranslations;


    public PpktPhenotypicfeatureChinese(HpInternational international) {
        german = international;
        missingTranslations = new HashSet<>();
    }


    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = german.getLabel(term.getTid());
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
            return String.join("和", items);
        }
        // if we have more than two, join all but the very last item with a comma
        String penultimate = items.stream()
                .limit(items.size() - 1)
                .collect(Collectors.joining(","));
        String ultimate = items.get(items.size() - 1);
        return penultimate + "和" + ultimate;
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observedTerms = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excludedTerms = getExcludedFeatures(ontologyTerms);
        List<String> observedLabels = getTranslations(observedTerms);
        List<String> excludedLabels = getTranslations(excludedTerms);
        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            return "无异常"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("%s被排除在外。", getCommaList(excludedLabels));
            } else {
                return String.format("%s被排除在外。",excludedLabels.getFirst());
            }
        } else {
            String exclusion = String.format("%s被排除在外。",  getCommaList(excludedLabels));
            return getCommaList(observedLabels) + "。 " +  exclusion;  // check this
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
            return String.format("%s %s出现以下症状: %s. 以下症状被排除: %s.",
                    ageString,
                    personString,
                    observedStr,
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s %s 出现以下症状: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s 排除以下症状: %s.",
                    ageString, excludedStr);
        } else {
            throw new PhenolRuntimeException("No features found for time point " + ageString); // should never happen
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
            return String.format("%s 出现了以下症状: %s. 以下症状被排除: %s.",
                    personString,
                    observedStr,
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s 出现以下症状: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("疾病发作时，排除以下症状: %s.",
                    excludedStr);
        } else {
            return "疾病发作时无明确描述症状.";
        }
    }



}