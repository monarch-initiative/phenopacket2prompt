package org.monarchinitiative.phenopacket2prompt.model.ppkt;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class HpoOnsetAge implements PhenopacketAge {

    private final TermId tid;
    private final String label;


    public HpoOnsetAge(String id, String label) {
        this.tid = TermId.of(id);
        this.label = label;
    }

    @Override
    public String age() {
        return label;
    }

    @Override
    public PhenopacketAgeType ageType() {
        return PhenopacketAgeType.HPO_ONSET_AGE_TYPE;
    }
}
