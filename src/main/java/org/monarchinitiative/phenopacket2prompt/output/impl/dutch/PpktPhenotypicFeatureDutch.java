package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class PpktPhenotypicFeatureDutch implements PpktPhenotypicFeatureGenerator {

    private final HpInternational dutch;
    private Set<String> missingTranslations;


    public PpktPhenotypicFeatureDutch(HpInternational international) {
        dutch = international;
        missingTranslations = new HashSet<>();
    }


    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = dutch.getLabel(term.getTid());
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
            return String.join(" en ", items);
        }
        // if we have more than two, join all but the very last item with a comma
        String penultimate = items.stream()
                .limit(items.size() - 1)
                .collect(Collectors.joining(", "));
        String ultimate = items.get(items.size() - 1);
        return penultimate + " en " + ultimate;
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observedTerms = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excludedTerms = getExcludedFeatures(ontologyTerms);
        List<String> observedLabels = getTranslations(observedTerms);
        List<String> excludedLabels = getTranslations(excludedTerms);
        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            return "zonder fenotypische abnormaliteiten"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("%s waren uitgesloten.", getCommaList(excludedLabels));
            } else {
                return String.format("%s was uitgesloten.",excludedLabels.getFirst());
            }
        } else {
            String exclusion = String.format("%s %s echter uitgesloten.", getCommaList(excludedLabels), excludedLabels.size()>1? "waren":"was");
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
        List<String> observeddutch = getTranslations(observed);
        List<String> excludeddutch = getTranslations(excluded);
        var observedStr = getCommaList(observeddutch);
        var excludedStr = getCommaList(excludeddutch);
        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s presenteerde %s met de volgende symptomen: %s. In tegenstelling daartegen %s uitgesloten: %s.",
                    ageString,
                    personString,
                    observedStr,
                    excluded.size()>1? "waren de volgende symptomen":"was het volgende symptoom",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s presenteerde %s met de volgende symptomen: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s %s de volgende symptomen uitgesloten: %s.",
                    ageString,
                    excluded.size()>1? "waren":"was", excludedStr);
        } else {
            throw new PhenolRuntimeException("No features found for time point " + ageString); // should never happen
        }
    }

    @Override
    public String featuresAtOnset(String personString, List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observed = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excluded = getExcludedFeatures(ontologyTerms);
        List<String> observeddutch = getTranslations(observed);
        List<String> excludeddutch = getTranslations(excluded);
        var observedStr = getCommaList(observeddutch);
        var excludedStr = getCommaList(excludeddutch);

        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s presenteerde met de volgende symptomen: %s. In tegenstelling daartegen %s uitgesloten: %s.",
                    personString,
                    observedStr,
                    excluded.size()>1? "waren de volgende symptomen":"was het volgende symptoom",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s presenteerde met %s: %s.", personString,
                    observed.size()>1? "de volgende symptomen":"het volgende symptoom", observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("Bij het begin van de ziekte %s uitgesloten: %s.",
                    excluded.size()>1? "waren de volgende symptomen":"was het volgende symptoom", excludedStr);
        } else {
            return "Geen fenotypische abnormaliteiten waren bij het begin van de ziekte vastgesteld.";
        }
    }



}
