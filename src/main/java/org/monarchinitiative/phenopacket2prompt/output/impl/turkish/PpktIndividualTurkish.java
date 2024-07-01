package org.monarchinitiative.phenopacket2prompt.output.impl.turkish;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualTurkish implements PPKtIndividualInfoGenerator {

    private final BuildingBlockGenerator bbGenerator;
    /** grammatical sex */
    private enum GrammatikalischesGeschlecht {
        MAENNLICH, WEIBLICH, NEUTRUM
    }

    public PpktIndividualTurkish() {
        bbGenerator = new TurkishBuildingBlocks();
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
            onsetDescription = "Hastalığın başlangıcı belirtilmedi";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);
    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge) {
        return String.format("Hastalığın başlangıcı %s meydana geldi",
                nameOfLifeStage(hpoOnsetTermAge));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return "fetüs döneminde";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "doğumda";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "infantil döneminde";
        } else if (hpoOnsetTermAge.isChild()) {
            return "çocukluk döneminde";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "ergenlik döneminde";
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "yenidoğan döneminde";
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "genç yetişkinlik döneminde";
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "orta yaş döneminde";
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "geç yetişkinlik döneminde";
        } else if (hpoOnsetTermAge.isAdult()) {
            return "yetişkinlik döneminde";
        } else {
            throw new PhenolRuntimeException("Could not identify Turkish life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("Hastalığın başlangıcı %s yaşında meydana geldi",
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
                    return String.format("%d yaşındaki bir hasta", y);
                } else if (y > 9) {
                    return String.format("%d yaşındaki bir genç", y);
                } else if (y > 0) {
                    return String.format("%d yaşındaki bir kız çocuğu", y);
                } else if (m > 0) {
                    return String.format("%d aylık bir kız bebek", m);
                } else  {
                    return String.format("%d günlük bir kız bebek", d);
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
            return String.format("%d yaşında", y);
        } else if (y > 0) {
            if (m > 0) {
                return String.format("%d %s ve %d %s yaşında", y, "yıl", m, "ay");
            } else {
                return String.format("%d yaşında", y);
            }
        }
        if (m > 0) {
            return String.format("%d %s ve %d %s yaşında", m, "ay", d, "gün");
        } else {
            return String.format("%d gün", d);
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            return switch (psex) {
                case FEMALE -> String.format("Proband %d yaşında bir kadındı",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH));
                case MALE -> String.format("\"Proband %d yaşında bir adamdı",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH));
                default -> String.format("Proband %d yaşında bir bireydi",
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
            String age = String.format("Olgu %d yaş %d aylık", y, m);
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM), // "das Mädchen"
                        bbGenerator.girl());
                case MALE -> String.format("%s bir erkek çocuktu.", age);
                default -> String.format("Bu Pat. bir yil on ay kacindik%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.NEUTRUM), // Das Individuum
                        bbGenerator.child());
            };
        } else if (m > 0 || d > 0) {
            return switch (psex) {
                case FEMALE -> String.format("%s ein %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH), // "der WEIBLICHe Säungling",
                        bbGenerator.femaleInfant());
                case MALE -> String.format("%s ein %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH),
                        bbGenerator.maleInfant());
                default -> String.format("%s %s %s", bbGenerator.probandWasA(),
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH), // "der Säugling
                        bbGenerator.infant());
            };
        } else {
            return switch (psex) {
                case FEMALE -> String.format("Die Probandin war ein %s", bbGenerator.probandWasA(), bbGenerator.newbornGirl());
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
            components.add(String.format("%d %s", y, y > 1 ? "Jahre" : "Jahr"));
        }
        if (m > 0) {
            components.add(String.format("%d %s", m, m > 1 ? "Monate" : "Monat"));
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, d > 1 ? "Tage" : "Tag"));
        }
        String ymd;
        if (components.isEmpty()) {
            ymd = "";
        } else if (components.size() == 1) {
            ymd = components.get(0);
        } else if (components.size() == 2) {
            ymd = String.format("%s und %s", components.get(0), components.get(1));
        } else {
            ymd = String.format("%s, %s und %s", components.get(0), components.get(1), components.get(2));
        }
        return switch (geschlecht) {
            case MAENNLICH -> String.format("%s alter", ymd);
            case WEIBLICH -> String.format("%s alte", ymd);
            case NEUTRUM -> String.format("%s altes", ymd);
        };
    }


    private String iso8601ToYearMonth(Iso8601Age iso8601Age, PhenopacketSex psex) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (psex.equals(PhenopacketSex.MALE)) {
            if (iso8601Age.getMonths() == 0) {
                return String.format("ein %djähriger Junge", y);
            } else {
                return String.format("ein %d %s, %d %s alter Junge", y, y>1?"Jahre":"Jahr", m, m>1?"Monate":"Monat");
            }
        } else if (psex.equals(PhenopacketSex.FEMALE)) {
            if (iso8601Age.getMonths() == 0) {
                return String.format("ein %djähriges Mädchen", y);
            } else {
                return String.format("ein %d %s, %d %s altes Mädchen", y, y>1?"Jahre":"Jahr", m, m>1?"Monate":"Monat");
            }
        }
        if (iso8601Age.getMonths() == 0) {
            return String.format("ein %djähriges Kind", y);
        } else {
            return String.format("ein %d %s, %d %s altes Kind", y, y>1?"Jahre":"Jahr", m, m>1?"Monate":"Monat");        }
    }

    private String monthString(int m) {
        return m>1 ? "Monate": "Monat";
    }

    private String dayString(int d) {
        return d>1 ? "Tage": "Tag";
    }

    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format("de %d dias", d);
        } else if (d>0){
            return String.format("%d %s und %d %s", m, monthString(m), d, dayString(d));
        } else {
            return String.format("%d %s", m, m>1 ? "Monate": "Monat");
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
            components.add(String.format("%d Jahren", isoAge.getYears()));
        } else if (isoAge.getYears() == 1) {
            components.add("einem Jahr");
        }
        if (isoAge.getMonths() > 1) {
            components.add(String.format("%d Monaten", isoAge.getMonths()));
        } else if (isoAge.getMonths() == 1) {
            components.add("einem Monat");
        }
        if (isoAge.getDays()>1) {
            components.add(String.format("%d Tagen", isoAge.getDays()));
        } else if (isoAge.getDays()==1) {
            components.add("einem Tag");
        }
        if (components.isEmpty()) {
            return "bei der Geburt";
        } else if (components.size() == 1) {
            return "im Alter von " + components.getFirst();
        } else if (components.size() == 2) {
            return "im Alter von  " + components.get(0) + " und " + components.get(1);
        } else {
            return "im Alter von "  + components.get(0) + ", " + components.get(1) +
                    " und " + components.get(2);
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
                case FEMALE -> "Die Probandin war ein WEIBLICHes Neugeborenes";
                case MALE -> "Der Probandwar ein männliches Neugeborenes";
                default -> "Der Patient war ein Neugeborenes ohne angegebenes Geschelcht";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "Die Probandin war ein WEIBLICHer Säugling";
                case MALE -> "Der Proband war ein männlicher Säugling";
                default -> "Der Proband war ein Säugling ohne angegebenes Geschlecht";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "Die Probandin war ein Mädchen";
                case MALE -> "Der Proband war ein Junge";
                default -> "Der Proband war ein Kind ohne angegebenes Geschlecht";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "Die Probandin war eine Jugendliche";
                case MALE -> "Der Proband war ein Jugendlicher";
                default -> "Der Proband war ein Jugendlicher ohne angegebenes Geschlecht";
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "Die Probandin war eine Frau";
                case MALE -> "Der Proband war ein Mann";
                default -> "Der Proband war eine erwachsene Person ohne angegebenes Geschlecht";

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
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "Als Säugling";
                case "Childhood onset" -> "In der Kindheit";
                case "Neonatal onset"  -> "In der neugeborenen Zeit";
                case "Congenital onset" -> "Zum Zeitpunkt der Geburt";
                case "Adult onset" -> "Im Erwachsenenalter";
                case "Juvenile onset" -> "Im Jugendlichenalter";
                default-> {
                    throw new PhenolRuntimeException("No German translation for " + label);
                }
            };
        } else {
            return ""; // should never get here
        }
    }




}
