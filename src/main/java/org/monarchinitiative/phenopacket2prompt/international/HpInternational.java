package org.monarchinitiative.phenopacket2prompt.international;

import org.monarchinitiative.phenol.ontology.data.TermId;
import java.util.Map;
import java.util.HashMap;

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
}
