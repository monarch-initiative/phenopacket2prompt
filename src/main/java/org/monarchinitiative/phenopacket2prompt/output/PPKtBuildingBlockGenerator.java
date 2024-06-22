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

    String monthDayOld(int m, int d);

    String yearsMonthsDaysOld(int y, int m, int d);

    // Phrases
    String asNewborn();

    String atTheAgeOf();


    // sexxage
    String she();

    String he();

    String theProband();

    String woman();

    String man();

    String individual();

    String theIndividual();

    String girl();

    String boy();

    String child();

    String adolescentGirl();

    String adolescentBoy();

    String adolescentChild();

    String maleInfant();

    String femaleInfant();

    String infant();

    String newbornBoy();

    String newbornGirl();

    String newborn();

    String maleFetus();

    String femaleFetus();

    String fetus();

    String female();

    String male();

    String adult();

    // general
    String probandWasA();

    String whoPresented();

    String presented();

    String probandNoAgePresented();

    String probandNoAgePresentedWith();

    String probandWasAMale();

    String probandWasAFemale();

    String probandWasAnIndividual();

    String presentedWith();

    String with();


    String inWhomManifestationsWereExcluded();

    // HPO Terms for onset and vignetttes
    String duringFetal();

    String asNeonate();

    String atBirth();

    String asInfant();

    String inChildhood();

    String asAdolescent();

    String asAdult();

    String asYoungAdult();

    String asMiddleAge();

    String asLateOnset();
}
