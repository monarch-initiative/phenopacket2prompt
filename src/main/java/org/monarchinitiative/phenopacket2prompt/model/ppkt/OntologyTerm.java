package org.monarchinitiative.phenopacket2prompt.model.ppkt;

import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v2.core.Disease;

import java.util.Optional;

public class OntologyTerm {

    private final TermId tid;
    private final String label;
    private final boolean excluded;
    private final PhenopacketAge age;

    public OntologyTerm(TermId tid, String label, boolean excluded, PhenopacketAge age) {
        this.tid = tid;
        this.label = label;
        this.excluded = excluded;
        this.age = age;

    }

    public OntologyTerm(TermId tid, String label, boolean excluded) {
        this(tid, label, excluded, null);

    }
    public OntologyTerm(TermId tid, String label) {
        this(tid, label, false);
    }

    public TermId getTid() {
        return tid;
    }

    public String getLabel() {
        return label;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public Optional<PhenopacketAge> getAgeOpt() {
        return Optional.ofNullable(age);
    }

}
