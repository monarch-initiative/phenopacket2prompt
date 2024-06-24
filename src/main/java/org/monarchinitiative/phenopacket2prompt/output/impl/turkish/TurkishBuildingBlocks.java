package org.monarchinitiative.phenopacket2prompt.output.impl.turkish;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class TurkishBuildingBlocks implements BuildingBlockGenerator {
    @Override
    public String days(int d) {
        return "";
    }

    @Override
    public String months(int m) {
        return "";
    }

    @Override
    public String years(int y) {
        return "";
    }

    @Override
    public String yearsOld(int y) {
        return String.format("%djährig", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d Monate alt", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d Tage alt", d);
    }

    @Override
    public String monthDayOld(int m, int d) {
        List<String> components = new ArrayList<>();
        if (m > 0) {
            components.add(String.format("%d %s", m, m > 1 ? "Monaten" : "Monat"));
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, d > 1 ? "Tagen" : "Tag"));
        }
        if (components.isEmpty()) {
            return "am ersten Lebenstag";
        } else if (components.size() == 1) {
            return components.get(0);
        } else {
            return String.format("im Alter von %s und %s", components.get(0), components.get(1));
        }
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d %s", y, y > 1 ? "Jahren" : "Jahr"));
        }
        if (m > 0) {
            components.add(String.format("%d %s", m, m > 1 ? "Monaten" : "Monat"));
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, d > 1 ? "Tagen" : "Tag"));
        }
        if (components.isEmpty()) {
            return "am ersten Lebenstag";
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            return String.format("im Alter von %s und %s", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("im Alter von %s, %s und %s", components.get(0), components.get(1), components.get(2));
        }
    }

    @Override
    public String asNewborn() {
        return "";
    }

    @Override
    public String atTheAgeOf() {
        return "";
    }

    @Override
    public String she() {
        return "";
    }

    @Override
    public String he() {
        return "";
    }

    @Override
    public String theProband() {
        return "";
    }

    @Override
    public String woman() {
        return "Frau";
    }

    @Override
    public String man() {
        return "Mann";
    }

    @Override
    public String individual() {
        return "erwachsene Person unbekannten Geschlechtes";
    }

    @Override
    public String theIndividual() {
        return "";
    }

    @Override
    public String girl() {
        return "Mädchen";
    }

    @Override
    public String boy() {
        return "Junge";
    }

    @Override
    public String child() {
        return "Kind";
    }

    @Override
    public String adolescentGirl() {
        return "";
    }

    @Override
    public String adolescentBoy() {
        return "";
    }

    @Override
    public String adolescentChild() {
        return "";
    }

    @Override
    public String maleInfant() {
        return "männlicher Säugling";
    }

    @Override
    public String femaleInfant() {
        return "weiblicher Säugling";
    }

    @Override
    public String infant() {
        return "Säugling";
    }

    @Override
    public String newbornBoy() {
        return "männliches Neugeborenes";
    }

    @Override
    public String newbornGirl() {
        return "weibliches Neugeborenes";
    }

    @Override
    public String newborn() {
        return "Neugeborenes";
    }

    @Override
    public String maleFetus() {
        return "männlicher Fet";
    }

    @Override
    public String femaleFetus() {
        return "weiblicher Fet";
    }

    @Override
    public String fetus() {
        return "Fet";
    }

    @Override
    public String female() {
        return "";
    }

    @Override
    public String male() {
        return "";
    }

    @Override
    public String adult() {
        return "";
    }

    @Override
    public String probandWasA() {
        return "Der Proband war";
    }

    @Override
    public String whoPresented() {
        return "";
    }

    @Override
    public String presented() {
        return "";
    }

    @Override
    public String probandNoAgePresented() {
        return "";
    }

    @Override
    public String probandNoAgePresentedWith() {
        return "";
    }

    @Override
    public String probandWasAMale() {
        return "Der Proband war ein Mann";
    }

    @Override
    public String probandWasAFemale() {
        return "Die Probandin war eine Frau";
    }

    @Override
    public String probandWasAnIndividual() {
        return "Der Proband war ein Individuum ohne angegebenes Geschlecht";
    }

    @Override
    public String presentedWith() {
        return "";
    }

    @Override
    public String with() {
        return "";
    }

    @Override
    public String inWhomManifestationsWereExcluded() {
        return "";
    }

    @Override
    public String duringFetal() {
        return "";
    }

    @Override
    public String asNeonate() {
        return "";
    }

    @Override
    public String atBirth() {
        return "";
    }

    @Override
    public String asInfant() {
        return "";
    }

    @Override
    public String inChildhood() {
        return "";
    }

    @Override
    public String asAdolescent() {
        return "";
    }

    @Override
    public String asAdult() {
        return "";
    }

    @Override
    public String asYoungAdult() {
        return "";
    }

    @Override
    public String asMiddleAge() {
        return "";
    }

    @Override
    public String asLateOnset() {
        return "";
    }

    @Override
    public String fromIso(Iso8601Age ppktAge) {
        return "";
    }
}
