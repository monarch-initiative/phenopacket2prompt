package org.monarchinitiative.phenopacket2prompt.output.impl.turkish;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class PpktPhenotypicfeatureTurkish implements PpktPhenotypicFeatureGenerator {

    private final HpInternational turkish;
    private Set<String> missingTranslations;


    public PpktPhenotypicfeatureTurkish(HpInternational international) {
        turkish = international;
        missingTranslations = new HashSet<>();
    }


    private List<String> getTranslations(List<OntologyTerm> ontologyTerms) {
        List<String> labels = new ArrayList<>();
        for (var term: ontologyTerms) {
            Optional<String> opt = turkish.getLabel(term.getTid());
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
            return String.join(" ve ", items);
        }
        // if we have more than two, join all but the very last item with a comma
        String penultimate = items.stream()
                .limit(items.size() - 1)
                .collect(Collectors.joining(", "));
        String ultimate = items.get(items.size() - 1);
        return penultimate + " ve " + ultimate;
    }

    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observedTerms = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excludedTerms = getExcludedFeatures(ontologyTerms);
        List<String> observedLabels = getTranslations(observedTerms);
        List<String> excludedLabels = getTranslations(excludedTerms);
        if (observedTerms.size() != observedLabels.size() ||
                excludedTerms.size() != excludedLabels.size() ) {
            throw new PhenolRuntimeException("Missing translation, function formatFeatures().");
        }
        if (observedLabels.isEmpty() && excludedLabels.isEmpty()) {
            return "fenotipik anormallik yok"; // should never happen, actually!
        } else if (excludedLabels.isEmpty()) {
            return getCommaList(observedLabels) + ". ";
        } else if (observedLabels.isEmpty()) {
            if (excludedLabels.size() > 1) {
                return String.format("%s dışlandı.", getCommaList(excludedLabels));
            } else {
                return String.format("%s dışlandı.",excludedLabels.getFirst());
            }
        } else {
            String exclusion = String.format("Buna karşın  %s %s dışlandı.", excludedLabels.size()>1? "wurden":"wurde", getCommaList(excludedLabels));
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
            return String.format("%s %s şu belirtilerle başvurdu: %s. Buna karşın %s dışlandı: %s.",
                    ageString,
                    personString,
                    observedStr,
                    excluded.size()>1? "şu belirtiler":"şu belirti",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s %s şu belirtilerle başvurdu: %s.", ageString, personString,  observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s %s dışlandı: %s.",
                    ageString,
                    excluded.size()>1? "şu belirtiler":"şu belirti", excludedStr);
        } else {
            throw new PhenolRuntimeException("No features found for time point " + ageString); // should never happen
        }
    }

    @Override
    public String featuresAtOnset(String personString, List<OntologyTerm> ontologyTerms) {
        List<OntologyTerm> observed = getObservedFeatures(ontologyTerms);
        List<OntologyTerm> excluded = getExcludedFeatures(ontologyTerms);
        List<String> observedTurkish = getTranslations(observed);
        List<String> excludedTurkish = getTranslations(excluded);

        if (observed.size() != observedTurkish.size() ||
                excluded.size() != excludedTurkish.size() ) {
            throw new PhenolRuntimeException("Missing translation, function featuresAtOnset().");
        }
        var observedStr = getCommaList(observedTurkish);
        var excludedStr = getCommaList(excludedTurkish);

        if (!observed.isEmpty() && !excluded.isEmpty()) {
            return String.format("%s şu belirtilerle ortaya çıktı: %s. Buna karşın %s dışlandı: %s.",
                    personString,
                    observedStr,
                    excluded.size() > 1 ? "şu belirtiler" : "şu belirti",
                    excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s şu belirtilerle ortaya çıktı: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("Hastalık başlangıcında %s dışlandı: %s.",
                    excluded.size() > 1 ? "şu belirtiler" : "şu belirti", excludedStr);
        } else {
            return "Hastalık başlangıcında açıkça belirtilmiş fenotipik anormallik yok.";
        }
    }
}



