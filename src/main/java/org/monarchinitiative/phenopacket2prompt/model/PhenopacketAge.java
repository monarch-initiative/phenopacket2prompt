package org.monarchinitiative.phenopacket2prompt.model;

public interface PhenopacketAge {

    String age();
    PhenopacketAgeType ageType();

    boolean isJuvenile();

    boolean isChild();

    boolean isInfant();

    boolean isFetus();

    boolean isCongenital();

    int totalDays();

    default boolean specified() {return true; }
}
