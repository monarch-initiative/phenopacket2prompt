package org.monarchinitiative.phenopacket2prompt.mining;

import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.phenopackets.schema.v2.Phenopacket;

public record CaseBundle(Case caseReport, Phenopacket phenopacket, PpktIndividual individual) {
}
