package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketIndividualInformationGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualEnglish implements PhenopacketIndividualInformationGenerator {

    public PpktIndividualEnglish() {

    }


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

    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "she";
            case MALE -> "he";
            default -> "the individual";
        };
    }


    private String iso8601ToYear(Iso8601Age iso8601Age) {
        return String.format("%d-year old", iso8601Age.getYears());
    }

    private String iso8601ToYearMonth(Iso8601Age iso8601Age) {
        if (iso8601Age.getMonths() == 0) {
            return String.format("%d-year old", iso8601Age.getYears());
        } else {
            return String.format("%d-year, %d-month old", iso8601Age.getYears(), iso8601Age.getMonths());
        }
    }

    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format("%d-day old", d);
        } else if (d>0){
            return String.format("%d-month, %d-day old", m, d);
        } else {
            return String.format("%d-month old", m, d);
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
            components.add(String.format("%d years", isoAge.getYears()));
        } else if (isoAge.getYears() == 1) {
            components.add("1 year");
        }
        if (isoAge.getMonths() > 1) {
            components.add(String.format("%d months", isoAge.getMonths()));
        } else if (isoAge.getMonths() == 1) {
            components.add("1 month");
        }
        if (isoAge.getDays()>1) {
            components.add(String.format("%d days", isoAge.getDays()));
        } else if (isoAge.getDays()==1) {
            components.add("1 day");
        }
        if (components.isEmpty()) {
            return "as a newborn";
        } else if (components.size() == 1) {
            return "at the age of " + components.get(0);
        } else if (components.size() == 2) {
            return "at the age of " + components.get(0) + " and " + components.get(1);
        } else {
            return "at the age of " + components.get(0) + "m " + components.get(1) +
                    ", and " + components.get(2);
        }
    }

    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "in the fetal period";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "as a newborn";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "as an infant";
        } else if (hpoOnsetTermAge.isChild()) {
            return "in childhood";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "as an adolescent";
        } else {
            return "in adulthood";
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y>17) {
            return switch (psex) {
                case FEMALE -> String.format("%d-year old woman", y);
                case MALE -> String.format("%d-year old man", y);
                default -> String.format("%d-year old individual", y);
            };
        } else if (y>9) {
            return switch (psex) {
                case FEMALE -> String.format("%d-year old adolescent female", y);
                case MALE -> String.format("%d-year old adolescent male", y);
                default -> String.format("%d-year old adolescent", y);
            };
        } else if (y>0) {
            return switch (psex) {
                case FEMALE -> String.format("%s girl", iso8601ToYearMonth(iso8601Age));
                case MALE -> String.format("%s boy", iso8601ToYearMonth(iso8601Age));
                default -> String.format("%s child", iso8601ToYearMonth(iso8601Age));
            };
        } else if (m>0 || d> 0) {
            return switch (psex) {
                case FEMALE -> String.format("%s baby girl", iso8601ToMonthDay(iso8601Age));
                case MALE -> String.format("\"%s baby boy", iso8601ToMonthDay(iso8601Age));
                default -> String.format("%s baby", iso8601ToMonthDay(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "newborn girl";
                case MALE -> "newborn boy";
                default -> "newborn";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "female fetus";
                case MALE -> "male fetus";
                default -> "fetus";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "female newborn";
                case MALE -> "male newborn";
                default -> "newborn";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "female infant";
                case MALE -> "male infant";
                default -> "infant";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "girl";
                case MALE -> "boy";
                default -> "child";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "female adolescent";
                case MALE -> "male adolescent";
                default -> "adolescent";
            };
        }else {
            return switch (psex) {
                case FEMALE -> "woman";
                case MALE -> "man";
                default -> "adult";
            };
        }
    }

    /**
     * A sentence such as The proband was a 39-year old woman who presented at the age of 12 years with
     * HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded. This method returns the phrase that ends with "with"
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
        return String.format("The proband was a %s who presented %s with", individualDescription, onsetDescription);
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
        return String.format("The proband was a %s who presented with", individualDescription);
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
        return String.format("The proband presented %s with", onsetDescription, onsetDescription);
    }

    private String ageNotAvailable(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "The proband was a female who presented with";
            case MALE -> "The proband was a male who presented with";
            default -> "The proband presented with";
        };
    }

    private String individualName(PpktIndividual individual) {
        PhenopacketSex psex = individual.getSex();
        Optional<PhenopacketAge> ageOpt = individual.getAgeAtLastExamination();
        if (ageOpt.isEmpty()) {
            ageOpt = individual.getAgeAtOnset();
        }
        if (ageOpt.isEmpty()) {
            return switch (psex) {
                case FEMALE -> "female";
                case MALE -> "male";
                default -> "individual";
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
                case FEMALE -> "female infant";
                case MALE -> "male infant";
                default -> "infant";
            };
        } else {
            return switch (psex) {
                case FEMALE -> "woman";
                case MALE -> "man";
                default -> "individual";
            };
        }
    }



  /*  @Override
    public String individualWithAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return  ppktAge.age() + " old";
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "infant";
                case "Childhood onset" -> "child";
                case "Neonatal onset"  -> "neonate";
                case "Congenital onset" -> "born";
                case "Adult onset" -> "adult";
                default-> String.format("During the %s", label.replace(" onset", ""));
            };
        } else {
            return ""; // should never get here
        }
    }*/

    @Override
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "At an age of " + ppktAge.age();
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "During the infantile period";
                case "Childhood onset" -> "During childhood";
                case "Neonatal onset"  -> "During the neonatal period";
                case "Congenital onset" -> "At birth";
                case "Adult onset" -> "As an adult";
                default-> String.format("During the %s", label.replace(" onset", ""));
            };
        } else {
            return ""; // should never get here
        }
    }


}
