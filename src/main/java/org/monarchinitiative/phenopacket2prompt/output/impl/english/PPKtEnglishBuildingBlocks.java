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
        return String.format("%d-%s old", y, years(y));
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d-%s old", m, months(m));
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
    public String inFetalPeriod() {
        return "in the fetal period";
    }

    @Override
    public String isCongenital() {
        return  "at birth";
    }

    @Override
    public String asInfant() {
        return "as an infant";
    }

    @Override
    public String inChildhood() {
        return "in childhood";
    }

    @Override
    public String asAdolescent() {
        return "as an adolescent";
    }

    @Override
    public String inAdulthoold() {
        return "in adulthood";
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
    public String whoPresentedWith() {
        return "who presented with";
    }

    @Override
    public String probandNoAgePresentedWith() {
        return "The proband presented with";
    }

    @Override
    public String probandMaleNoAgePresentedWith() {
        return "The proband was a male who presented with";
    }

    @Override
    public String probandFemaleNoAgeePresentedWith() {
        return "The proband was a female who presented with";
    }


}
