package org.monarchinitiative.phenopacket2prompt.legacy;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

/**
 * The concept type for the strings that we manually match in the original text.
 * <ol>
 * <li>PHENOTYPE: observed phenotypic feature</li>
 * <li>EXCLUDE: excluded phenotypic feature</li>
 * <li>DIAGNOSTICS</li>
 * <li>TREATMENT</li>
 * <li>PMH: past medical history</li>
 * <li>VERBATIM - "other", to be just added</li>
 * </ol>
 */
public enum AdditionalConceptType {
    PHENOTYPE,
    EXCLUDE,
    DIAGNOSTICS,
    TREATMENT,
    PMH,
    FAMILY_HISTORY,
    VERBATIM;


    public static AdditionalConceptType of(String s) {
        String concept = s.toUpperCase();
        return switch (concept) {
            case "PHENOTYPE" ->  PHENOTYPE;
            case "EXCLUDE" ->  EXCLUDE;
            case "DIAGNOSTICS" ->  DIAGNOSTICS;
            case "TREATMENT" -> TREATMENT;
            case "PMH" -> PMH;
            case "FAMILY_HISTORY" -> FAMILY_HISTORY;
            case "VERBATIM" -> VERBATIM;
            default -> throw new PhenolRuntimeException("Unrecognised concept \"" + concept + "\"");
        };
    }
}


