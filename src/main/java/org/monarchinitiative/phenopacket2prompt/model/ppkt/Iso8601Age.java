package org.monarchinitiative.phenopacket2prompt.model.ppkt;

public class Iso8601Age implements PhenopacketAge {

    private final String iso8601;

    public Iso8601Age(String iso) {
        iso8601 = iso;
    }



    @Override
    public String age() {
        return iso8601;
    }

    @Override
    public PhenopacketAgeType ageType() {
        return PhenopacketAgeType.ISO8601_AGE_TYPE;
    }
}
