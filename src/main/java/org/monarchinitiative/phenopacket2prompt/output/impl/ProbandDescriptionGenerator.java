package org.monarchinitiative.phenopacket2prompt.output.impl;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.model.*;
import org.monarchinitiative.phenopacket2prompt.output.PPKtBuildingBlockGenerator;

import java.util.Optional;

public abstract class ProbandDescriptionGenerator {


    protected final PPKtBuildingBlockGenerator buildingBlocks;

    public ProbandDescriptionGenerator(PPKtBuildingBlockGenerator buildingBlockGenerator) {
        this.buildingBlocks = buildingBlockGenerator;
    }

    /**
     * Uses the strings in the building blocks to create phrases such as "in the fetal period";
     * Note that the strings need to be adapted for each translation.
     * @param hpoOnsetTermAge an HpoTerm representiing onset age
     * @return String representing the same age.
     */
    protected String onsetTermAtAgeOf(HpoOnsetAge hpoOnsetTermAge) {
        if (hpoOnsetTermAge.isFetus()) {
            return  buildingBlocks.inFetalPeriod();
        } else if (hpoOnsetTermAge.isCongenital()) {
            return  buildingBlocks.isCongenital();
        } else if (hpoOnsetTermAge.isInfant()) {
            return buildingBlocks.asInfant();
        } else if (hpoOnsetTermAge.isChild()) {
            return buildingBlocks.inChildhood();
        } else if (hpoOnsetTermAge.isJuvenile()) {
            return buildingBlocks.asAdolescent();
        } else {
            return buildingBlocks.inAdulthoold();
        }
    }

    /**
     * To do -- do we really need an extra function here or can we just use the building block
     * @param iso8601Age
     * @return
     */
    protected String iso8601ToYearMonth(Iso8601Age iso8601Age) {
        if (iso8601Age.getMonths() == 0) {
            return buildingBlocks.yearsOld(iso8601Age.getYears());
        } else {
            return buildingBlocks.yearsMonthsDaysOld(iso8601Age.getYears(), iso8601Age.getMonths(), 0);
        }
    }

    protected String iso8601ToMonthDay(Iso8601Age iso8601Age) {
        return buildingBlocks.monthDayOld(iso8601Age.getMonths(), iso8601Age.getDays());
    }


    /**
     * There are eight cases we need to deal with.
     * (1) has age of onset and age at last exam
     * (2) has age of onset only
     * (3) has age at last exam only
     * (4) no age information available
     * Each of these cases needs to be done for hasObservedHpoAtOnset and not hasObservedHpoAtOnset
     * @param individual the individual (proband, patient) about whom the phenopacket is
     * @return First phrase of the prompt (see within the code for examples)
     */
    protected String individualDescriptionWithSexAndAge(PpktIndividual individual) {
        Optional<PhenopacketAge> lastExamOpt = individual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        PhenopacketSex psex = individual.getSex();
        if (individual.hasObservedPhenotypeFeatureAtOnset()) {
            return individualDescriptionWithSexAndAgeWithObservedHpo(individual);
        } else {
            return individualDescriptionWithSexAndAgeWithoutObservedHpo(individual);
        }

    }

    private String individualDescriptionWithSexAndAgeWithObservedHpo(PpktIndividual individual) {
        Optional<PhenopacketAge> lastExamOpt = individual.getAgeAtLastExamination();
        Optional<PhenopacketAge> onsetOpt = individual.getAgeAtOnset();
        PhenopacketSex psex = individual.getSex();
        if (lastExamOpt.isPresent() && onsetOpt.isPresent()) {
            return onsetAndLastEncounterAvailableWithObservedHpo(psex, lastExamOpt.get(), onsetOpt.get());
        } else if (lastExamOpt.isPresent()) {
            return lastEncounterAvailable(psex, lastExamOpt.get());
        } else if (onsetOpt.isPresent()) {
            return onsetAvailableWithObservedHpo(psex, onsetOpt.get());
        } else {
            return ageNotAvailableWithObservedHpo(psex);
        }
    }

    private String individualDescriptionWithSexAndAgeWithoutObservedHpo(PpktIndividual individual) {
        return "";
    }

    /**
     * Age at last examination not available but age of onset available
     * The proband  presented  at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
     * @param psex
     * @param onsetAge
     * @return
     */
    private String onsetAvailableWithObservedHpo(PhenopacketSex psex, PhenopacketAge onsetAge) {
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
        return String.format("%s %s with",
                buildingBlocks.probandWasA(),
                onsetDescription
        );
    }

    /**
     * e.g. the proband was a female who presented with HPO1, HPO2, and HPO3.
     * This may optionally be followed by [In contrast, HPO4 was excluded.]
     * @param psex sex of the proband
     * @return String representing first presentation.
     */
    private String ageNotAvailableWithObservedHpo(PhenopacketSex psex) {
        return switch (psex) {
            case FEMALE -> String.format("%s %s %s", buildingBlocks.probandWasA(),
                    buildingBlocks.female(), buildingBlocks.whoPresentedWith());
            case MALE -> String.format("%s %s %s", buildingBlocks.probandWasA(),
                            buildingBlocks.male(), buildingBlocks.whoPresentedWith());
            default -> String.format("%s %s %s", buildingBlocks.probandWasA(),
                    buildingBlocks.adult(), buildingBlocks.whoPresentedWith());
        };
    }

    /**
     * A sentence such as The proband was a 39-year-old woman who presented at the age of 12 years with
     * HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded. This method returns the phrase that ends with "with"
     * @param psex
     * @param lastExamAge
     * @param onsetAge
     * @return
     */
    private String onsetAndLastEncounterAvailableWithObservedHpo(PhenopacketSex psex, PhenopacketAge lastExamAge, PhenopacketAge onsetAge) {
        String individualDescription;
        String onsetDescription;
        String ageString;
        if (lastExamAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            Iso8601Age isoAge = (Iso8601Age) lastExamAge;
            ageString = iso8601ToYearMonth(isoAge);
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
        return String.format("The proband was a %s who presented with", individualDescription);
    }


}
