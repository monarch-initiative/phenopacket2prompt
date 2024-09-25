package org.monarchinitiative.phenopacket2prompt.mining;

public record Case(String pmid,
                   String title,
                   String disease_id,
                   String disease_label,
                   String caseText) {
}
