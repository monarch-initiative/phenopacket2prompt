package org.monarchinitiative.phenopacket2prompt.model;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoOnset;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Optional;
import java.util.Set;

public class HpoOnsetAge implements PhenopacketAge {

    private final TermId tid;
    private final String label;

    private final int totalDays;


    /** One of Antenatal onset HP:0030674; Fetal onset HP:0011461; Late first trimester onset HP:0034199;
     * Third trimester onset HP:0034197; Second trimester onset HP:0034198; Embryonal onset HP:0011460*/
    private final static  Set<TermId> fetalIds = Set.of(TermId.of(" HP:0030674"), TermId.of("HP:0011461"), TermId.of("HP:0034199"),
            TermId.of("HP:0034197"), TermId.of("HP:0034198"), TermId.of("HP:0011460*"));

    /** Childhood onset */
    private final static TermId childhoodOnset = TermId.of("HP:0011463");


    /** Infantile onset */
    private final static TermId infantileOnset = TermId.of("HP:0003593");

    /** Congenital onset */
    private final static TermId congenitalOnset = TermId.of("HP:0003577");



    public HpoOnsetAge(String id, String label) {
        this.tid = TermId.of(id);
        this.label = label;
        Optional<HpoOnset> opt = HpoOnset.fromTermId(tid);
        if (opt.isPresent()) {
            HpoOnset onset = opt.get();
           totalDays = (int) ( onset.start().days() /2+ onset.end().days()/2);
        } else {
            totalDays = Integer.MAX_VALUE;
        }

    }

    @Override
    public String age() {
        return label;
    }

    @Override
    public PhenopacketAgeType ageType() {
        return PhenopacketAgeType.HPO_ONSET_AGE_TYPE;
    }


    @Override
    public boolean isChild() {
        return tid.equals(childhoodOnset);
    }

    @Override
    public boolean isInfant() {
        return tid.equals(infantileOnset);
    }

    @Override
    public boolean isCongenital() {
        return tid.equals(congenitalOnset);
    }

    @Override
    public boolean isFetus() {
        return fetalIds.contains(tid);
    }


    @Override
    public int totalDays() {
        return totalDays;
    }
}
