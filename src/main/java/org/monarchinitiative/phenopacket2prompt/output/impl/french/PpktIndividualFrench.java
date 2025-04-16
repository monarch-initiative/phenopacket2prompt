package org.monarchinitiative.phenopacket2prompt.output.impl.french;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualFrench implements PPKtIndividualInfoGenerator {


    public PpktIndividualFrench() {
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
            var lastExamAge = lastExamOpt.get();
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
        } else {
            individualDescription = switch (psex) {
                case FEMALE -> individualDescription = "La paciente era de sexo femenino y de edad no especificada";
                case MALE -> individualDescription = "El paciente era de sexo masculino y de edad no especificada";
                default -> individualDescription = "El paciente era una persona de sexo y edad no especificados";
            };
        }
        if (onsetOpt.isPresent()) {
            var onsetAge = onsetOpt.get();
            if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) onsetAge;
                onsetDescription = iso8601onsetDescription(isoAge);
            } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
                onsetDescription = hpoOnsetDescription(hpoOnsetTermAge, psex);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + onsetAge.ageType());
            }
        } else {
            onsetDescription = "L'âge d'apparition de la maladie n'a pas été indiqué.";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);
    }


    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        return String.format("Le début de la maladie s'est produit\n" +
                        "\n %s",
                nameOfLifeStage(hpoOnsetTermAge, psex));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        if (hpoOnsetTermAge.isFetus()) {
            return "pendant la période fœtale";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "à la naissance";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "dans la petite enfance"; // infancia temprana is 1-5 yrs!
        } else if (hpoOnsetTermAge.isChild()) {
            return "dans l'enfance";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "à l'adolescence";
        } else if (hpoOnsetTermAge.isNeonate()) {
            if (psex.equals(PhenopacketSex.FEMALE)) {
                return "comme un nouveau-né";
            } else {
                return "comme un nouveau-né";
            }
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "chez le jeune adulte";
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "à l'âge moyen";
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "à un âge avancé";
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "à l'âge adulte";
        } else {
            throw new PhenolRuntimeException("Could not identify French life stage name for HpoOnsetAge " + hpoOnsetTermAge);
        }
    }


    private String ymd(Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d %s", y, y > 1 ? "années" : "ans"));
        }
        if (m > 0) {
            components.add(String.format("%d mois",m)); // mois - same in singular/plural
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, d > 1 ? "jours" : "jour"));
        }
        if (components.isEmpty()) {
            return "au premier jour de la vie";
        } else if (components.size() == 1) {
            return components.getFirst();
        } else if (components.size() == 2) {
            return String.format("à l'âge de %s et %s", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("à l'âge de %s, %s et %s", components.get(0), components.get(1), components.get(2));
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("La maladie est apparue à l'âge de %s", ymd(isoAge));
    }


    private String atIsoAgeExact(PhenopacketAge ppktAge) {
        Iso8601Age iso8601Age = (Iso8601Age) ppktAge;
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        if (y > 10) {
            return String.format("%d ans", y);
        } else if (y > 0) {
            if (m > 0) {
                return String.format("%d ans y %d mois", y, m);
            } else {
                return String.format("%d ans", y);
            }
        } else if (m > 0) {
            return String.format("%d mois et %d jours", m, d);
        } else {
            return String.format("%d jours", d);
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            return switch (psex) {
                case FEMALE -> String.format("Le patient était une femme de %d ans", y);
                case MALE -> String.format("Le patient était un homme de %d ans", y);
                default -> String.format("Le patient était une personne de %d ans", y);
            };
        } else if (y > 9) {
            return switch (psex) {
                case FEMALE -> String.format("La patiente était une jeune fille de %d ans", y);
                case MALE -> String.format("Le patient était un adolescent de %d ans", y);
                default -> String.format("Le patient était un adolescent de %d ans", y);
            };
        } else if (y > 0) {
            return switch (psex) {
                case FEMALE -> String.format("Le patient était une jeune fille de %s", ymd(iso8601Age));
                case MALE -> String.format("Le patient était un garçon de %s", ymd(iso8601Age));
                default -> String.format("Le patient était un enfant de %s", ymd(iso8601Age));
            };
        } else if (m > 0 || d > 0) {
            return switch (psex) {
                // note that in Spanish infante is up to 5 years
                case FEMALE -> String.format("Le patient était un bébé de %s", ymd(iso8601Age));
                case MALE -> String.format("Le patient était un bébé de %s", ymd(iso8601Age));
                default -> String.format("Le patient était un bébé de %s", ymd(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "Le patient était un nouveau-né";
                case MALE -> "Le patient était un nouveau-né";
                default -> "Le patient était un nouveau-né";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "Le patient était un fœtus de sexe féminin";
                case MALE -> "Le patient était un fœtus de sexe masculin";
                default -> "Le patient était un fœtus";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "Le patient était un nouveau-né";
                case MALE -> "Le patient était un nouveau-né";
                default -> "Le patient était un nouveau-né";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "Le patient était un bébé";
                case MALE -> "Le patient était un bébé";
                default -> "Le patient était un bébé";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "La patiente était une fille";
                case MALE -> "Le patient était un enfant";
                default -> "Le patient était un enfant";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "La patiente était une adolescente";
                case MALE -> "Le patient était un adolescent de sexe masculin";
                default -> "Le patient était un adolescent";
            };
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return switch (psex) {
                case FEMALE -> "Le patient était une femme d'âge moyen";
                case MALE -> "Le patient était un homme d'âge moyen";
                default -> "Le patient était un adulte d'âge moyen";
            };
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return switch (psex) {
                case FEMALE -> "Le patient était une jeune femme adulte.";
                case MALE -> "Le patient était un jeune adulte de sexe masculin.";
                default -> "Le patient était un jeune adulte";
            };
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return switch (psex) {
                case FEMALE -> "Le patient était une femme âgée";
                case MALE -> "Le patient était un homme âgé";
                default -> "Le patient était un adulte âgé";
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "Le patient était une femme";
                case MALE -> "Le patient était un homme";
                default -> "Le patient était un adulte";
            };
        } else {
            throw new PhenolRuntimeException("Did not recognize French HPO Onset term");
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "elle";
            case MALE -> "le";
            default -> "l'individu";
        };
    }

    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "À l'âge de " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            if (ppktAge.isFetus()) {
                return "Pendant la période fœtale";
            } else if (ppktAge.isCongenital()) {
                return "A la naissance";
            } else if (ppktAge.isEmbryo()) {
                return "Pendant la période embryonnaire";
            } else if (ppktAge.isNeonate()) {
                return "Pendant la période néonatale";
            } else if (ppktAge.isInfant()) {
                return "Pendant la petite enfance";
            } else if (ppktAge.isChild()) {
                return "Pendant l'enfance";
            } else if (ppktAge.isJuvenile()) {
                return "Pendant l'adolescence";
            } else if (ppktAge.isYoungAdult()) {
                return "Chez le jeune adulte";
            } else if (ppktAge.isMiddleAge()) {
                return "À l'âge moyen";
            } else if (ppktAge.isLateAdultAge()) {
                return "À un âge plus avancé";
            } else if (ppktAge.isAdult()) {
                return "À l'âge adulte";
            } else {
                throw new PhenolRuntimeException("Did not recognize French onset: " + ppktAge);
            }
        } else {
            throw new PhenolRuntimeException("Bad age type");

        }


    }
}
