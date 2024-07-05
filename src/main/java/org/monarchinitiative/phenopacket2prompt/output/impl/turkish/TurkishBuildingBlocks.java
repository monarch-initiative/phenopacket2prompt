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
        return String.format("%d yaşında", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d aylik", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d günlük", d);
    }

    @Override
    public String monthDayOld(int m, int d) {
        List<String> components = new ArrayList<>();
        if (m > 0) {
            components.add(String.format("%d ay", m));
        }
        if (d > 0) {
            components.add(String.format("%d gün", d));
        }
        if (components.isEmpty()) {
            return "doğumdan sonraki ilk gün";
        } else if (components.size() == 1) {
            return components.get(0);
        } else {
            return String.format("%s ve %slıkken", components.get(0), components.get(1));
        }
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d yıl", y));
        }
        if (m > 0) {
            components.add(String.format("%d ay", m));
        }
        if (d > 0) {
            components.add(String.format("%d gün", d));
        }
        if (components.isEmpty()) {
            return "doğumdan sonraki ilk gün";
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            return String.format("%s ve %slıkken", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("%s %s ve %slıkken", components.get(0), components.get(1), components.get(2));
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
        return "kadın";
    }

    @Override
    public String man() {
        return "adam";
    }

    @Override
    public String individual() {
        return "cinsiyeti bilinmeyen yetişkin kişi";
    }

    @Override
    public String theIndividual() {
        return "";
    }

    @Override
    public String girl() {
        return "kız";
    }

    @Override
    public String boy() {
        return "erkek çocuk";
    }

    @Override
    public String child() {
        return "çocuk";
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
        return "erkek bebek";
    }

    @Override
    public String femaleInfant() {
        return "kız bebek";
    }

    @Override
    public String infant() {
        return "bebek";
    }

    @Override
    public String newbornBoy() {
        return "erkek yenidoğan";
    }

    @Override
    public String newbornGirl() {
        return "kız yenidoğan";
    }

    @Override
    public String newborn() {
        return "yenidoğan";
    }

    @Override
    public String maleFetus() {
        return "erkek fetüs";
    }

    @Override
    public String femaleFetus() {
        return "kız fetüs";
    }

    @Override
    public String fetus() {
        return "fetüs";
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
        return "proband şuydu";
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
        return "Proband bir adamdı";
    }

    @Override
    public String probandWasAFemale() {
        return "Proband bir kadındı";
    }

    @Override
    public String probandWasAnIndividual() {
        return "Proband cinsiyeti belirtilmemiş bir bireydi";
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
