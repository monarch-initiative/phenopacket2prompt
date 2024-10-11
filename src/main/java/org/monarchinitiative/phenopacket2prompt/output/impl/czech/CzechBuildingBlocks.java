package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class CzechBuildingBlocks implements BuildingBlockGenerator {


    @Override
    public String days(int d) {
        return d>1 ? "dní" : "den";
    }

    @Override
    public String months(int m) {
        return m>1 ? "měsíců" : "měsíc";
    }

    @Override
    public String years(int y) {
        return y>1 ? "let" : "rok";
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
        return String.format("%d let", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d měsíců", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d %s", d, days(d));
    }

    @Override
    public String monthDayOld(int m, int d) {
        if (m==0) {
            return "vo věku %d %s".formatted(d, days(d));
        } else if (d==0) {
            return "vo věku %d %s".formatted(m, months(m));
        }
        return String.format("vo věku %d %s %d %s", m, months(m), d, days(d));
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        if (y==0) {
            return monthDayOld(m,d);
        }
        if (d==0) {
            return String.format("vo věku %d %s a %d %s", y, years(y), m, months(m));
        }
        return String.format(
                "vo věku %d %s, %d %s a %d %s",
                y, years(y),
                m, months(m),
                d, days(d)
        );
    }

    @Override
    public String asNewborn() {
        return "jako novorozenec";
    }

    @Override
    public String atTheAgeOf() {
        return "vo věku";
    }


    @Override
    public String she() {
        return "ona";
    }

    @Override
    public String he() {
        return "on";
    }

    @Override
    public String theProband() {
        return "proband";
    }

    @Override
    public String woman() {
        return "žena";
    }

    @Override
    public String man() {
        return "muž";
    }

    @Override
    public String individual() {
        return "osoba";
    }

    @Override
    public String theIndividual() {
        return "osoba";
    }

    @Override
    public String girl() {
        return "dívka";
    }

    @Override
    public String boy() {
        return "chlapec";
    }

    @Override
    public String child() {
        return "dítě";
    }

    @Override
    public String adolescentGirl() {
        return "adolescent ženského pohlaví";
    }

    @Override
    public String adolescentBoy() {
        return "adolescent mužského pohlaví";
    }

    @Override
    public String adolescentChild() {
        return "adolescent";
    }

    @Override
    public String maleInfant() {
        return "novorozenec mužského pohlaví";
    }

    @Override
    public String femaleInfant() {
        return "novorozenec ženského pohlaví";
    }

    @Override
    public String infant() {
        return "kojenec";
    }

    @Override
    public String newbornBoy() {
        return "novorozený chlapec";
    }

    @Override
    public String newbornGirl() {
        return "novorozená dívka";
    }

    @Override
    public String newborn() {
        return "novorozenec";
    }

    @Override
    public String maleFetus() {
        return "plod mužského pohlaví";
    }

    @Override
    public String femaleFetus() {
        return "plod ženského pohlaví";
    }

    @Override
    public String fetus() {
        return "plod";
    }

    @Override
    public String female() {
        return "žena";
    }

    @Override
    public String male() {
        return "muž";
    }

    @Override
    public String adult() {
        return "dospelý";
    }

    @Override
    public String probandWasA() {
        return "Proband bol";
    }

    @Override
    public String whoPresented() {
        return "ktorý prezentoval";
    }

    @Override
    public String presented() {
        return "prezentoval";
    }

    @Override
    public String probandNoAgePresented() {
        return "Klinické příznaky probanda";
    }

    @Override
    public String probandNoAgePresentedWith() {
        return "Klinické příznaky probanda zahrnovaly";
    }



    @Override
    public String presentedWith() {
        return "prezentoval s";
    }

    @Override
    public String with() {
        return "s";
    }


    @Override
    public String probandWasAFemale() {
        return "Probandka byla žena";
    }

    @Override
    public String probandWasAMale() {
        return "Proband byl muž";
    }

    @Override
    public String probandWasAnIndividual() {
        return "Proband byla osoba blíže neurčeného pohlaví a věku";
    }

    @Override
    public String inWhomManifestationsWereExcluded() {
        return "u koho boli vyloučeny následující klinické manifestace";
    }

    @Override
    public String duringFetal() {
        return "Počas tehotenstva";
    }

    @Override
    public String duringEmbryonic() {
        return "Počas embryonálnej fázy tehotenstva";
    }

    @Override
    public String asNeonate() {
        return "Ako novorozenec";
    }

    @Override
    public String atBirth() {
        return  "Od narození";
    }

    @Override
    public String asInfant() {
        return "Ako novorozenec";
    }

    @Override
    public String inChildhood() {
        return "V dětství";
    }

    @Override
    public String asAdolescent() {
        return "Během dospívání";
    }

    @Override
    public String asAdult() {
        return "Jako dospělý";
    }

    @Override
    public String asYoungAdult() {
        return "Ve věku mladé dospělosti";
    }

    @Override
    public String asMiddleAge() {
        return "Během středního věku";
    }

    @Override
    public String asLateOnset() {
        return "Ve stáří";
    }


}
