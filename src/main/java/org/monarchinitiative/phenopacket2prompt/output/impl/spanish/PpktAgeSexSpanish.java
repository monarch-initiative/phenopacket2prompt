package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.IndividualInformation;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketAgeSexGenerator;

import java.util.Optional;

public class PpktAgeSexSpanish implements PhenopacketAgeSexGenerator {


    public PpktAgeSexSpanish() {
    }



    public IndividualInformation getInformation(PpktIndividual individual) {
        PhenopacketSex psex = individual.getSex();
        String ageSexAtLastExam = ageAndSexAtLastExamination(individual);



        return new IndividualInformation(psex, ageSexAtLastExam);
    }


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
                } else if (m>0) {
                    return String.format("una bebe niña de %d meses", m);
                } else  {
                    return String.format("una recien nacida %d meses", d);
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
                case FEMALE -> "un feto femenino";
                case MALE -> "un feto masculino";
                default -> "un feto";
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> "un bebé femenino";
                case MALE -> "un bebé masculino";
                default -> "un bebé";
            };
        } else {
            return switch (psex) {
                case FEMALE -> "un mujer";
                case MALE -> "un hombre";
                default -> "una persona adulta";
            };
        }
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


    @Override
    public String individualWithAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return  ppktAge.age() + " old";
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "bebé";
                case "Childhood onset" -> "niño";
                case "Neonatal onset"  -> "neonate";
                case "Congenital onset" -> "recién nacido";
                case "Adult onset" -> "adulto";
                default-> String.format("During the %s", label.replace(" onset", ""));
            };
        } else {
            return ""; // should never get here
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
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "A la edad de " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "Durante el periodo infantil";
                case "Childhood onset" -> "Durante la infancia";
                case "Neonatal onset"  -> "Durante el periodo neonatal";
                case "Congenital onset" -> "Al nacer";
                case "Adult onset" -> "Como adulto";
                default-> String.format("Durante el %s periodo", label.replace(" onset", ""));
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

    @Override
    public String ppktSex() {
        return "";
    }
}
