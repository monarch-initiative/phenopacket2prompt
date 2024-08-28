package org.monarchinitiative.phenopacket2prompt.output.impl.chinese;

import org.monarchinitiative.phenopacket2prompt.model.Iso8601Age;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class ChineseBuildingBlocks implements BuildingBlockGenerator {
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
        return String.format("%d岁", y);
    }

    @Override
    public String monthsOld(int m) {
        return String.format("%d个月大", m);
    }

    @Override
    public String daysOld(int d) {
        return String.format("%d天大", d);
    }

    @Override
    public String monthDayOld(int m, int d) {
        List<String> components = new ArrayList<>();
        if (m > 0) {
            components.add(String.format("%d个月", m));
        }
        if (d > 0) {
            components.add(String.format("%d天", d));
        }
        if (components.isEmpty()) {
            return "自出生起";
        } else if (components.size() == 1) {
            return components.get(0);
        } else {
            return String.format("%s%s大时", components.get(0), components.get(1));
        }
    }

    @Override
    public String yearsMonthsDaysOld(int y, int m, int d) {
        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d岁", y));
        }
        if (m > 0) {
            components.add(String.format("%d个月", m));
        }
        if (d > 0) {
            components.add(String.format("%d天", d));
        }
        if (components.isEmpty()) {
            return "自出生起";
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            return String.format("%s%s大时", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("%s%s%s大时", components.get(0), components.get(1), components.get(2));
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
        return "受试者";
    }

    @Override
    public String woman() {
        return "女性";
    }

    @Override
    public String man() {
        return "男性";
    }

    @Override
    public String individual() {
        return "未知性别成年人";
    }

    @Override
    public String theIndividual() {
        return "";
    }

    @Override
    public String girl() {
        return "女孩";
    }

    @Override
    public String boy() {
        return "男孩";
    }

    @Override
    public String child() {
        return "儿童";
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
        return "男婴";
    }

    @Override
    public String femaleInfant() {
        return "女婴";
    }

    @Override
    public String infant() {
        return "婴儿";
    }

    @Override
    public String newbornBoy() {
        return "男性新生儿";
    }

    @Override
    public String newbornGirl() {
        return "女性新生儿";
    }

    @Override
    public String newborn() {
        return "新生儿";
    }

    @Override
    public String maleFetus() {
        return "男性胎儿";
    }

    @Override
    public String femaleFetus() {
        return "女性胎儿";
    }

    @Override
    public String fetus() {
        return "胎儿";
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
        return "受试者是";
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
        return "受试者为一名男性";
    }

    @Override
    public String probandWasAFemale() {
        return "受试者为一名女性";
    }

    @Override
    public String probandWasAnIndividual() {
        return "受试者性别未知";
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