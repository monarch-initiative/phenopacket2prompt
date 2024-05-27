package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketIndividualInformationGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualSpanish implements PhenopacketIndividualInformationGenerator {

    //TODO translate from ita to spanish and edit this file in order to actually use these
    private static final String FEMALE_FETUS = "un feto femenino";
    private static final String MALE_FETUS = "un feto masculino";
    private static final String FETUS = "un feto";

    private static final String FEMALE_NEWBORN = "una niña recién nacida"; // CHECK
    private static final String MALE_NEWBORN = "un neonato maschio";
    private static final String NEWBORN = "un neonato";

    private static final String FEMALE_INFANT = "un bebé femenino";
    private static final String MALE_INFANT = "un bebé masculino";
    private static final String INFANT = "un bebé";

    private static final String FEMALE_CHILD = "una bambina";
    private static final String MALE_CHILD = "un bambino";
    private static final String CHILD = "un bambino";

    private static final String FEMALE_ADOLESCENT = "un'adolescente femmina";
    private static final String MALE_ADOLESCENT = "un adolescente maschio";
    private static final String ADOLESCENT = "un adolescente";

    private static final String FEMALE_ADULT = "una donna";
    private static final String MALE_ADULT = "un uomo";
    private static final String ADULT = "una persona adulta";


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
            case FEMALE -> sex = "una paciente femenina";
            case MALE -> sex = "un paciente masculino";
            default -> sex = "una persona";
        };

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
                    return String.format("una mujer de %d años", y);
                } else if (y > 9) {
                    return String.format("una adolescente de %d años", y);

                } else if (y > 0) {
                    return String.format("una niña de %d años", y);
                } else if (m > 0) {
                    return String.format("una bebe niña de %d meses", m);
                } else  {
                    return String.format("una recien nacida de %d dias de edad", d);
                }
            }
        } else {
            // age is an HPO onset term, we do not have an exact date
        }
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> "una niña";
                case MALE -> "un niño";
                default -> "un niño"; // difficult to be gender neutral
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "una recien nacida";
                case MALE -> "un recien nacido";
                default -> "un recien nacido";
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
                case FEMALE -> "una mujer";
                case MALE -> "un hombre";
                default -> "una persona adulta";
            };
        }
    }


    private String atIsoAgeExact(PhenopacketAge ppktAge) {
        Iso8601Age iso8601Age = (Iso8601Age) ppktAge;
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        if (y > 10) {
            return String.format("%d años", y);
        } else if (y > 0) {
            if (m > 1) {
                return String.format("%d años y %d meses", y, m);
            } else if (m == 1) {
                return String.format("%d años y un mes", y);
            } else {
                return String.format("%d años", y);
            }
        } else if (m>0) {
            return String.format("%d meses y %d días", m, d);
        } else {
            return String.format("%d días",  d);
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
            components.add(String.format("%d años", isoAge.getYears()));
        } else if (isoAge.getYears() == 1) {
            components.add("1 año");
        }
        if (isoAge.getMonths() > 1) {
            components.add(String.format("%d meses", isoAge.getMonths()));
        } else if (isoAge.getMonths() == 1) {
            components.add("1 mes");
        }
        if (isoAge.getDays()>1) {
            components.add(String.format("%d dias", isoAge.getDays()));
        } else if (isoAge.getDays()==1) {
            components.add("1 dia");
        }
        if (components.isEmpty()) {
            return "en el período neonatal";
        } else if (components.size() == 1) {
            return "a la edad de " + components.get(0);
        } else if (components.size() == 2) {
            return "a la edad de " + components.get(0) + " y " + components.get(1);
        } else {
            return "a la edad de "  + components.get(0) + ", " + components.get(1) +
                    " y " + components.get(2);
        }
    }

    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "en el período fetal";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "en el período neonatal";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "como un bebe";
        } else if (hpoOnsetTermAge.isChild()) {
            return "en la niñez";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "como adolescente";
        } else {
            return "en la edad adulta";
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y>17) {
            return switch (psex) {
                case FEMALE -> String.format("mujer de %d años", y);
                case MALE -> String.format("hombre de %d años", y);
                default -> String.format("persona de %d años", y);
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
                case FEMALE ->  "una niña recién nacida";
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
                case FEMALE -> "una mujer";
                case MALE -> "un hombre";
                default -> "un adulto";
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
        return String.format("El sujeto era %s que se presentó %s con", individualDescription, onsetDescription);
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
        return String.format("El paciente se presentó %s con", onsetDescription);
    }

    private String ageNotAvailable(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "La paciente se presentó con";
            case MALE -> "El paciente se presentó con";
            default -> "El paciente se presentó con";
        };
    }

    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "el";
            case MALE -> "ella";
            default -> "la persona";
        };
    }

    @Override
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "A la edad de " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "Durante el período infantil";
                case "Childhood onset" -> "Durante la infancia";
                case "Neonatal onset"  -> "Durante el período neonatal";
                case "Congenital onset" -> "Al nacer";
                case "Adult onset" -> "Como adulto";
                default-> String.format("Durante el %s período", label.replace(" onset", ""));
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
                case FEMALE -> "mujer";
                case MALE -> "hombre";
                default -> "individuo";
            };
        }
        PhenopacketAge age = ageOpt.get();;
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> "girl";
                case MALE -> "boy";
                default -> "child";
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "female newborn";
                case MALE -> "male newborn";
                default -> "newborn";
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> "female fetus";
                case MALE -> "male fetus";
                default -> "fetus";
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> FEMALE_INFANT;
                case MALE -> "un infante masculino";
                default -> "un infante";
            };
        } else {
            return switch (psex) {
                case FEMALE -> "mujer";
                case MALE -> "hombre";
                default -> "adulto";
            };
        }
    }


}
