package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;

public class PhenopacketOnlyQuery extends AbstractQueryGenerator {


    private final String promptText;

    public PhenopacketOnlyQuery(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        super(filterer, id, miner, hpo);
        String intro = getPersonIntroduction();
        String phenotext = getPlainPhenopacketText(filterer, id, miner, hpo);
        promptText = String.format("%s%s", QUERY_HEADER, phenotext);
    }


    @Override
    public String getQuery() {
        return this.promptText;
    }
}
