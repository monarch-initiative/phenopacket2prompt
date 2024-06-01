package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.*;

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
        if (items.size() == 1) {
            return items.getFirst();
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            return String.join(" und ", items);
        }
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        String end = symList.substring(jj+2);
        symList = symList.substring(0, jj) + " und " + end;
        return symList;
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
}
