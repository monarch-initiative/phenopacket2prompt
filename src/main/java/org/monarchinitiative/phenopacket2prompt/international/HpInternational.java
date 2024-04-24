package org.monarchinitiative.phenopacket2prompt.international;

import org.monarchinitiative.phenol.ontology.data.TermId;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class HpInternational {

    private final String languageAcronym;

    private final Map<TermId, String> termIdToLabelMap;

    public HpInternational(String language) {
        languageAcronym = language;
        termIdToLabelMap = new HashMap<>();
    }

    public void addTerm(TermId tid, String label) {
        this.termIdToLabelMap.put(tid, label);
    }

    public String getLanguageAcronym() {
        return languageAcronym;
    }

    public Map<TermId, String> getTermIdToLabelMap() {
        return termIdToLabelMap;
    }

    public Optional<String> getLabel(TermId hpoId) {
        if (termIdToLabelMap.containsKey(hpoId)) {
            return Optional.of(termIdToLabelMap.get(hpoId));
        } else {
            return Optional.empty();
        }
    }

}
