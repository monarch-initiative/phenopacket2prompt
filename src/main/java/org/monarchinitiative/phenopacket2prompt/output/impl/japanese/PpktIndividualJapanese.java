package org.monarchinitiative.phenopacket2prompt.output.impl.japanese;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualJapanese implements PPKtIndividualInfoGenerator {

    private final BuildingBlockGenerator bbGenerator;
    /** grammatical sex */
    private enum GrammatikalischesGeschlecht {
        MAENNLICH, WEIBLICH, NEUTRUM
    }

    public PpktIndividualJapanese() {
        bbGenerator = new JapaneseBuildingBlocks();
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
            onsetDescription = "発症は特定されていない"; // Onset was not indicated
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);
    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge) {
        return String.format("発症したのは: %s ", // disease had onset at
                nameOfLifeStage(hpoOnsetTermAge));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return "胎児期"; // during fetal period
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "出生時"; // at birth
        } else if (hpoOnsetTermAge.isInfant()) {
            return "幼時"; // in infacny
        } else if (hpoOnsetTermAge.isChild()) {
            return "幼少期"; // in childhood
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "思春期"; // in adolescence
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "新生児期"; // neonatal period
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "年少時" ; // as a young adult
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "中年期" ; // in middle age
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "晩年" ; // in old age
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "成長期" ; // in adulthood
        } else {
            throw new PhenolRuntimeException("Could not identify Japanese life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("発病したのは19歳のときだった。 %s", // disease had onset at (iso 8601 age)
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
                    return String.format("Eine %djährige Patientin", y);
                } else if (y > 9) {
                    return String.format("Eine %djährige Jugendliche", y);
                } else if (y > 0) {
                    return String.format("Ein %djähriges Mädchen", y);
                } else if (m>0) {
                    return String.format("Ein %d Monate alter weiblicher Säugling", m);
                } else  {
                    return String.format("Ein %d Tage alter weiblicher Säugling", d);
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
            return String.format("歳で %d ", y); // at an age of ... ywars
        } else if (y > 0) {
            if (m > 0) {
                return String.format("歳のとき %d  月 %d", y, m);
            } else {
                return String.format("歳のとき %d 年", y); // At age of. 年=year
            }
        }
        if (m>0 && d>0) {
            return String.format("歳のとき  %d 月 そして %d 日", m, d); // 月 = Month 日=day 歳のとき - at age of
        } else if (m>0 && d==0) {
            return String.format("歳のとき  %d 月",  m);
        }
        else {
            return String.format("歳のとき  %d 日",  d);
        }
     }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            return switch (psex) {
                case FEMALE -> String.format("テストに参加したのは %s の女性でした。", // the proband was a .. woman
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH));
                case MALE -> String.format("テストに参加したのは%sの男性",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH));
                default -> String.format("テスト担当者は %s 人だった %s",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH),
                        bbGenerator.individual());
            };
        } else if (y > 9) {
            return switch (psex) {
                case FEMALE -> String.format("%s  %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH),
                        bbGenerator.adolescentGirl());
                case MALE -> String.format("%s  %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),
                        bbGenerator.adolescentBoy());
                default -> String.format("%s  %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),  bbGenerator.adolescentChild());
            };
        } else if (y > 0) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM), // "das Mädchen"
                        bbGenerator.girl());
                case MALE -> String.format("%s  %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),
                        bbGenerator.boy());
                default -> String.format("%s  %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM), // Das Individuum
                        bbGenerator.child());
            };
        } else if (m > 0 || d > 0) {
            return switch (psex) {
                case FEMALE -> String.format("遺族は女児であった。 %s", iso8601Age); // proband was an infant girl of age
                case MALE -> String.format("遺族は男児であった。 %s",  iso8601Age); // proband was an infant boy of age
                default -> String.format("遺児は生後間もない乳児であった。 %s", iso8601Age);// proband was an infant of age
            };
        } else {
            return switch (psex) {
                case FEMALE -> String.format("被験者は生まれたばかりの女の子"); // The proband was a newborn girl
                case MALE -> String.format("被験者は生まれたばかりの男の子");// The proband was a newborn boy
                default -> String.format("被験者は新生児"); // The proband was a newborn  (sex unknown)
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
            components.add(String.format("%d 年", y)); // year=年
        }
        if (m > 0) {
            components.add(String.format("%d 月", m)); // 月 = Month
        }
        if (d > 0) {
            components.add(String.format("%d 日", d)); // 日 = day
        }
        String ymd;
        if (components.isEmpty()) {
            ymd = "";
        } else if (components.size() == 1) {
            ymd = components.get(0);
        } else if (components.size() == 2) {
            ymd = String.format("%s そして %s", components.get(0), components.get(1)); // そして = and
        } else {
            ymd = String.format("%s, %s そして %s", components.get(0), components.get(1), components.get(2));
        }
        return switch (geschlecht) {
            case MAENNLICH -> String.format("%s 年齢", ymd); // 年齢 = "-old"
            case WEIBLICH -> String.format("%s 年齢", ymd);
            case NEUTRUM -> String.format("%s 年齢", ymd);
        };
    }


    private String iso8601ToYearMonth(Iso8601Age iso8601Age, PhenopacketSex psex) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (psex.equals(PhenopacketSex.MALE)) {
            return String.format("%d 歳の少年", y); //-year old boy
        } else if (psex.equals(PhenopacketSex.FEMALE)) {
            return String.format("%d 歳の少女", y); //-year old girl
        }
        return String.format("ein %d 歳の子供", y); //-year old child
    }


    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format(" %d 日", d); // days
        } else if (d>0){
            return String.format("%d 月 そして %d 日", m, d); // "月"=Month 日=days そして=and
        } else {
            return String.format("%d 月", m); // months
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

        if (isoAge.getYears()>1) {
            components.add(String.format("%d 年", isoAge.getYears())); // years
        } else if (isoAge.getYears() == 1) {
            components.add("一年"); // 1 year
        }
        if (isoAge.getMonths() > 1) {
            components.add(String.format("%d 月", isoAge.getMonths())); // months
        } else if (isoAge.getMonths() == 1) {
            components.add("一ヶ月"); // one month
        }
        if (isoAge.getDays()>1) {
            components.add(String.format("%d 日", isoAge.getDays())); // days
        } else if (isoAge.getDays()==1) {
            components.add("一日"); // one day
        }
        if (components.isEmpty()) {
            return "生まれながら"; // at birth
        } else if (components.size() == 1) {
            return "歳にして " + components.getFirst(); // at the age of
        } else if (components.size() == 2) {
            return "歳にして  " + components.get(0) + " そして " + components.get(1);
        } else {
            return "歳にして "  + components.get(0) + ", " + components.get(1) +
                    " そして " + components.get(2);
        }
    }



    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", bbGenerator.probandWasAFemale(), bbGenerator.femaleFetus());
                case MALE -> String.format("%s %s", bbGenerator.probandWasAMale(), bbGenerator.maleFetus());
                default -> String.format("%s %s", bbGenerator.probandWasA(), bbGenerator.fetus());
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "被験者は新生児の女性";// patient was a female newborn
                case MALE -> "被験者は新生児の男性"; // patient was a male newborn
                default -> "患者は性別不明の新生児であった。"; // patient was a newborn
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "被験者は女性の乳児"; // Proband was a female infant
                case MALE -> "被験者は男性の乳児"; // Proband was a male infant
                default -> "被験者は性別不詳の幼児"; // proband was an infant
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "被験者は少女"; // proband was a girl
                case MALE -> "テストに参加したのは少年だった"; // proband was a boy
                default -> "被検者は性別不詳の子供である。"; // proband was a child
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "被験者はティーンエイジャー";// the proband was an adolescent girl
                case MALE -> "被験者はティーンエイジャー";// the proband was an adolescent boy
                default -> "被験者の性別は不詳。"; // the proband was an adolescent
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "被験者は女性";
                case MALE -> "テストに参加したのは、ある男性だった。";
                default -> "被験者は性別不詳の成人である。";

            };
        } else {
            throw new PhenolRuntimeException("Could not find HPO onset type " + hpoOnsetTermAge.toString());
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "sie";
            case MALE -> "er";
            default -> "die Person";
        };
    }

    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return imAlterVonIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            if (ppktAge.isFetus()) {
                return "Während der Fetalperiode";
            } else if (ppktAge.isCongenital()) {
                return "Zum Zeitpunkt der Geburt";
            } else if (ppktAge.isEmbryo()) {
                return "Während der Embryonalzeit";
            } else if (ppktAge.isNeonate()) {
                return "In der neugeborenen Zeit";
            } else if (ppktAge.isInfant()) {
                return "Als Säugling";
            } else if (ppktAge.isChild()) {
                return "In der Kindheit";
            } else if (ppktAge.isJuvenile()) {
                return "Im Jugendlichenalter";
            } else if (ppktAge.isYoungAdult()) {
                return "Im jungen Erwachsenenalter";
            } else if (ppktAge.isMiddleAge()) {
                return "Im mittleren Erwachsenenalter";
            } else if (ppktAge.isLateAdultAge()) {
                return "Im späten Erwachsenenalter";
            } else if (ppktAge.isAdult()) {
                return "Im Erwachsenenalter";
            } else {
                throw new PhenolRuntimeException("Did not recognize onset: " + ppktAge.toString());
            }
        } else {
            throw new PhenolRuntimeException("Bad age type");

        }


    }
}
