package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.output.PPKtBuildingBlockGenerator;

public class PPKtEnglishBuildingBlocks implements PPKtBuildingBlockGenerator {


    @Override
    public String days(int d) {
        return d>1 ? "days" : "day";
    }

    @Override
    public String months(int m) {
        return m>1 ? "months" : "month";
    }

    @Override
    public String years(int y) {
        return y>1 ? "years" : "year";
    }

    @Override
    public String yearsOld(int y) {
        return String.format("%d-year-old", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d-month-old", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d-%s old", d, days(d));
    }

    @Override
    public String monthDayOld(int m, int d) {
        if (m==0) {
            return daysOld(d);
        } else if (d==0) {
            return monthsOld(m);
        }
        return String.format("%d-%s, %d-%s old", m, months(m), d, days(d));
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        if (y==0) {
            return monthDayOld(m,d);
        }
        if (d==0) {
            return String.format("%d-%s, %d-%s old", y, years(y), m, months(m));
        }
        return String.format("%d-%s, %d-%s, %d-%s old", y, years(y), m, months(m), d, days(d));
    }

    @Override
    public String asNewborn() {
        return "as a newborn";
    }

    @Override
    public String atTheAgeOf() {
        return "at the age of";
    }


    @Override
    public String she() {
        return "she";
    }

    @Override
    public String he() {
        return "he";
    }

    @Override
    public String theProband() {
        return "the proband";
    }

    @Override
    public String woman() {
        return "woman";
    }

    @Override
    public String man() {
        return "man";
    }

    @Override
    public String individual() {
        return "individual";
    }

    @Override
    public String theIndividual() {
        return "the individual";
    }

    @Override
    public String girl() {
        return "girl";
    }

    @Override
    public String boy() {
        return "boy";
    }

    @Override
    public String child() {
        return "child";
    }

    @Override
    public String adolescentGirl() {
        return "adolescent girl";
    }

    @Override
    public String adolescentBoy() {
        return "adolescent boy";
    }

    @Override
    public String adolescentChild() {
        return "adolescent child";
    }

    @Override
    public String maleInfant() {
        return "male infant";
    }

    @Override
    public String femaleInfant() {
        return "female infant";
    }

    @Override
    public String infant() {
        return "infant";
    }

    @Override
    public String newbornBoy() {
        return "newborn boy";
    }

    @Override
    public String newbornGirl() {
        return "newborn girl";
    }

    @Override
    public String newborn() {
        return "newborn";
    }

    @Override
    public String maleFetus() {
        return "male fetus";
    }

    @Override
    public String femaleFetus() {
        return "female fetus";
    }

    @Override
    public String fetus() {
        return "fetus";
    }

    @Override
    public String female() {
        return "female";
    }

    @Override
    public String male() {
        return "male";
    }

    @Override
    public String adult() {
        return "adult";
    }

    @Override
    public String probandWasA() {
        return "The proband was a";
    }

    @Override
    public String whoPresented() {
        return "who presented";
    }

    @Override
    public String presented() {
        return "presented";
    }

    @Override
    public String probandNoAgePresented() {
        return "The proband presented";
    }

    @Override
    public String probandNoAgePresentedWith() {
        return "The proband presented with";
    }



    @Override
    public String presentedWith() {
        return "presented with";
    }

    @Override
    public String with() {
        return "with";
    }


    @Override
    public String probandWasAFemale() {
        return "The proband was a female";
    }

    @Override
    public String probandWasAMale() {
        return "The proband was a male";
    }

    @Override
    public String probandWasAnIndividual() {
        return "The proband was an individual";
    }

    @Override
    public String inWhomManifestationsWereExcluded() {
        return "in whom the following clinical manifestations were excluded";
    }

    @Override
    public String duringFetal() {
        return "During the fetal period";
    }

    @Override
    public String asNeonate() {
        return "As a neonate";
    }

    @Override
    public String atBirth() {
        return  "At birth";
    }

    @Override
    public String asInfant() {
        return "As an infant";
    }

    @Override
    public String inChildhood() {
        return "As a child";
    }

    @Override
    public String asAdolescent() {
        return "As an adolescent";
    }

    @Override
    public String asAdult() {
        return "As an adult";
    }

    @Override
    public String asYoungAdult() {
        return "As a young adult";
    }

    @Override
    public String asMiddleAge() {
        return "During middle age";
    }

    @Override
    public String asLateOnset() {
        return "During old age";
    }
}
