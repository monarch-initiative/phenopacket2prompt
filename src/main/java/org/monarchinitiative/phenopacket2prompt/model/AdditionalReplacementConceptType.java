package org.monarchinitiative.phenopacket2prompt.model;

public record AdditionalReplacementConceptType(AdditionalConceptType ctype,
                                               String text,
                                               String replacement
                                               ) implements AdditionalConceptI {


    public static AdditionalReplacementConceptType of(String concept, String text, String replacement) {
        AdditionalConceptType act = AdditionalConceptType.of(concept);
        return new AdditionalReplacementConceptType(act, text, replacement);
    }


    @Override
    public String originalText() {
        return text;
    }

    @Override
    public AdditionalConceptType conceptType() {
        return ctype;
    }

    @Override
    public String insertText() {
        return replacement;
    }
}
