package org.monarchinitiative.phenopacket2prompt.output.impl.german;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketIndividualInformationGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualGerman implements PhenopacketIndividualInformationGenerator {


    private static final String FEMALE_INFANT = "ein weiblicher Säugling";
    private static final String MALE_INFANT = "ein männlicher Säugling";
    private static final String INFANT = "ein Säugling";

    private static final String FEMALE_FETUS = "ein weiblicher Fet";
    private static final String MALE_FETUS = "ein männlicher Fet";
    private static final String FETUS = "ein Fet";

    private static final String FEMALE_CHILD = "Mädchen";
    private static final String MALE_CHILD = "Junge";
    private static final String CHILD = "Kind";

    private static final String FEMALE_ADULT = "Frau";
    private static final String MALE_ADULT = "Mann";
    private static final String ADULT = "erwachsene Person unbekannten Geschlechtes";
    /**
     * Equivalent of "The clinical
     * @param individual
     * @return
     */
    public String ageAndSexAtOnset(PpktIndividual individual) {
        Optional<PhenopacketAge> ageOpt = individual.getAgeAtOnset();
        return "";
    }




    public String ageAndSexAtLastExamination(PpktIndividual individual) {
        PhenopacketSex psex = individual.getSex();
        Optional<PhenopacketAge> ageOpt = individual.getAgeAtLastExamination();
        if (ageOpt.isEmpty()) {
            ageOpt = individual.getAgeAtOnset();
        }
        String sex;
        switch (psex) {
            case FEMALE -> sex = FEMALE_ADULT;
            case MALE -> sex = MALE_ADULT;
            default -> sex = ADULT;
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
                case FEMALE -> FEMALE_CHILD;
                case MALE -> MALE_CHILD;
                default -> CHILD;
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "ein weibliches Neugeborenes";
                case MALE -> "ein männliches Neugeborenes";
                default -> "ein Neugeborenes";
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> "ein weiblicher Fet";
                case MALE -> "ein männlicher Fet";
                default -> "ein Fet";
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> FEMALE_INFANT;
                case MALE -> MALE_INFANT;
                default -> INFANT;
            };
        } else {
            return switch (psex) {
                case FEMALE -> FEMALE_ADULT;
                case MALE -> MALE_ADULT;
                default -> ADULT;
            };
        }
    }


    private String atIsoAgeExact(PhenopacketAge ppktAge) {
        Iso8601Age iso8601Age = (Iso8601Age) ppktAge;
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        if (y > 10) {
            return String.format("%d Jahre", y);
        } else if (y > 0) {
            if (m > 1) {
                return String.format("%d Jahre und %d Monate", y, m);
            } else if (m == 1) {
                return String.format("%d Jahre und ein Monat", y);
            } else {
                return String.format("%d Jahre", y);
            }
        } else if (m>0) {
            return String.format("%d Monate y %d Tage", m, d);
        } else {
            return String.format("%d Tage",  d);
        }
     }


    @Override
    public String getIndividualDescription(PpktIndividual individual) {
        Optional<PhenopacketAge> lastExamOpt = individual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        PhenopacketSex psex = individual.getSex();
        if (lastExamOpt.isPresent() && onsetOpt.isPresent()) {
            return onsetAndLastEncounterAvailable(psex, lastExamOpt.get(), onsetOpt.get());
        } else if (lastExamOpt.isPresent()) {
            return lastEncounterAvailable(psex, lastExamOpt.get());
        } else if (onsetOpt.isPresent()) {
            return onsetAvailable(psex, onsetOpt.get());
        } else {
            return ageNotAvailable(psex);
        }
    }


    private String iso8601ToYearMonth(Iso8601Age iso8601Age) {
        if (iso8601Age.getMonths() == 0) {
            return String.format("de %d años", iso8601Age.getYears());
        } else {
            return String.format("de %d años y %d meses", iso8601Age.getYears(), iso8601Age.getMonths());
        }
    }

    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format("de %d dias", d);
        } else if (d>0){
            return String.format("de %d meses y %d dias", m, d);
        } else {
            return String.format("de %d meses", m);
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
            return "im Neugeborenen Alter";
        } else if (components.size() == 1) {
            return "im Alter von " + components.get(0);
        } else if (components.size() == 2) {
            return "im Alter von  " + components.get(0) + " und " + components.get(1);
        } else {
            return "im Alter von "  + components.get(0) + ", " + components.get(1) +
                    " und " + components.get(2);
        }
    }

    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "in der Fetalperiode";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "im Neugeborenenalter";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "im Säuglingsalter";
        } else if (hpoOnsetTermAge.isChild()) {
            return "in der Kindheit";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "como adolescente";
        } else {
            return "im Erwachsenenalter";
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y>17) {
            return switch (psex) {
                case FEMALE -> String.format("eine %djährige Frau", y);
                case MALE -> String.format("ein %djähriger Mann", y);
                default -> String.format("eine %djährige Person", y);
            };
        } else if (y>9) {
            return switch (psex) {
                case FEMALE -> String.format("una adolescente de %d años", y);
                case MALE -> String.format("un adolescente de %d años", y);
                default -> String.format("un adolescente de %d años", y);
            };
        } else if (y>0) {
            return switch (psex) {
                case FEMALE -> String.format("niña %s", iso8601ToYearMonth(iso8601Age));
                case MALE -> String.format("niño %s", iso8601ToYearMonth(iso8601Age));
                default -> String.format("niño %s", iso8601ToYearMonth(iso8601Age));
            };
        } else if (m>0 || d> 0) {
            return switch (psex) {
                case FEMALE -> String.format("una infante %s", iso8601ToMonthDay(iso8601Age));
                case MALE -> String.format("un infante %s", iso8601ToMonthDay(iso8601Age));
                default -> String.format("un infante %s", iso8601ToMonthDay(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "recien nacida";
                case MALE -> "recien nacido";
                default -> "recien nacido";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> FEMALE_FETUS;
                case MALE -> MALE_FETUS;
                default -> FETUS;
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "una niña recién nacida";
                case MALE -> "un niño recién nacido";
                default -> "un bebe recién nacido";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> FEMALE_INFANT;
                case MALE -> MALE_INFANT;
                default -> INFANT;
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "niña";
                case MALE -> "niño";
                default -> "niño";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "una adolescente femenina";
                case MALE -> "un adolescente masculino";
                default -> "un adolescente";
            };
        }else {
            return switch (psex) {
                case FEMALE -> "eine Frau";
                case MALE -> "ein Mann";
                default -> "eine Person";
            };
        }
    }

    /**
     * A sentence such as The proband was a 39-year old woman who presented at the age of 12 years with
     * HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded. This method returns the phrase that ends with "with"
     * El sujeto era un niño de 1 año y 10 meses que se presentó como recién nacido con un filtrum largo.
     * @param psex
     * @param lastExamAge
     * @param onsetAge
     * @return
     */
    private String onsetAndLastEncounterAvailable(PhenopacketSex psex, PhenopacketAge lastExamAge, PhenopacketAge onsetAge) {
        String individualDescription;
        String onsetDescription;
        if (lastExamAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) lastExamAge;
            individualDescription = iso8601individualDescription(psex, isoAge);
        } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
            individualDescription = hpoOnsetIndividualDescription(psex,hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize last exam age type " + lastExamAge.ageType());
        }
        if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) onsetAge;
            onsetDescription = iso8601AtAgeOf(isoAge);
        } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
            onsetDescription = onsetTermAtAgeOf(hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize onset age type " + onsetAge.ageType());
        }
        return switch (psex) {
            case FEMALE -> String.format("Die Probandin war %s, der sich %s mit den folgenden Symptomen vorgestellt hat: ", individualDescription, onsetDescription);
            default -> String.format("Der Proband war %s, der sich %s mit den folgenden Symptomen vorgestellt hat: ", individualDescription, onsetDescription);
        };
    }


    /**
     * Age at last examination available but age of onset not available
     * The proband was a 39-year old woman who presented with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
     * @param psex
     * @param lastExamAge
     */
    private String lastEncounterAvailable(PhenopacketSex psex, PhenopacketAge lastExamAge) {
        String individualDescription;
        if (lastExamAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) lastExamAge;
            individualDescription = iso8601individualDescription(psex, isoAge);
        } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
            individualDescription = hpoOnsetIndividualDescription(psex,hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize last exam age type " + lastExamAge.ageType());
        }
        return String.format("El paciente era %s quien se presentó con", individualDescription);
    }

    /**
     * Age at last examination not available but age of onset available
     * The proband  presented  at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
     * @param psex
     * @param onsetAge
     * @return
     */
    private String onsetAvailable(PhenopacketSex psex, PhenopacketAge onsetAge) {
        String onsetDescription;
        if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) onsetAge;
            onsetDescription = iso8601AtAgeOf(isoAge);
        } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
            onsetDescription = onsetTermAtAgeOf(hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize onset age type " + onsetAge.ageType());
        }
        return String.format("Der Patient stellte sich %s mit den folgenden Symptomen vor: ", onsetDescription);
    }

    private String ageNotAvailable(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "Die Patientin stellte sich mit den folgenden Symptomen vor: ";
            case MALE -> "Der Patient stellte sich mit den folgenden Symptomen vor: ";
            default -> "Der Patient stellte sich mit den folgenden Symptomen vor: ";
        };
    }

    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "er";
            case MALE -> "sie";
            default -> "die Person";
        };
    }

    @Override
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "im Alter von " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "als Säugling";
                case "Childhood onset" -> "in der Kindheit";
                case "Neonatal onset"  -> "in der neugeborenen Zeit";
                case "Congenital onset" -> "zum Zeitpunkt der Geburt";
                case "Adult onset" -> "im Erwachsenenalter";
                default-> String.format("TODO TODO el %s período", label.replace(" onset", ""));
            };
        } else {
            return ""; // should never get here
        }
    }

  //  @Override
    public String ppktSex(PpktIndividual individual) {
        PhenopacketSex psex = individual.getSex();
        Optional<PhenopacketAge> ageOpt = individual.getAgeAtLastExamination();
        if (ageOpt.isEmpty()) {
            ageOpt = individual.getAgeAtOnset();
        }
        if (ageOpt.isEmpty()) {
            return switch (psex) {
                case FEMALE -> "Frau";
                case MALE -> "Mann";
                default -> "Person";
            };
        }
        PhenopacketAge age = ageOpt.get();
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> "Mädchen";
                case MALE -> "Junge";
                default -> "Kind";
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "weibliches Neugeborenes";
                case MALE -> "männliches Neugeborenes";
                default -> "Neugeborenes";
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> FEMALE_FETUS;
                case MALE -> MALE_FETUS;
                default -> FETUS;
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> FEMALE_INFANT;
                case MALE -> MALE_INFANT;
                default -> INFANT;
            };
        } else {
            return switch (psex) {
                case FEMALE -> "Frau";
                case MALE -> "Mann";
                default -> "Person";
            };
        }
    }


}
