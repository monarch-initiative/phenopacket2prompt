package org.monarchinitiative.phenopacket2prompt.output.impl.japanese;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class JapaneseBuildingBlocks implements BuildingBlockGenerator {
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
        return String.format("%d 歳", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d モナート高度", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d 日", d);
    }

    @Override
    public String monthDayOld(int m, int d) {
        List<String> components = new ArrayList<>();
        if (m > 0) {
            components.add(String.format("%d 月", m));
        }
        if (d > 0) {
            components.add(String.format("%d 日数", d));
        }
        if (components.isEmpty()) {
            return "生まれて初めて";
        } else if (components.size() == 1) {
            return components.getFirst();
        } else {
                return String.format("歳にして %s アンド %s", components.get(0), components.get(1));
        }
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d 年", y)); // Years
        }
        if (m > 0) {
            components.add(String.format("%d 月", m)); // Months
        }
        if (d > 0) {
            components.add(String.format("%d 日数", d)); // Days
        }
        if (components.isEmpty()) {
            return "生まれて初めて"; // On the first day of life
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            // At the age of
            return String.format("\"歳にして %s そして %s", components.get(0), components.get(1));
        } else {
            // we must have y,m,d - At the age of ? years, ? months, and ? days
            return String.format("歳にして  %s, %s そして %s", components.get(0), components.get(1), components.get(2));
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
        return "夫人";
    }

    @Override
    public String man() {
        return "男性";
    }

    @Override
    public String individual() {
        return "性別不明";
    }

    @Override
    public String theIndividual() {
        return "";
    }

    @Override
    public String girl() {
        return "女子";
    }

    @Override
    public String boy() {
        return "少年";
    }

    @Override
    public String child() {
        return "子供";
    }

    @Override
    public String adolescentGirl() {
        return "ティーンエイジャー";
    }

    @Override
    public String adolescentBoy() {
        return "思春期";
    }

    @Override
    public String adolescentChild() {
        return "性別不明の青少年";
    } // Adolescent child (sex unknown)

    @Override
    public String maleInfant() {
        return "男児";
    }

    @Override
    public String femaleInfant() {
        return "女児";
    }

    @Override
    public String infant() {
        return "幼児";
    }

    @Override
    public String newbornBoy() {
        return "男性新生児";
    }

    @Override
    public String newbornGirl() {
        return "女性新生児";
    }

    @Override
    public String newborn() {
        return "新生児";
    }

    @Override
    public String maleFetus() {
        return "男性フェット";
    }

    @Override
    public String femaleFetus() {
        return "女性フェット";
    }

    @Override
    public String fetus() {
        return "フェット";
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
        return "テストに参加したのは";
    } // the proband was...

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
            return "テストに参加したのは、ある男性だった。";
    }

    @Override
    public String probandWasAFemale() {
        return "被験者は女性";
    }

    @Override
    public String probandWasAnIndividual() {
        return "被験者は性別・年齢不詳の個人。";
        // The proband was an individual with unspecified age and sex";
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
    public String duringEmbryonic() {
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
