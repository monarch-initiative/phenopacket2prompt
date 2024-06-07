package org.monarchinitiative.phenopacket2prompt.model;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.util.Objects;

public final class Iso8601Age implements PhenopacketAge {

    private final String iso8601;

    private final int years;
    private final int months;
    private final int days;

    private final int totalDays;

    public Iso8601Age(String iso) {
        iso8601 = iso;
        String ageString = iso;
        if (! ageString.startsWith("P")) {
            throw new PhenolRuntimeException("Malformed iso8601 age \"" + iso + "\"");
        }
        ageString = ageString.substring(1);
        int i = ageString.indexOf("Y");
        if (i>0) {
            years = Integer.parseInt(ageString.substring(0,i));
            ageString = ageString.substring(i+1);
        } else {
            years = 0;
        }
        i = ageString.indexOf("M");
        if (i>0) {
            months = Integer.parseInt(ageString.substring(0,i));
            ageString = ageString.substring(i+1);
        } else {
            months = 0;
        }
        i = ageString.indexOf("D");
        if (i>0) {
            days = Integer.parseInt(ageString.substring(0,i));
        } else {
            days = 0;
        }
        totalDays = (int) ( days + 30.437*months + 365.25*years);
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    @Override
    public String age() {
        StringBuilder sb = new StringBuilder();
        if (years == 1) {
            return "one year";
        } else if (years > 1) {
            return String.format("%d years", years);
        } else if (months > 0) {
            return String.format("%d months", months);
        } else {
            return String.format("%d days", days);
        }
    }

    @Override
    public PhenopacketAgeType ageType() {
        return PhenopacketAgeType.ISO8601_AGE_TYPE;
    }


    @Override
    public boolean isJuvenile() {
        return years >= 6 && years < 18;
    }

    @Override
    public boolean isChild() {
        return years >= 1 && years < 6;
    }

    @Override
    public boolean isInfant() {
        return years < 1;
    }

    @Override
    public boolean isFetus() {
        // always false because we cannot express prenatal ages with iso
        return false;
    }

    @Override
    public boolean isCongenital() {
       // rarely. Usually we use Hpo Onset for congenital
        return years == 0 && months == 0 && days == 0;
    }

    @Override
    public int totalDays() {
        return totalDays;
    }


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
