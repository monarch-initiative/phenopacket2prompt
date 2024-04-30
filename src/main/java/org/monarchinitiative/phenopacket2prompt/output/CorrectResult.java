package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenol.ontology.data.TermId;

public record CorrectResult(String promptFileName, TermId diseaseId, String diseaseLabel) {
}
