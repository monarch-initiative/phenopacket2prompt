package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.BuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualSpanish implements PPKtIndividualInfoGenerator {


    private final BuildingBlockGenerator bbGenerator;

    public PpktIndividualSpanish() {
        this.bbGenerator = new SpanishBuildingBlocks();
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
            onsetDescription = "No se indicó la edad del inicio de la enfermedad";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);
    }


    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        return String.format("El inicio de la enfermedad ocurrió %s",
                nameOfLifeStage(hpoOnsetTermAge, psex));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        if (hpoOnsetTermAge.isFetus()) {
            return "durante el período fetal";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "en el momento del nacimiento";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "en la infancia temprana"; // infancia temprana is 1-5 yrs!
        } else if (hpoOnsetTermAge.isChild()) {
            return "en la niñez";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "en la adolescencia";
        } else if (hpoOnsetTermAge.isNeonate()) {
            if (psex.equals(PhenopacketSex.FEMALE)) {
                return "como recién nacida";
            } else {
                return "como recién nacido";
            }
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "en la edad joven adulta";
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "en la mediana edad";
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "en la edad adulta avanzada";
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "en la edad adulta";
        } else {
            throw new PhenolRuntimeException("Could not identify Spanish life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }


    private String ymd(Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d %s", y, y > 1 ? "años" : "año"));
        }
        if (m > 0) {
            components.add(String.format("%d %s", m, m > 1 ? "meses" : "mes"));
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, d > 1 ? "días" : "día"));
        }
        if (components.isEmpty()) {
            return "en el primer día de vida";
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            return String.format("en la edad de %s y %s", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("en la edad de %s, %s y %s", components.get(0), components.get(1), components.get(2));
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("El inicio de la enfermedad ocurrió a los %s de edad", ymd(isoAge));
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
        } else if (m > 0) {
            return String.format("%d meses y %d días", m, d);
        } else {
            return String.format("%d días", d);
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            return switch (psex) {
                case FEMALE -> String.format("La paciente era una mujer de %d años", y);
                case MALE -> String.format("El paciente era un hombre de %d años", y);
                default -> String.format("El paciente era una persona de %d años", y);
            };
        } else if (y > 9) {
            return switch (psex) {
                case FEMALE -> String.format("La paciente era una adolescente de %d años", y);
                case MALE -> String.format("El paciente era un adolescente de %d años", y);
                default -> String.format("El paciente era un adolescente de %d años", y);
            };
        } else if (y > 0) {
            return switch (psex) {
                case FEMALE -> String.format("La paciente era una niña de %s", ymd(iso8601Age));
                case MALE -> String.format("El paciente era un niño de %s", ymd(iso8601Age));
                default -> String.format("El paciente era un niño de %s", ymd(iso8601Age));
            };
        } else if (m > 0 || d > 0) {
            return switch (psex) {
                // note that in Spanish infante is up to 5 years
                case FEMALE -> String.format("La paciente era una bebé de %s", ymd(iso8601Age));
                case MALE -> String.format("El paciente era un bebé de %s", ymd(iso8601Age));
                default -> String.format("El paciente era un bebé de %s", ymd(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "La paciente era una recien nacida";
                case MALE -> "El paciente era un recien nacido";
                default -> "El paciente era un recien nacido";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "La paciente era un feto feminino";
                case MALE -> "El paciente era un feto masculino";
                default -> "El paciente era un feto";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una niña recién nacida";
                case MALE -> "El paciente era un niño masculino recién nacido";
                default -> "El paciente era un bebe recién nacido";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una bebé";
                case MALE -> "El paciente era un bebé";
                default -> "El paciente era un bebé";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una niña";
                case MALE -> "El paciente era un niño";
                default -> "El paciente era un niño";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una adolescente femenina";
                case MALE -> "El paciente era un adolescente masculino";
                default -> "El paciente era un adolescente";
            };
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una mujer de mediana edad";
                case MALE -> "El paciente era un hombre de mediana edad";
                default -> "El paciente era un adulto de mediana edad";
            };
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una mujer de edad adulta joven";
                case MALE -> "El paciente era un hombre de edad adulta joven";
                default -> "El paciente era un adulto de edad adulta joven";
            };
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una mujer de edad avanzada";
                case MALE -> "El paciente era un hombre de edad avanzada";
                default -> "El paciente era un adulto de edad avanzada";
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "La paciente era una mujer";
                case MALE -> "El paciente era un hombre";
                default -> "El paciente era un adulto";
            };
        } else {
            throw new PhenolRuntimeException("Did not recognize Spanish HPO Onset term");
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "ella";
            case MALE -> "el";
            default -> "el individuo";
        };
    }

    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "A la edad de " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            if (ppktAge.isFetus()) {
                return "Durante el período fetal";
            } else if (ppktAge.isCongenital()) {
                return "Al nacer";
            } else if (ppktAge.isEmbryo()) {
                return "Durante el período embrionario";
            } else if (ppktAge.isNeonate()) {
                return "Durante el período neonatal";
            } else if (ppktAge.isInfant()) {
                return "Durante la infancia temprana";
            } else if (ppktAge.isChild()) {
                return "Durante la niñez";
            } else if (ppktAge.isJuvenile()) {
                return "Durante la adolescencia";
            } else if (ppktAge.isYoungAdult()) {
                return "En la edad joven adulta";
            } else if (ppktAge.isMiddleAge()) {
                return "En la mediana edad";
            } else if (ppktAge.isLateAdultAge()) {
                return "En la edad adulta avanzada";
            } else if (ppktAge.isAdult()) {
                return "En la edad adulta";
            } else {
                throw new PhenolRuntimeException("Did not recognize onset: " + ppktAge.toString());
            }
        } else {
            throw new PhenolRuntimeException("Bad age type");

        }


    }
}
