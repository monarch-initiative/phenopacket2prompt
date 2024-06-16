package org.monarchinitiative.phenopacket2prompt.model;

public sealed interface PhenopacketAge permits AgeNotSpecified, HpoOnsetAge, Iso8601Age {

    String age();
    PhenopacketAgeType ageType();

    boolean isJuvenile();

    boolean isChild();

    boolean isInfant();

    boolean isFetus();

    boolean isCongenital();

    boolean isYoungAdult();

    boolean isMiddleAge();

    boolean isLateAdultAge();

    boolean isAdult();

    int totalDays();

    default boolean specified() {return true; }

}
