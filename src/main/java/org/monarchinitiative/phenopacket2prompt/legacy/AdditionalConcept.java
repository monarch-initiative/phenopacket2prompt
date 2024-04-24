package org.monarchinitiative.phenopacket2prompt.legacy;

public record AdditionalConcept(AdditionalConceptType ctype,String text) implements AdditionalConceptI {

    public static AdditionalConcept of(String concept, String text) {
        AdditionalConceptType act = AdditionalConceptType.of(concept);
        return new AdditionalConcept(act, text);
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
        return text;
    }
}
