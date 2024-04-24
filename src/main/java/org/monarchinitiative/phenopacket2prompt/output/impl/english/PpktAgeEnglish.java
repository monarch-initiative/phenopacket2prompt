package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAgeType;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketAgeGenerator;

public class PpktAgeEnglish implements PhenopacketAgeGenerator {
    @Override
    public String age(PhenopacketAge ppktAge) {
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
    }

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
