package org.monarchinitiative.phenopacket2prompt.output.impl.spanish;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAgeType;
import org.monarchinitiative.phenopacket2prompt.output.PhenopacketAgeGenerator;

public class PpktAgeSpanish implements PhenopacketAgeGenerator {
    @Override
    public String age(PhenopacketAge ppktAge) {
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

    @Override
    public String atAge(PhenopacketAge ppktAge) {
        if (ppktAge.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
            return "A la edad de " + ppktAge.age();
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
}
