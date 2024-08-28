package org.monarchinitiative.phenopacket2prompt.output.impl.chinese;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualChinese implements PPKtIndividualInfoGenerator {

    private final BuildingBlockGenerator bbGenerator;
    /** grammatical sex */
    private enum GrammatikalischesGeschlecht {
        MAENNLICH, WEIBLICH, NEUTRUM
    }// may not need this

    public PpktIndividualChinese() {
        bbGenerator = new ChineseBuildingBlocks();
    }

    @Override
    public String getIndividualDescription(PpktIndividual individual) {
        if (individual.annotationCount() == 0) {
            throw new PhenolRuntimeException("No HPO annotations");
        }
        Optional<PhenopacketAge> lastExamOpt = individual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        PhenopacketSex psex = individual.getSex();
        String individualDescription;
        String onsetDescription;
        if (lastExamOpt.isPresent()) {
            var lastExamAge =  lastExamOpt.get();
            if (lastExamAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) lastExamAge;
                individualDescription = iso8601individualDescription(psex, isoAge);
            } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
                individualDescription = hpoOnsetIndividualDescription(psex, hpoOnsetTermAge);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + lastExamAge.ageType());
            }
        }  else {
            individualDescription =  switch (psex) {
                case FEMALE -> bbGenerator.probandWasAFemale();
                case MALE -> bbGenerator.probandWasAMale();
                default -> bbGenerator.probandWasAnIndividual();
            };
        }
        if (onsetOpt.isPresent()) {
            var onsetAge = onsetOpt.get();
            if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) onsetAge;
                onsetDescription =  iso8601onsetDescription(isoAge);
            } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
                onsetDescription = hpoOnsetDescription(hpoOnsetTermAge);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + onsetAge.ageType());
            }
        } else {
            onsetDescription = "发病时间未知";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);
    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge) {
        return String.format("疾病于患者 %s 发作",
                nameOfLifeStage(hpoOnsetTermAge));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return "胎儿时期";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "出生时";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "婴儿时";
        } else if (hpoOnsetTermAge.isChild()) {
            return "幼年时";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "青少年时";
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "为新生儿时"; // +bbGenerator.newborn();
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "青年时" ;
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "中年时" ;
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "晚年时" ;
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "成年后" ;
        } else {
            throw new PhenolRuntimeException("Could not identify German life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("发病时间为 %s ",
                bbGenerator.yearsMonthsDaysOld(isoAge.getYears(), isoAge.getMonths(), isoAge.getDays()));
    }







    public String ageAndSexAtLastExamination(PpktIndividual individual) {
        PhenopacketSex psex = individual.getSex();
        Optional<PhenopacketAge> ageOpt = individual.getAgeAtLastExamination();
        if (ageOpt.isEmpty()) {
            ageOpt = individual.getAgeAtOnset();
        }
        String sex;
        switch (psex) {
            case FEMALE -> sex = bbGenerator.woman();
            case MALE -> sex = bbGenerator.man();
            default -> sex = bbGenerator.adult();
        }

        if (ageOpt.isEmpty()) {
           return sex;
        }
        PhenopacketAge age = ageOpt.get();
        if (age.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoage = (Iso8601Age) age;
            int y = isoage.getYears();
            int m = isoage.getMonths();
            int d = isoage.getDays();
            if (psex.equals(PhenopacketSex.FEMALE)) {
                if (y > 17) {
                    return String.format("%d 岁患者", y);
                } else if (y > 9) {
                    return String.format("%d 岁女孩", y);
                } else if (y > 0) {
                    return String.format("%d 岁女童", y);
                } else if (m>0) {
                    return String.format("%d 月大的女婴", m);
                } else  {
                    return String.format("%d 天大的女婴", d);
                }
            }
        } else {
            // age is an HPO onset term, we do not have an exact date
        }
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> bbGenerator.girl();
                case MALE -> bbGenerator.boy();
                default -> bbGenerator.child();
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> bbGenerator.newbornGirl();
                case MALE -> bbGenerator.newbornBoy();
                default -> bbGenerator.newborn();
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> bbGenerator.femaleFetus();
                case MALE -> bbGenerator.maleFetus();
                default -> bbGenerator.fetus();
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> bbGenerator.femaleInfant();
                case MALE -> bbGenerator.maleInfant();
                default -> bbGenerator.infant();
            };
        } else {
            return switch (psex) {
                // TODO -- MORE GRANULARITY
                case FEMALE -> bbGenerator.woman();
                case MALE -> bbGenerator.man();
                default -> bbGenerator.adult();
            };
        }
    }


    private String imAlterVonIsoAgeExact(PhenopacketAge ppktAge) {
        Iso8601Age iso8601Age = (Iso8601Age) ppktAge;
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        if (y > 10) {
            return String.format("%d岁时", y);
        } else if (y > 0) {
            if (m > 0) {
                return String.format("%d岁%d个月时", y,
                        m);
            } else {
                return String.format("%d岁时", y);
            }
        }
        if (m>0) {
            return String.format("%d个月%d天大时", m,
                    d);
        } else {
            return String.format("%d天大时",  d);
        }
     }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            return switch (psex) {
                case FEMALE -> String.format("受试者是一名 %s 岁的女性",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH));
                case MALE -> String.format("受试者是一名 %s 岁的男性",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH));
                default -> String.format("受试者是一名%s %s",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM),
                        bbGenerator.individual());
            };
        } else if (y > 9) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH),
                        bbGenerator.adolescentGirl());
                case MALE -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),
                        bbGenerator.adolescentBoy());
                default -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM),  bbGenerator.adolescentChild());
            };
        } else if (y > 0) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM), // "das Mädchen"
                        bbGenerator.girl());
                case MALE -> String.format("%s ein %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),
                        bbGenerator.boy());
                default -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM), // Das Individuum
                        bbGenerator.child());
            };
        } else if (m > 0 || d > 0) {
            return switch (psex) {
                case FEMALE -> String.format("%s ein %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH), // "der weibliche Säungling",
                        bbGenerator.femaleInfant());
                case MALE -> String.format("%s ein %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),
                        bbGenerator.maleInfant());
                default -> String.format("%s ein %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH), // "der Säugling
                        bbGenerator.infant());
            };
        } else {
            return switch (psex) {
                case FEMALE -> String.format("Die Probandin war ein %s", bbGenerator.probandWasA(), bbGenerator.newbornGirl()); // das
                case MALE -> String.format("Der Proband war ein %s", bbGenerator.probandWasA(), bbGenerator.newbornBoy());
                default -> String.format("Der Proband war ein Neugeborenes ohne angegebenes Geschlecht");
            };
        }
    }

    /**
     * @param iso8601Age
     * @return zB. "4 Jahre und 2 Monate alter" "3 Monate und 1 Tag altes"
     */
    private String dAlter(Iso8601Age iso8601Age, GrammatikalischesGeschlecht geschlecht) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
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
        String ymd;
        if (components.isEmpty()) {
            ymd = "";
        } else if (components.size() == 1) {
            ymd = components.get(0);
        } else if (components.size() == 2) {
            ymd = String.format("%s%s", components.get(0), components.get(1));
        } else {
            ymd = String.format("%s%s%s", components.get(0), components.get(1), components.get(2));
        }

        /*
        return switch (geschlecht) { //may not need this switch function 
            case MAENNLICH -> String.format("%s alter", ymd);
            case WEIBLICH -> String.format("%s alte", ymd);
            case NEUTRUM -> String.format("%s altes", ymd);
        };
        */
       return String.format("%s时",ymd);
    }


    private String iso8601ToYearMonth(Iso8601Age iso8601Age, PhenopacketSex psex) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (psex.equals(PhenopacketSex.MALE)) {
            if (iso8601Age.getMonths() == 0) {
                return String.format("%d岁男孩", y);
            } else {
                return String.format("%d岁%d个月大的男孩", y,m);
            }
        } else if (psex.equals(PhenopacketSex.FEMALE)) {
            if (iso8601Age.getMonths() == 0) {
                return String.format("%d岁女孩", y);
            } else {
                return String.format("%d岁%d个月大女孩", y, m);
            }
        }
        if (iso8601Age.getMonths() == 0) {
            return String.format("%d岁儿童", y);
        } else {
            return String.format("%d岁%d个月大儿童", y, m);        }
    }

    private String monthString(int m) {
        return m>1 ? "个月": "个月"; // maynot need this
    }

    private String dayString(int d) {
        return d>1 ? "天": "天"; // maynot need this
    }

    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format("%d天大", d);
        } else if (d>0){
            return String.format("%d个月%d天大", m, d);
        } else {
            return String.format("%d个月大", m);
        }
    }

    /**
     * Create a phrase such as "at the age of 7 years, 4 months, and 2 days"
     * Leave out the months and days if they are zero.
     * @param isoAge
     * @return
     */
    private String iso8601AtAgeOf(Iso8601Age isoAge) {
        List<String> components = new ArrayList<>();

        if (isoAge.getYears()>=1) {
            components.add(String.format("%d岁", isoAge.getYears()));
        //} else if (isoAge.getYears() == 1) {  //maynot need to seperate the case ==1
        //    components.add("einem Jahr");
        }
        if (isoAge.getMonths() >= 1) {
            components.add(String.format("%d个月", isoAge.getMonths()));
        //} else if (isoAge.getMonths() == 1) {
        //    components.add("einem Monat");
        }
        if (isoAge.getDays()>=1) {
            components.add(String.format("%d天", isoAge.getDays()));
        //} else if (isoAge.getDays()==1) {
        //    components.add("einem Tag");
        }
        if (components.isEmpty()) {
            return "出生时";
        } else if (components.size() == 1) {
            return components.getFirst()+"大";
        } else if (components.size() == 2) {
            return components.get(0) + components.get(1)+"大";
        } else {
            return components.get(0)+ components.get(1) +
                    components.get(2)+"大";
        }
    }
/*
    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "in der Fetalperiode";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "bei der Geburt";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "im Säuglingsalter";
        } else if (hpoOnsetTermAge.isChild()) {
            return "in der Kindheit";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "als Jugendlich adolescente";
        } else {
            return "im Erwachsenenalter";
        }
    }
*/


    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", bbGenerator.probandWasAFemale(), bbGenerator.femaleFetus());
                case MALE -> String.format("%s %s", bbGenerator.probandWasAMale(), bbGenerator.maleFetus());
                default -> String.format("%s %s", bbGenerator.probandWasA(), bbGenerator.fetus());
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "受试者为女性新生儿";
                case MALE -> "受试者为男性新生儿";
                default -> "受试者为新生儿，性别不详";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "受试者为一名女婴";
                case MALE -> "受试者为一名男婴";
                default -> "受试者为婴幼儿，性别不详";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "受试者为一名女童";
                case MALE -> "受试者为一名男童";
                default -> "受试者为儿童，性别不详";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "受试者为女性青少年";
                case MALE -> "受试者为男性青少年";
                default -> "受试者为青少年，性别不详";
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "受试者为成年女性";
                case MALE -> "受试者为成年男性";
                default -> "受试者已成年，性别不详";

            };
        } else {
            throw new PhenolRuntimeException("Could not find HPO onset type " + hpoOnsetTermAge.toString());
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "她";
            case MALE -> "他";
            default -> "患者";
        };
    }

    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return imAlterVonIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "婴幼儿时";
                case "Childhood onset" -> "童年时";
                case "Neonatal onset"  -> "在新生儿时期";
                case "Congenital onset" -> "出生时";
                case "Adult onset" -> "成年后";
                case "Juvenile onset" -> "青少年时期";
                default-> {
                    throw new PhenolRuntimeException("No Chinese translation for " + label);
                }
            };
        } else {
            return ""; // should never get here
        }
    }




}