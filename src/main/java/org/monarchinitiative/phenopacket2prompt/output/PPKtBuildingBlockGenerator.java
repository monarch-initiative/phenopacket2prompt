package org.monarchinitiative.phenopacket2prompt.output;


/**
 * Provide the "building blocks" (i.e., text fragments) needed to generate the
 * texts in the various languages.
 */
public interface PPKtBuildingBlockGenerator {

    // days, months, years -- format singular and plural forms
    String days(int d);
    String months(int m);
    String years(int y);


    // Ages
    String yearsOld(int y);
    String monthsOld(int m);
    String daysOld(int d);
    String montDayOld(int m, int d);
    String yearsMonthsDaysOld(int y, int m, int d);

    // Phrases
    String asNewborn();
    String atTheAgeOf();


    // HPO Terms
    String inFetalPeriod();
    String isCongenital();
    String asInfant();
    String inChildhood();
    String asAdolescent();
    String inAdulthoold();

    // sexxage
    String woman();
    String man();
    String individual();
    String girl();
    String boy();
    String child();

}
