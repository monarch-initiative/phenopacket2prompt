package org.monarchinitiative.phenopacket2prompt.model;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

public enum AdditionalConceptType {
    PHENOTYPE,
    EXCLUDE,
    DIAGNOSTICS,
    TREATMENT,
    VERBATIM;


    public static AdditionalConceptType of(String s) {
        String concept = s.toUpperCase();
        return switch (concept) {
            case "PHENOTYPE" ->  PHENOTYPE;
            case "EXCLUDE" ->  EXCLUDE;
            case "DIAGNOSTICS" ->  DIAGNOSTICS;
            case "TREATMENT" -> TREATMENT;
            case "VERBATIM" -> VERBATIM;
            default -> throw new PhenolRuntimeException("Unrecognised concept \"" + concept + "\"");
        };
    }
}


