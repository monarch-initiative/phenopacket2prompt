package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;

import java.util.List;

public class TextWithoutDiscussionQuery extends AbstractQueryGenerator {

    private final String promptText;

    public TextWithoutDiscussionQuery(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        super(filterer, id, miner, hpo);
        List<String> lines = filterer.getPresentationWithoutDiscussionLines();
        promptText = QUERY_HEADER + caseLines(lines);
    }
    @Override
    public String getQuery() {
        return promptText;
    }
}
