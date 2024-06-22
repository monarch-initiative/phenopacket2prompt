package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PPKtBuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.PPKtIndividualInfoGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * This class produces the very first sentence of the prompt, e.g.,
 * <pre>
 * "The proband was a 22-year-old woman."
 * </pre>
 * <p>
 * There are several possible cases to take into account, depending on whether we have the
 * the age at last presentation and whether the sex is specified.
 * The proband was a female.
 * The proband was 33-years old.
 * The proband was a 22-year-old man.
 * The proband was a male fetus.
 * If neither age nor sex is available, we write
 * "The proband was an individual."
 * </p>
 * <p>
 * The entry point to the generation of this sentence is the function {@code #getIndividualDescription}.
 * </p>
 */
public class PpktIndividualEnglish implements PPKtIndividualInfoGenerator {

    private final PPKtBuildingBlockGenerator buildBlocks;

    public PpktIndividualEnglish() {
        buildBlocks = new PPKtEnglishBuildingBlocks();
    }

    /**
     * We begin our description with a sentence
     *
     * @param individual
     * @return
     */
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
                individualDescription =  iso8601individualDescription(psex, isoAge);
            } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
                individualDescription = hpoOnsetIndividualDescription(psex, hpoOnsetTermAge);
            } else {
                // should never happen
                throw new PhenolRuntimeException("Did not recognize last exam age type " + lastExamAge.ageType());
            }
        }  else {
            individualDescription =  switch (psex) {
                case FEMALE -> buildBlocks.probandWasAFemale();
                case MALE -> buildBlocks.probandWasAMale();
                default -> buildBlocks.probandWasAnIndividual();
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
            onsetDescription = "Disease onset was not specified";
        }
        return String.format("%s. %s.", individualDescription, onsetDescription);

    }



    private String iso8601onsetDescription(Iso8601Age isoAge) {
        return String.format("Disease onset occurred when the proband was %s",
                buildBlocks.yearsMonthsDaysOld(isoAge.getYears(), isoAge.getMonths(), isoAge.getDays()));
    }

    private String hpoOnsetDescription(HpoOnsetAge hpoOnsetTermAge) {
        return String.format("Disease onset occurred when the proband was %s",
            nameOfLifeStage(hpoOnsetTermAge));
    }

    private String nameOfLifeStage(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return "a " + buildBlocks.fetus();
        } else if (hpoOnsetTermAge.isCongenital()) {
            return "a " + buildBlocks.newborn();
        } else if (hpoOnsetTermAge.isInfant()) {
            return "an " + buildBlocks.infant();
        } else if (hpoOnsetTermAge.isChild()) {
            return "a " + buildBlocks.child();
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return "an " +buildBlocks.adolescentChild();
        } else if (hpoOnsetTermAge.isNeonate()) {
            return "a " +buildBlocks.newborn();
        } else if (hpoOnsetTermAge.isAdult()) {
            return "an " +buildBlocks.adult();
        } else {
            throw new PhenolRuntimeException("Could not identify life stage name for HpoOnsetAge " + hpoOnsetTermAge);
        }
    }



    /**
     * NOTE THIS SHOULD BE REMOVED
     *
     * @param psex
     * @return
     */
    @Override
    public String heSheIndividual(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> buildBlocks.she();
            case MALE -> buildBlocks.he();
            default -> buildBlocks.theIndividual();
        };
    }


    /**
     * Create a phrase such as "at the age of 7 years, 4 months, and 2 days"
     * Leave out the months and days if they are zero.
     *
     * @param isoAge
     * @return
     */
    private String iso8601AtAgeOf(Iso8601Age isoAge) {
        List<String> components = new ArrayList<>();

        if (isoAge.getYears() > 0) {
            components.add(buildBlocks.years(isoAge.getYears()));
        }
        if (isoAge.getMonths() > 0) {
            components.add(buildBlocks.months(isoAge.getMonths()));
        }
        if (isoAge.getDays() > 0) {
            components.add(buildBlocks.months(isoAge.getDays()));
        }
        if (components.isEmpty()) {
            return buildBlocks.asNewborn();
        } else if (components.size() == 1) {
            return buildBlocks.atTheAgeOf() + " " + components.getFirst();
        } else if (components.size() == 2) {
            return buildBlocks.atTheAgeOf() + " " + components.get(0) + " and " + components.get(1);
        } else {
            return buildBlocks.atTheAgeOf() + " " + components.get(0) + ", " + components.get(1) +
                    ", and " + components.get(2);
        }
    }



    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (y > 17) {
            String age = buildBlocks.yearsOld(y);
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.woman());
                case MALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.man());
                default -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.individual());
            };
        } else if (y > 9) {
            String age = buildBlocks.yearsOld(y);
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.adolescentGirl());
                case MALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.adolescentBoy());
                default -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.adolescentChild());
            };
        } else if (y > 0) {
            String age = buildBlocks.yearsMonthsDaysOld(y, m, d);
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.girl());
                case MALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.boy());
                default -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.child());
            };
        } else if (m > 0 || d > 0) {
            String age = buildBlocks.monthDayOld(m, d);
            return switch (psex) {
                case FEMALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.femaleInfant());
                case MALE -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.maleInfant());
                default -> String.format("%s %s %s", buildBlocks.probandWasA(), age, buildBlocks.infant());
            };
        } else {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newbornGirl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newbornBoy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newborn());
            };
        }
    }


    private String iso8601individualDescriptionAsA(PhenopacketSex psex, Iso8601Age iAge) {
        String onsetWithSex = iso8601individualDescription(psex, iAge);
        return String.format("as a %s", onsetWithSex);
    }

    private String hpoOnsetAsA(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        String onsetWithSex = hpoOnsetIndividualDescription(psex, hpoOnsetTermAge);
        final Set<Character> vowels = Set.of('A', 'E', 'I', 'O', 'U');
        if (vowels.contains(onsetWithSex.charAt(0))) {
            return String.format("as an %s", onsetWithSex);
        } else {
            return String.format("as a %s", onsetWithSex);
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.femaleFetus());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.maleFetus());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.fetus());
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newbornGirl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newbornBoy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.newborn());
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.femaleInfant());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.maleInfant());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.infant());
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.girl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.boy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.child());
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adolescentGirl());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adolescentBoy());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adolescentChild());
            };
        } else if (hpoOnsetTermAge.isAdult()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.woman());
                case MALE -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.male());
                default -> String.format("%s %s", buildBlocks.probandWasA(), buildBlocks.adult());
            };
        } else {
            throw new PhenolRuntimeException("Could not find HPO onset type " + hpoOnsetTermAge.toString());
        }
    }

    /**
     * A sentence such as The proband was a 39-year-old woman who presented at the age of 12 years with
     * HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded. This method returns the phrase that ends with "with"
     *
     * @param psex
     * @param lastExamAge
     * @param onsetAge
     * @return

    private String onsetAndLastEncounterAvailable(PhenopacketSex psex,
                                                  PhenopacketAge lastExamAge,
                                                  PhenopacketAge onsetAge) {
        String individualDescription;
        String onsetDescription;
        if (lastExamAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) lastExamAge;
            individualDescription = iso8601individualDescription(psex, isoAge);
        } else if (lastExamAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) lastExamAge;
            individualDescription = hpoOnsetAsA(psex, hpoOnsetTermAge);
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
        return String.format("%s %s %s %s:",
                buildBlocks.probandWasA(),
                individualDescription,
                buildBlocks.inWhomManifestationsWereExcluded(),
                onsetDescription);

    }
     */

    /**
     * Age at last examination available but age of onset not available
     * The proband was a 39-year-old woman who presented with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
     *
     * @param psex
     * @param lastExamAge

    private String lastEncounterAvailable(PhenopacketSex psex,
                                          PhenopacketAge lastExamAge) {
        String individualDescription;
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

        return String.format("%s %s %s",
                buildBlocks.probandWasA(),
                individualDescription,
                buildBlocks.inWhomManifestationsWereExcluded());

    }*/

    /**
     * Age at last examination not available but age of onset available
     * The proband  presented  at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
     *
     * @param psex     sex of the proband
     * @param onsetAge age at onset of disease
     * @return

    private String onsetAvailable(PhenopacketSex psex,
                                  PhenopacketAge onsetAge) {
        String onsetDescription;
        String individualDescription;
        if (onsetAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) onsetAge;
            individualDescription = iso8601individualDescriptionAsA(psex, isoAge);
        } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
            individualDescription = hpoOnsetAsA(psex, hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize onset age type " + onsetAge.ageType());
        }

        return String.format("%s %s %s",
                buildBlocks.probandNoAgePresented(),
                individualDescription,
                buildBlocks.inWhomManifestationsWereExcluded());


    }*/

    /**
     * This method is called if we have no information at all about the age of the proband
     *
     * @param psex Sex of the proband
     * @return A string such as  "The proband was a female who presented with";
     */
    private String ageNotAvailable(PhenopacketSex psex) {

            return switch (psex) {
                case FEMALE -> buildBlocks.probandWasAFemale();
                case MALE -> buildBlocks.probandWasAMale();
                default -> buildBlocks.probandWasAnIndividual();
            };

    }

    /*
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
                case FEMALE -> buildBlocks.girl();
                case MALE -> buildBlocks.boy();
                default -> buildBlocks.child();
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
                case FEMALE -> buildBlocks.woman();
                case MALE -> buildBlocks.man();
                default -> buildBlocks.inAdulthoold();
            };
        }
    }
*/


    @Override
    public String atAgeForVignette(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "At an age of " + ppktAge.age();
        } else if (ppktAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            if (ppktAge.isFetus()) {
                return buildBlocks.duringFetal();
            } else if (ppktAge.isCongenital()) {
                return buildBlocks.atBirth();
            } else if (ppktAge.isNeonate()) {
                return buildBlocks.asNeonate();
            } else if (ppktAge.isInfant()) {
                return buildBlocks.asInfant();
            } else if (ppktAge.isChild()) {
                return buildBlocks.inChildhood();
            } else if (ppktAge.isJuvenile()) {
                return buildBlocks.asAdolescent();
            } else if (ppktAge.isYoungAdult()) {
                return buildBlocks.asYoungAdult();
            } else if (ppktAge.isMiddleAge()) {
                return buildBlocks.asMiddleAge();
            } else if (ppktAge.isLateAdultAge()) {
                return buildBlocks.asLateOnset();
            } else if (ppktAge.isAdult()) {
                return buildBlocks.asAdult();
            } else {
                throw new PhenolRuntimeException("Did not recognize onset: " + ppktAge.toString());
            }
        } else {
            throw new PhenolRuntimeException("Bad age type");
        }
    }


}
