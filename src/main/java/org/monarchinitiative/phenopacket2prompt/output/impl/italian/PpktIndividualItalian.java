package org.monarchinitiative.phenopacket2prompt.output.impl.italian;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualItalian implements PPKtIndividualInfoGenerator {



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
                case FEMALE -> individualDescription = "La paziente era di sesso femminile e di età non specificata";
                case MALE -> individualDescription = "Il paziente era di sesso maschile e di età non specificata";
                default -> individualDescription = "Il paziente era di sesso e di età non specificati";
            };
        }
        if (onsetOpt.isPresent()) {
            var onsetAge = onsetOpt.get();
            if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoAge = (Iso8601Age) onsetAge;
                onsetDescription =  iso8601onsetDescription(isoAge);
            } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
                onsetDescription = hpoOnsetDescription(hpoOnsetTermAge, psex);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + onsetAge.ageType());
            }
        } else {
            onsetDescription = "Non venne indicata l'età dell'inizio della malattia";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);

    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        return String.format("L'inizio della malattia avvenne %s",
                nameOfLifeStage(hpoOnsetTermAge, psex));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge, PhenopacketSex psex) {
        if (hpoOnsetTermAge.isFetus()) {
            return "durante il periodo fetale";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "alla nascita";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "durante l'infanzia";
        } else if (hpoOnsetTermAge.isChild()) {
            return "da bambino";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "nell'adolescenza";
        } else if (hpoOnsetTermAge.isNeonate()) {
            if (psex.equals(PhenopacketSex.FEMALE)) {
                return "da neonata";
            } else {
                return "da nenoato";
            }
        } else if (hpoOnsetTermAge.isYoungAdult()) {
            return "da giovane adulto" ;
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return "in media età" ;
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return "da adulto di età avanzata" ;
        } else if (hpoOnsetTermAge.isAdult()) {
            // d.h. nicht weiter spezifiziert
            return "nell'età adulta" ;
        } else {
            throw new PhenolRuntimeException("Could not identify Italian life stage name for HpoOnsetAge " + hpoOnsetTermAge.toString());
        }
    }


    private String ymd(Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        List<String> components = new ArrayList<>();
        if (y > 0) {
            components.add(String.format("%d %s", y, y > 1 ? "anni" : "anno"));
        }
        if (m > 0) {
            components.add(String.format("%d %s", m, m > 1 ? "mesi" : "mese"));
        }
        if (d > 0) {
            components.add(String.format("%d %s", d, d > 1 ? "giorni" : "giorno"));
        }
        if (components.isEmpty()) {
            return "nel primo giorno di vita";
        } else if (components.size() == 1) {
            return components.get(0);
        } else if (components.size() == 2) {
            return String.format("all'età di %s y %s", components.get(0), components.get(1));
        } else {
            // we must have y,m,d
            return String.format("all'età di %s, %s y %s", components.get(0), components.get(1), components.get(2));
        }
    }

    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("L'inizio della malattia avvenne all'età di %s", ymd(isoAge));
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


    //TODO delete this method when sure no translation in it is needed
    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "nel periodo fetale";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "alla nascita";
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
                case FEMALE -> String.format("La paziente era una donna di %d anni", y);
                case MALE -> String.format("Il paziente era un uomo di %d anni", y);
                default -> String.format("Il paziente era una persona di %d anni", y);
            };
        } else if (y>9) {
            return switch (psex) {
                case FEMALE -> String.format("La paziente era un'adolescente femmina di %d anni", y);
                case MALE -> String.format("Il paziente era un adolescente maschio di %d anni", y);
                default -> String.format("Il paziente era un adolescente di %d anni", y);
            };
        } else if (y>0) {
            return switch (psex) {
                case FEMALE -> String.format("La paziente era una bambina %s", ymd(iso8601Age));
                case MALE -> String.format("Il paziente era un bambino %s", ymd(iso8601Age));
                default -> String.format("Il paziente era un bambino %s", ymd(iso8601Age));
            };
        } else if (m>0 || d> 0) {
            return switch (psex) {
                case FEMALE -> String.format("La paziente era un'infante %s", ymd(iso8601Age));
                case MALE -> String.format("Il paziente era un infante %s", ymd(iso8601Age));
                default -> String.format("Il paziente era un infante %s", ymd(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "La paziente era una neonata";
                case MALE -> "Il paziente era un neonato";
                default -> "Il paziente era un neonato";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "La paziente era un feto femmina";
                case MALE -> "Il paziente era un feto maschio";
                default -> "Il paziente era un feto";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE ->  "La paziente era una neonata";
                case MALE -> "Il paziente era un neonato";
                default -> "Il paziente era un neonato";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE ->  "La paziente era un'infante'";
                case MALE -> "Il paziente era un infante";
                default -> "Il paziente era un infante";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "La paziente era una bambina";
                case MALE -> "Il paziente era un bambino";
                default -> "Il paziente era un bambino";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "La paziente era un'adolescente femmina";
                case MALE -> "Il paziente era un adolescente maschio";
                default -> "Il paziente era un adolescente";
            };
        } else if (hpoOnsetTermAge.isMiddleAge()) {
            return switch (psex) {
                case FEMALE -> "La paziente era una donna di media età";
                case MALE -> "Il paziente era un uomo di media età";
                default -> "Il paziente era un adulto di media età";
            };
        }  else if (hpoOnsetTermAge.isYoungAdult()) {
            return switch (psex) {
                case FEMALE -> "La paziente era una giovane donna adulta";
                case MALE -> "Il paziente era un giovane uomo adulto";
                default -> "Il paziente era una giovane persona adulta";
            };
        } else if (hpoOnsetTermAge.isLateAdultAge()) {
            return switch (psex) {
                case FEMALE -> "La paziente era una donna di età avanzata";
                case MALE -> "Il paziente era un uomo di età avanzata";
                default -> "Il paziente era un adulto di età avanzata";
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> "La paziente era una donna";
                case MALE -> "Il paziente era un uomo";
                default -> "Il paziente era un adulto";
            };
        } else {
            throw new PhenolRuntimeException("Did not recognize Italian HPO Onset term");
        }
    }


    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "lei";
            case MALE -> "lui";
            default -> "il soggetto";
        };
    }

    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
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


}
