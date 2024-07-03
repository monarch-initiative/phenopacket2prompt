package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualGerman implements PPKtIndividualInfoGenerator {

    private final BuildingBlockGenerator bbGenerator;
    /** grammatical sex */
    private enum GrammatikalischesGeschlecht {
        MAENNLICH, WEIBLICH, NEUTRUM
    }

    public PpktIndividualGerman() {
        bbGenerator = new GermanBuildingBlocks();
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
            onsetDescription = "Der Krankheitsbeginn wurde nicht angegeben";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);
    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge) {
        return String.format("Der Krankheitsbeginn trat %s auf",
                nameOfLifeStage(hpoOnsetTermAge));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return "während der Fetalperiode";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "zum Zeitpunkt der Geburt";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "im Säuglingsalter";
        } else if (hpoOnsetTermAge.isChild()) {
            return "im Kindesalter";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "im Jugendlichenalter";
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "im Neugeborenenalter"; // +bbGenerator.newborn();
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "im jungen Erwachsenenalter" ;
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "im mittleren Erwachsenenalter" ;
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "im späten Erwachsenenalter" ;
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "im Erwachsenenalter" ;
        } else {
            throw new PhenolRuntimeException("Could not identify German life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("Der Krankheitsbeginn trat im Alter von %s auf",
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
            return String.format("Im Alter von %d Jahren", y);
        } else if (y > 0) {
            if (m > 0) {
                return String.format("Im Alter von %d %s und %d %s", y,
                        y>1?"Jahren" : "Jahr",
                        m,  m>1?"Monaten" : "Monat");
            } else {
                return String.format("Im Alter von %d %s", y, y>1?"Jahren" : "Jahr");
            }
        }
        if (m>0) {
            return String.format("Im Alter von %d %s y %d %s", m,  m>1?"Monaten" : "Monat",
                    d,  d>1?"Tagen" : "Tag");
        } else {
            return String.format("%d Tage",  d);
        }
     }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            return switch (psex) {
                case FEMALE -> String.format("Die Probandin war eine %s Frau",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.WEIBLICH));
                case MALE -> String.format("Der Proband war ein %s Mann",
                        dAlter(iso8601Age, GrammatikalischesGeschlecht.MAENNLICH));
                default -> String.format("Der Proband war ein %s %s",
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
                case FEMALE -> String.format("Die Probandin war ein %s", bbGenerator.newbornGirl()); // das
                case MALE -> String.format("Der Proband war ein %s", bbGenerator.newbornBoy());
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
            case NEUTRUM -> String.format("%s alte", ymd);
            //TODO: check this is OK. "alte" in the examples I have seen always refers to "die Person", which is feminine, e.g. "46 Jahre alte erwachsene Person", not "altes"
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
                case FEMALE -> "Die Probandin war ein weibliches Neugeborenes";
                case MALE -> "Der Proband war ein männliches Neugeborenes";
                default -> "Der Patient war ein Neugeborenes ohne angegebenes Geschelcht";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "Die Probandin war ein weiblicher Säugling";
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
