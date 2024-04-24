package org.monarchinitiative.phenopacket2prompt.legacy;


/**
 * This interface represents the concepts used for manual replacements, e.g.
 * <pre>
 * eye redness:PHENOTYPE
 * conjunctival injection:PHENOTYPE
 * lungs were clear on auscultation:EXCLUDE:Abnormal breath sound
 * (SARS-CoV-2) RNA was negative:DIAGNOSTICS
 * rapid antigen testing for influenza types A and B was negative:DIAGNOSTICS
 * amoxicillin:TREATMENT
 * sputum had streaks of bright red blood:PHENOTYPE:Hemoptysis
 * developmental dysplasia of the hip:PHENOTYPE
 * patchy airspace opacities:DIAGNOSTICS:predominantly lower lung patchy airspace opacities
 * amoxicillin, acetaminophen, ibuprofen, benzonatate, guaifenesin, and dextro-methorphan:TREATMENT
 * the temperature was 38.5°C:PHENOTYPE
 * the heart rate 124 beats per minute:PHENOTYPE:Tachycardia
 * The body-mass index (the weight in kilograms divided by the square of the height in meters) was 35.9:PHENOTYPE:Obesity
 * </pre>
 * For items with two fields, the original text and the text inserted into our query prompt are the same.
 * For the items with three fields, the last field is used to replace the original text
 */
public interface AdditionalConceptI {

    String originalText();
    AdditionalConceptType conceptType();
    String insertText();
}
