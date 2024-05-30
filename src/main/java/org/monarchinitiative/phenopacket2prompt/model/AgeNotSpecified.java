package org.monarchinitiative.phenopacket2prompt.model;

import java.util.Objects;

public final class AgeNotSpecified implements  PhenopacketAge {
    @Override
    public String age() {
        return "";
    }

    @Override
    public PhenopacketAgeType ageType() {
        return PhenopacketAgeType.NOT_SPECIFIED;
    }

    @Override
    public boolean isJuvenile() {
        return false;
    }

    @Override
    public boolean isChild() {
        return false;
    }

    @Override
    public boolean isInfant() {
        return false;
    }

    @Override
    public boolean isFetus() {
        return false;
    }

    @Override
    public boolean isCongenital() {
        return false;
    }

    @Override
    public int totalDays() {
        return 0;
    }

    @Override
    public boolean specified() {return  false; }

    @Override
    public int hashCode() {
        return Objects.hashCode(totalDays());
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof PhenopacketAge)) return false;
        PhenopacketAge iso = (PhenopacketAge) obj;
        return iso.totalDays() == totalDays();
    }


}
