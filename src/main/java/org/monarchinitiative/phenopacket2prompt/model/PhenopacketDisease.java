package org.monarchinitiative.phenopacket2prompt.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class PhenopacketDisease {

    private final TermId diseaseId;
    private final String label;

    public TermId getDiseaseId() {
        return diseaseId;
    }

    public String getLabel() {
        return label;
    }

    public PhenopacketDisease(String diseaseId, String label) {
        this.diseaseId = TermId.of(diseaseId);
        this.label = label;
    }
}
