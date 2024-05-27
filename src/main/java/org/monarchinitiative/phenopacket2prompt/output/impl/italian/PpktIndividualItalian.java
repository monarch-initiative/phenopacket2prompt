package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketIndividualInformationGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualItalian implements PhenopacketIndividualInformationGenerator {



    private static final String FEMALE_FETUS = "un feto femmina";
    private static final String MALE_FETUS = "un feto maschio";
    private static final String FETUS = "un feto";

    private static final String FEMALE_NEWBORN = "una neonata femmina";
    private static final String MALE_NEWBORN = "un neonato maschio";
    private static final String NEWBORN = "un neonato";

    private static final String FEMALE_INFANT = "un'infante femmina";
    private static final String MALE_INFANT = "un infante maschio";
    private static final String INFANT = "un infante";

    private static final String FEMALE_CHILD = "una bambina";
    private static final String MALE_CHILD = "un bambino";
    private static final String CHILD = "un bambino";

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
            case FEMALE -> sex = "una paziente femmina";
            case MALE -> sex = "un paziente maschio";
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
                    return String.format("una donna di %d anni", y);
                } else if (y > 9) {
                    return String.format("una adolescente di %d anni", y);

                } else if (y > 0) {
                    return String.format("una bambina di %d anni", y);
                } else if (m > 0) {
                    return String.format("un'infante femmina di %d mesi", m);
                } else  {
                    return String.format("una neonata di %d giorni", d);
                }
            }
        } else {
            // age is an HPO onset term, we do not have an exact date
        }
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> FEMALE_CHILD;
                case MALE -> MALE_CHILD;
                default -> CHILD; // difficult to be gender neutral
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> FEMALE_NEWBORN;
                case MALE -> MALE_NEWBORN;
                default -> NEWBORN;
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
                case MALE ->  MALE_INFANT;
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
            return String.format("%d anni", y);
        } else if (y > 0) {
            if (m > 1) {
                return String.format("%d anni e %d mesi", y, m);
            } else if (m == 1) {
                return String.format("%d anni e un mese", y);
            } else {
                return String.format("%d anni", y);
            }
        } else if (m>0) {
            return String.format("%d mesi e %d giorni", m, d);
        } else {
            return String.format("%d giorni",  d);
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
            return String.format("di %d anni", iso8601Age.getYears());
        } else {
            return String.format("di %d anni e %d mesi", iso8601Age.getYears(), iso8601Age.getMonths());
        }
    }

    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format("di %d giorni", d);
        } else if (d>0){
            return String.format("di %d mesi e %d giorni", m, d);
        } else {
            return String.format("di %d mesi", m);
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
            components.add(String.format("%d anni", isoAge.getYears()));
        } else if (isoAge.getYears() == 1) {
            components.add("1 anno");
        }
        if (isoAge.getMonths() > 1) {
            components.add(String.format("%d mesi", isoAge.getMonths()));
        } else if (isoAge.getMonths() == 1) {
            components.add("1 mese");
        }
        if (isoAge.getDays()>1) {
            components.add(String.format("%d giorni", isoAge.getDays()));
        } else if (isoAge.getDays()==1) {
            components.add("1 giorno");
        }
        if (components.isEmpty()) {
            return "nel periodo neonatale";
        } else if (components.size() == 1) {
            return "all'età di " + components.get(0);
        } else if (components.size() == 2) {
            return "all'età di " + components.get(0) + " e " + components.get(1);
        } else {
            return "all'età di "  + components.get(0) + ", " + components.get(1) +
                    " e " + components.get(2);
        }
    }

    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "nel periodo fetale";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "nel periodo neonatale";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "nel periodo infantile"; // unsure, to be checked
        } else if (hpoOnsetTermAge.isChild()) {
            return "da bambino"; // check
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "nell'adolescenza";
        } else {
            return "in età adulta";
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y>17) {
            return switch (psex) {
                case FEMALE -> String.format("una donna di %d anni", y);
                case MALE -> String.format("un uomo di %d anni", y);
                default -> String.format("una persona di %d anni", y);
            };
        } else if (y>9) {
            return switch (psex) {
                case FEMALE -> String.format("un'adolescente di %d anni", y);
                case MALE -> String.format("un adolescente di %d anni", y);
                default -> String.format("un adolescente di %d anni", y);
            };
        } else if (y>0) {
            return switch (psex) {
                case FEMALE -> String.format("bambina %s", iso8601ToYearMonth(iso8601Age));
                case MALE -> String.format("bambino %s", iso8601ToYearMonth(iso8601Age));
                default -> String.format("bambino %s", iso8601ToYearMonth(iso8601Age));
            };
        } else if (m>0 || d> 0) {
            return switch (psex) {
                case FEMALE -> String.format("una infante %s", iso8601ToMonthDay(iso8601Age));
                case MALE -> String.format("un infante %s", iso8601ToMonthDay(iso8601Age));
                default -> String.format("un infante %s", iso8601ToMonthDay(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "neonata";
                case MALE -> "neonato";
                default -> "neonato";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "feto femmina";
                case MALE -> "feto maschio";
                default -> "feto";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "una neonata";
                case MALE -> "un neonato";
                default -> "un neonato";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> FEMALE_INFANT;
                case MALE -> "un infante maschio";
                default -> "un infante";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "bambina";
                case MALE -> "bambino";
                default -> "bambino";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "un'adolescente femmina";
                case MALE -> "un adolescente maschio";
                default -> "un adolescente";
            };
        }else {
            return switch (psex) {
                case FEMALE -> "una donna";
                case MALE -> "un uomo";
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
        return String.format("Il soggetto era %s che si è presentato %s con", individualDescription, onsetDescription);
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
        return String.format("Il paziente era %s che si è presentato con", individualDescription);
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
        return String.format("Il paziente si è presentato con %s", onsetDescription);
    }

    private String ageNotAvailable(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "La paziente si è presentata con";
            case MALE -> "Il paziente si è presentato con";
            default -> "Il paziente si è presentato con";
        };
    }

    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "lui";
            case MALE -> "lei";
            default -> "la persona";
        };
    }

    @Override
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "All'età di " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "Durante il periodo infantile";
                case "Childhood onset" -> "Durante l'infanzia";
                case "Neonatal onset"  -> "Durante il periodo neonatale";
                case "Congenital onset" -> "Alla nascita";
                case "Adult onset" -> "Da adulto";
                default-> String.format("Durante il %s periodo", label.replace(" onset", ""));
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
                case FEMALE -> FEMALE_ADULT;
                case MALE -> MALE_ADULT;
                default -> ADULT;
            };
        }
        PhenopacketAge age = ageOpt.get();;
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> FEMALE_CHILD;
                case MALE -> MALE_CHILD;
                default -> CHILD;
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> FEMALE_NEWBORN;
                case MALE -> MALE_NEWBORN;
                default -> NEWBORN;
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
                case FEMALE -> FEMALE_ADULT;
                case MALE -> MALE_ADULT;
                default -> ADULT;
            };
        }
    }


}
