package org.monarchinitiative.phenopacket2prompt.output.impl.dutch;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PpktIndividualDutch implements PPKtIndividualInfoGenerator {


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
            case FEMALE -> sex = "zij";
            case MALE -> sex = "hij";
            default -> sex = "de persoon";
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
                    return String.format("vrouw van %d jaar oud", y);
                } else if (y > 9) {
                    return String.format("adolescente vrouw van %d jaar oud", y);
                } else if (y > 0) {
                    return String.format("meisje van %d jaar oud", y);
                } else if (m>0) {
                    return String.format("baby van %d maanden oud", m);
                } else  {
                    return String.format("pasgeboren vrouwelijke baby van %d dagen oud", d);
                }
            }
        } else {
            // age is an HPO onset term, we do not have an exact date
        }
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> "meisje";
                case MALE -> "jongetje";
                default -> "kind"; // difficult to be gender neutral
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "pasgeboren meisje";
                case MALE -> "pasgeboren jongetje";
                default -> "pasgeborene";
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke foetus";
                case MALE -> "mannelijke foetus";
                default -> "foetus";
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke baby";
                case MALE -> "mannelijke baby";
                default -> "baby";
            };
        } else {
            return switch (psex) {
                case FEMALE -> "vrouw";
                case MALE -> "man";
                default -> "volwassene";
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
                case FEMALE -> "vrouw";
                case MALE -> "man";
                default -> "individu";
            };
        }
        PhenopacketAge age = ageOpt.get();;
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> "meisje";
                case MALE -> "jongetje";
                default -> "kind";
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "pasgeboren vrouwelijke baby";
                case MALE -> "pasgeboren mannelijke baby";
                default -> "pasgeborene";
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke foetus";
                case MALE -> "mannelijke foetus";
                default -> "foetus";
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke baby";
                case MALE -> "mannelijke baby";
                default -> "baby";
            };
        } else {
            return switch (psex) {
                case FEMALE -> "vrouw";
                case MALE -> "man";
                default -> "individu";
            };
        }
    }


   /* @Override
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
*/

    private String atIsoAgeExact(PhenopacketAge ppktAge) {
        Iso8601Age iso8601Age = (Iso8601Age) ppktAge;
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();

        if (y > 10) {
            return String.format("%d jaar oud", y);
        } else if (y > 0) {
            if (m > 1) {
                return String.format("%d jaar en %d maanden oud", y, m);
            } else if (m == 1) {
                return String.format("%d jaar en één maand oud", y);
            } else {
                return String.format("%d jaar oud", y);
            }
        } else if (m>0) {
            return String.format("%d maanden en %d dagen oud", m, d);
        } else {
            return String.format("%d dagen oud",  d);
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
            return String.format("van %d jaar oud", iso8601Age.getYears());
        } else {
            return String.format("van %d jaar en %d maanden", iso8601Age.getYears(), iso8601Age.getMonths());
        }
    }

    private String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        if (m == 0) {
            return String.format("van %d dagen oud", d);
        } else if (d>0){
            return String.format("van %d maanden en %d dagen oud", m, d);
        } else {
            return String.format("van %d maanden oud", m);
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
            components.add(String.format("%d jaar", isoAge.getYears()));
        } else if (isoAge.getYears() == 1) {
            components.add("één jaar oud");
        }
        if (isoAge.getMonths() > 1) {
            components.add(String.format("%d maanden", isoAge.getMonths()));
        } else if (isoAge.getMonths() == 1) {
            components.add("één maand oud");
        }
        if (isoAge.getDays()>1) {
            components.add(String.format("%d dagen", isoAge.getDays()));
        } else if (isoAge.getDays()==1) {
            components.add("één dag");
        }
        if (components.isEmpty()) {
            return "als pasgeborene";
        } else if (components.size() == 1) {
            return "op de leeftijd van " + components.getFirst();
        } else if (components.size() == 2) {
            return "op de leeftijd van " + components.get(0) + " en " + components.get(1);
        } else {
            return "op de leeftijd van " + components.get(0) + ". " + components.get(1) +
                    ", en " + components.get(2);
        }
    }

    private String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  "in de foetale periode";
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  "in de neonatale periode";
        } else if (hpoOnsetTermAge.isInfant()) {
            return "als baby";
        } else if (hpoOnsetTermAge.isChild()) {
            return "als kind";
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "als adolescent";
        } else {
            return "als volwassene";
        }
    }


    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y>17) {
            return switch (psex) {
                case FEMALE -> String.format("vrouw van %d jaar oud", y);
                case MALE -> String.format("man van %d jaar oud", y);
                default -> String.format("persoon van %d jaar oud", y);
            };
        } else if (y>9) {
            return switch (psex) {
                case FEMALE -> String.format("vrouwelijke adolescent van %d jaar oud", y);
                case MALE -> String.format("mannelijke adolescent van %d jaar oud", y);
                default -> String.format("adolescent van %d jaar oud", y);
            };
        } else if (y>0) {
            return switch (psex) {
                case FEMALE -> String.format("meisje %s", iso8601ToYearMonth(iso8601Age));
                case MALE -> String.format("jongetje %s", iso8601ToYearMonth(iso8601Age));
                default -> String.format("kind %s", iso8601ToYearMonth(iso8601Age));
            };
        } else if (m>0 || d> 0) {
            return switch (psex) {
                case FEMALE -> String.format("vrouwelijke baby %s", iso8601ToMonthDay(iso8601Age));
                case MALE -> String.format("mannelijke baby %s", iso8601ToMonthDay(iso8601Age));
                default -> String.format("baby %s", iso8601ToMonthDay(iso8601Age));
            };
        } else {
            return switch (psex) {
                case FEMALE -> "pasgeboren meisje";
                case MALE -> "pasgeboren jongetje";
                default -> "pasgeborene";
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke foetus";
                case MALE -> "mannelijke foetus";
                default -> "foetus";
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "pasgeboren meisje";
                case MALE -> "pasgeboren jongetje";
                default -> "pasgeborene";
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke baby";
                case MALE -> "mannelijke baby";
                default -> "baby";
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> "meisje";
                case MALE -> "jongetje";
                default -> "kind";
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke adolescent";
                case MALE -> "mannelijke adolescent";
                default -> "adolescent";
            };
        }else {
            return switch (psex) {
                case FEMALE -> "vrouw";
                case MALE -> "man";
                default -> "volwassene";
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
        return String.format("De proband was een %s die %s presenteerde met", individualDescription, onsetDescription);
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
        return String.format("De proband was een %s die presenteerde met", individualDescription);
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
        return String.format("De proband presenteerde %s met", onsetDescription);
    }

    private String ageNotAvailable(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "De proband was een vrouw die presenteerde met";
            case MALE -> "De proband was een man die presenteerde met";
            default -> "De proband presenteerde met";
        };
    }

    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "zij";
            case MALE -> "hij";
            default -> "de persoon";
        };
    }

    @Override
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "Op de leeftijd van " + atIsoAgeExact(ppktAge);
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            String label = ppktAge.age(); // something like "Infantile onset"
            return switch (label) {
                case "Infantile onset" -> "Tijdens de infantiele periode";
                case "Childhood onset" -> "Tijdens de jeugd";
                case "Neonatal onset"  -> "Tijdens de neonatale periode";
                case "Congenital onset" -> "Bij geboorte";
                case "Adult onset" -> "Op volwassen leeftijd";
                default-> String.format("Tijdens de %s periode", label.replace(" onset", ""));
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
                case FEMALE -> "vrouw";
                case MALE -> "man";
                default -> "individu";
            };
        }
        PhenopacketAge age = ageOpt.get();;
        if (age.isChild()) {
            return switch (psex) {
                case FEMALE -> "meisje";
                case MALE -> "jongetje";
                default -> "kind";
            };
        } else if (age.isCongenital()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke pasgeborene";
                case MALE -> "mannelijke pasgeborene";
                default -> "pasgeborene";
            };
        } else if (age.isFetus()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke foetus";
                case MALE -> "mannelijke foetus";
                default -> "foetus";
            };
        } else if (age.isInfant()) {
            return switch (psex) {
                case FEMALE -> "vrouwelijke baby";
                case MALE -> "mannelijke baby";
                default -> "baby";
            };
        } else {
            return switch (psex) {
                case FEMALE -> "vrouw";
                case MALE -> "man";
                default -> "individu";
            };
        }
    }


}
