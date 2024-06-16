package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PPKtBuildingBlockGenerator;
import org.monarchinitiative.phenopacket2prompt.output.impl.ProbandDescriptionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProbandDescriptionEnglish extends ProbandDescriptionGenerator {


    private final boolean hasObserved;
    private final boolean hasExcluded;
    private String onsetAgeDescription;
    private String lastExamAgeDescription;


    private String description;

    /**
     * The proband was a <? old> <male/female/individual> who presented <at the age of ?> <with ...>/. <The following were excluded>
     *     This class creates the text up to the word "The proband was [an individual] who presented [at the age of] with" or
     *     "The proband was [an individual] in whom the following features were excluded: ".
     *
     * @param individual
     */
    public ProbandDescriptionEnglish(PpktIndividual individual) {
        super(new PPKtEnglishBuildingBlocks());
        hasObserved = individual.hasObservedPhenotypeFeatureAtOnset();
        hasExcluded = individual.hasExcludedPhenotypeFeatureAtOnset();
        if (individual.annotationCount() == 0) {
            throw new PhenolRuntimeException("No HPO annotations");
        }
        onsetAgeDescription = createOnsetAgeDescription(individual);
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        Optional<PhenopacketAge> lastExamOpt = individual.getAgeAtLastExamination();

        PhenopacketSex psex = individual.getSex();
        if (lastExamOpt.isPresent() && onsetOpt.isPresent()) {
            description = onsetAndLastEncounterAvailable(psex, lastExamOpt.get(), onsetOpt.get());
        } else if (lastExamOpt.isPresent()) {
            description =  lastEncounterAvailable(psex, lastExamOpt.get());
        } else if (onsetOpt.isPresent()) {
            description =  onsetAvailable(psex, onsetOpt.get());
        } else {
            description =  ageNotAvailable(psex);
        }
    }

    private String ageString(PhenopacketAge page, PhenopacketSex psex) {
        if (page.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) page;
            return iso8601individualDescription(psex, isoAge);
        } else if (page.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) page;
            return hpoOnsetIndividualDescription(psex,hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize last exam age type " + page.ageType());
        }
    }

    private String createOnsetAgeDescription(PpktIndividual individual) {
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        if (onsetOpt.isEmpty()) return null;
        return ageString(onsetOpt.get(), individual.getSex());
    }

    /**
     * A sentence such as The proband was a 39-year-old woman who presented at the age of 12 years with
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
            onsetDescription = buildingBlocks.yearsMonthsDaysOld(isoAge.getYears(),
                    isoAge.getMonths(),
                    isoAge.getDays());
        } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
            onsetDescription = onsetTermAtAgeOf(hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize onset age type " + onsetAge.ageType());
        }
        return String.format("%s %s %s %s with",
                buildingBlocks.probandWasA(),
                individualDescription,
                buildingBlocks.whoPresented(),
                onsetDescription);
    }

    /**
     * Age at last examination available but age of onset not available
     * The proband was a 39-year-old woman who presented with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
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
        return String.format("%s %s %s",
                buildingBlocks.probandWasA(),
                individualDescription,
                buildingBlocks.whoPresentedWith());
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
            onsetDescription = buildingBlocks.yearsMonthsDaysOld(isoAge.getYears(), isoAge.getMonths(), isoAge.getDays());
        } else if (onsetAge.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
            HpoOnsetAge hpoOnsetTermAge = (HpoOnsetAge) onsetAge;
            onsetDescription = onsetTermAtAgeOf(hpoOnsetTermAge);
        } else {
            // should never happen
            throw new PhenolRuntimeException("Did not recognize onset age type " + onsetAge.ageType());
        }
        return String.format("The proband presented %s with", onsetDescription);
    }

    private String ageNotAvailable(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> "The proband was a female who presented with";
            case MALE -> "The proband was a male who presented with";
            default -> "The proband presented with";
        };
    }






    private String iso8601individualDescription(PhenopacketSex psex, Iso8601Age iso8601Age) {
        int y = iso8601Age.getYears();
        int m = iso8601Age.getMonths();
        int d = iso8601Age.getDays();
        // if older
        if (iso8601Age.isAdult()) {
            return switch (psex) {
                case FEMALE -> String.format("%s %s", buildingBlocks.yearsOld(y), buildingBlocks.woman());
                case MALE -> String.format("%s %s", buildingBlocks.yearsOld(y), buildingBlocks.man());
                default -> String.format("%s %s", buildingBlocks.yearsOld(y), buildingBlocks.individual());
            };
        } else if (iso8601Age.isJuvenile()) {
            String age = buildingBlocks.yearsOld(y);
            String sex = switch (psex) {
                case FEMALE ->  buildingBlocks.adolescentGirl();
                case MALE -> buildingBlocks.adolescentBoy();
                default -> buildingBlocks.adolescentChild();
            };
            return String.format("%s %s", age, sex);
        } else if (iso8601Age.isChild()) {
            String age = buildingBlocks.yearsOld(y);
            String sex = switch (psex) {
                case FEMALE ->  buildingBlocks.girl();
                case MALE -> buildingBlocks.boy();
                default -> buildingBlocks.child();
            };
            return String.format("%s %s", age, sex);
        } else if (iso8601Age.isInfant()) {
            String age = buildingBlocks.yearsOld(y);
            String sex = switch (psex) {
                case FEMALE ->  buildingBlocks.femaleInfant();
                case MALE -> buildingBlocks.maleInfant();
                default -> buildingBlocks.infant();
            };
            return String.format("%s %s", age, sex);
        } else if (iso8601Age.isCongenital()) {
            String age = buildingBlocks.yearsOld(y);
            String sex = switch (psex) {
                case FEMALE ->  buildingBlocks.newbornGirl();
                case MALE -> buildingBlocks.newbornBoy();
                default -> buildingBlocks.newborn();
            };
            return String.format("%s %s", age, sex);
        }

        else if (y>0) {
            String age = buildingBlocks.yearsMonthsDaysOld(y, m, d);
            String sex = switch (psex) {
                case FEMALE -> buildingBlocks.girl();
                case MALE -> buildingBlocks.boy();
                default -> buildingBlocks.child();
            };
            return String.format("%s %s", age, sex);
        } else if (m>0 || d> 0) {
            String age = buildingBlocks.monthDayOld(iso8601Age.getMonths(), iso8601Age.getDays());
            String sex = switch (psex) {
                case FEMALE -> buildingBlocks.femaleInfant();
                case MALE -> buildingBlocks.maleInfant();
                default -> buildingBlocks.infant();
            };
            return String.format("%s %s", age, sex);
        } else {
            return switch (psex) {
                case FEMALE -> buildingBlocks.newbornGirl();
                case MALE -> buildingBlocks.newbornBoy();
                default -> buildingBlocks.newborn();
            };
        }
    }

    private String hpoOnsetIndividualDescription(PhenopacketSex psex, HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return switch (psex) {
                case FEMALE -> buildingBlocks.femaleFetus();
                case MALE -> buildingBlocks.maleFetus();
                default -> buildingBlocks.fetus();
            };
        } else if (hpoOnsetTermAge.isCongenital()) {
            return switch (psex) {
                case FEMALE -> buildingBlocks.newbornGirl();
                case MALE -> buildingBlocks.newbornBoy();
                default -> buildingBlocks.newborn();
            };
        } else if (hpoOnsetTermAge.isInfant()) {
            return switch (psex) {
                case FEMALE -> buildingBlocks.femaleInfant();
                case MALE -> buildingBlocks.maleInfant();
                default -> buildingBlocks.infant();
            };
        } else if (hpoOnsetTermAge.isChild()) {
            return switch (psex) {
                case FEMALE -> buildingBlocks.girl();
                case MALE -> buildingBlocks.boy();
                default -> buildingBlocks.child();
            };
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return switch (psex) {
                case FEMALE -> buildingBlocks.adolescentGirl();
                case MALE -> buildingBlocks.adolescentBoy();
                default -> buildingBlocks.adolescentChild();
            };
        }else {
            return switch (psex) {
                case FEMALE -> buildingBlocks.woman();
                case MALE -> buildingBlocks.man();
                default -> buildingBlocks.individual();
            };
        }
    }


}
