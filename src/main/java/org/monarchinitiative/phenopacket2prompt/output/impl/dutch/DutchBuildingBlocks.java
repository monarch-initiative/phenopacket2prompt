package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class DutchBuildingBlocks implements BuildingBlockGenerator {


    @Override
    public String days(int d) {
        return d>1 ? "dagen" : "dag";
    }

    @Override
    public String months(int m) {
        return "maand";
    }

    @Override
    public String years(int y) {
        return "jaar";
    }



    @Override
    public String fromIso(Iso8601Age ppktAge) {
        List<String> components = new ArrayList<>();
        int y = ppktAge.getYears();
        int m = ppktAge.getMonths();
        int d = ppktAge.getDays();
        if (y > 0) {
            components.add(String.format("%d %s", y, years(y)));
        }
        if (m > 0) {
            components.add(String.format("%d %s", m, months(m)));
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, days(d)));
        }
        return String.join(" ", components);
    }



    @Override
    public String yearsOld(int y) {
        return String.format("%d jaar oud", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d maand oud", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d %s oud", d, days(d));
    }

    @Override
    public String monthDayOld(int m, int d) {
        if (m==0) {
            return daysOld(d);
        } else if (d==0) {
            return monthsOld(m);
        } else if (d==1){
            return String.format("%d maand en %d dag oud", m,  d);
        } else
        return String.format("%d maand en %d dagen oud", m,  d);
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        if (y==0) {
            return monthDayOld(m,d);
        } else if (d==0) {
            return String.format("%d jaar en %d maand oud", y,  m);
        } else if (d==1){
            return String.format("%d jaar, %d maand en %d dag oud", y, m,  d);
        } else
        return String.format("%d jaar, %d maand en %d dagen oud", y, m, d);
    }

    @Override
    public String asNewborn() {
        return "als pasgeborene";
    }

    @Override
    public String atTheAgeOf() {
        return "op de leeftijd van";
    }


    @Override
    public String she() {
        return "zij";
    }

    @Override
    public String he() {
        return "hij";
    }

    @Override
    public String theProband() {
        return "de proband";
    }

    @Override
    public String woman() {
        return "vrouw";
    }

    @Override
    public String man() {
        return "man";
    }

    @Override
    public String individual() {
        return "persoon";
    }

    @Override
    public String theIndividual() {
        return "de persoon";
    }

    @Override
    public String girl() {
        return "meisje";
    }

    @Override
    public String boy() {
        return "jongen";
    }

    @Override
    public String child() {
        return "kind";
    }

    @Override
    public String adolescentGirl() {
        return "adolescent meisje";
    }

    @Override
    public String adolescentBoy() {
        return "adolescente jongen";
    }

    @Override
    public String adolescentChild() {
        return "adolescent kind";
    }

    @Override
    public String maleInfant() {
        return "mannelijk kind";
    }

    @Override
    public String femaleInfant() {
        return "vrouwelijk kind";
    }

    @Override
    public String infant() {
        return "kind";
    }

    @Override
    public String newbornBoy() {
        return "pasgeboren jongetje";
    }

    @Override
    public String newbornGirl() {
        return "pasgeboren meisje";
    }

    @Override
    public String newborn() {
        return "pasgeborene";
    }

    @Override
    public String maleFetus() {
        return "mannelijke foetus";
    }

    @Override
    public String femaleFetus() {
        return "vrouwelijke foetus";
    }

    @Override
    public String fetus() {
        return "foetus";
    }

    @Override
    public String female() {
        return "vrouw";
    }

    @Override
    public String male() {
        return "man";
    }

    @Override
    public String adult() {
        return "volwassene";
    }

    @Override
    public String probandWasA() {
        return "De proband was een";
    }

    @Override
    public String whoPresented() {
        return "die presenteerde";
    }

    @Override
    public String presented() {
        return "presenteerde";
    }

    @Override
    public String probandNoAgePresented() {
        return "De proband presenteerde";
    }

    @Override
    public String probandNoAgePresentedWith() {
        return "De proband presenteerde met";
    }



    @Override
    public String presentedWith() {
        return "presenteerde met";
    }

    @Override
    public String with() {
        return "met";
    }


    @Override
    public String probandWasAFemale() {
        return "De proband was een vrouw";
    }

    @Override
    public String probandWasAMale() {
        return "De proband was een man";
    }

    @Override
    public String probandWasAnIndividual() {
        return "De proband was een persoon van onbepaald geslacht en leeftijd";
    }

    @Override
    public String inWhomManifestationsWereExcluded() {
        return "bij wie de volgende klinische manifestaties werden uitgesloten";
    }

    @Override
    public String duringFetal() {
        return "Tijdens de foetale periode";
    }

    @Override
    public String duringEmbryonic() {
        return "Tijdens de embryonale periode";
    }
    @Override
    public String asNeonate() {
        return "Als pasgeborene";
    }

    @Override
    public String atBirth() {
        return  "Bij de geboorte";
    }

    @Override
    public String asInfant() {
        return "Als baby";
    }

    @Override
    public String inChildhood() {
        return "Als kind";
    }

    @Override
    public String asAdolescent() {
        return "Als adolescent";
    }

    @Override
    public String asAdult() {
        return "Als volwassene";
    }

    @Override
    public String asYoungAdult() {
        return "Als jongvolwassene";
    }

    @Override
    public String asMiddleAge() {
        return "Op middelbare leeftijd";
    }

    @Override
    public String asLateOnset() {
        return "Op oude leeftijd";
    }


}
