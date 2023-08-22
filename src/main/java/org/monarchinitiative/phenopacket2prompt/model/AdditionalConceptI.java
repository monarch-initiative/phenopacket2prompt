package org.monarchinitiative.phenopacket2prompt.model;

public interface AdditionalConceptI {

    String originalText();
    AdditionalConceptType conceptType();
    String insertText();
}
