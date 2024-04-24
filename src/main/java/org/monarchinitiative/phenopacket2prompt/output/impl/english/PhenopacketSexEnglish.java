package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketSexGenerator;

import java.util.Optional;

public class PhenopacketSexEnglish implements PhenopacketSexGenerator  {
    @Override
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
}
