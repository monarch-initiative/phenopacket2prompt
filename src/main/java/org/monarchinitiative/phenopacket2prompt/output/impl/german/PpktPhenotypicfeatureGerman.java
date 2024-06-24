package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class PpktPhenotypicfeatureGerman implements PpktPhenotypicFeatureGenerator {

    private final HpInternational german;
    private Set<String> missingTranslations;


    public PpktPhenotypicfeatureGerman(HpInternational international) {
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
            return String.join(" und ", items);
        }
        // if we have more than two, join all but the very last item with a comma
        String penultimate = items.stream()
                .limit(items.size() - 1)
                .collect(Collectors.joining(","));
        String ultimate = items.get(items.size() - 1);
        return penultimate + " und " + ultimate;
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observedTerms = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excludedTerms = getExcludedFeatures(ontologyTerms);
        List<String> observedLabels = getTranslations(observedTerms);
        List<String> excludedLabels = getTranslations(excludedTerms);
        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            return "keine phänotypischen Abnormalitäten"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("%s wurden ausgeschlossen.", getCommaList(excludedLabels));
            } else {
                return String.format("%s wurde ausgeschlossen.",excludedLabels.getFirst());
            }
        } else {
            String exclusion = String.format("Dagegen %s %s ausgeschlossen.", excludedLabels.size()>1? "wurden":"wurde", getCommaList(excludedLabels));
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
            return String.format("%s präsentierte %s mit den folgenden Symptomen: %s. Im Gegensatz %s die folgenden Symptome ausgeschlossen: %s.",
                    ageString,
                    personString,
                    observedStr,
                    excluded.size()>1? "wurden":"wurde",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s präsentierte %s mit den folgenden Symptomen: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s %s die folgenden Symptome ausgeschlossen: %s.",
                    ageString,
                    excluded.size()>1? "wurden":"wurde", excludedStr);
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
            return String.format("%s präsentierte mit den folgenden Symptomen: %s. Im Gegensatz %s die folgenden Symptome ausgeschlossen: %s.",
                    personString,
                    observedStr,
                    excluded.size()>1? "wurden":"wurde",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s präsentierte mit den folgenden Symptomen: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("Beim Krankheitsbeginn %s die folgenden Symptome ausgeschlossen: %s.",
                    excluded.size()>1? "wurden":"wurde", excludedStr);
        } else {
            return "Keine phänotypischen Abnormalitäten wurden explizit zu Krankheitsbeginn beschrieben.";
        }
    }



}
