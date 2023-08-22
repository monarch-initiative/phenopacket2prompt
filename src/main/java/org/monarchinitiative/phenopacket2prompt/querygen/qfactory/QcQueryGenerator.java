package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;

import java.util.List;

public class QcQueryGenerator extends AbstractQueryGenerator {

    private final String promptText;



    public QcQueryGenerator(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        super(filterer, id, miner, hpo);
        String phenopacketText = getPlainPhenopacketText(filterer, id, miner, hpo);
        List<String> lines = filterer.getPresentationWithoutDiscussionLines();
        String original = caseLines(lines);
        promptText = String.format("### Phenopacket-text ###\n\n%s\n\n###%s###\n\n",
                phenopacketText, original);
    }


    @Override
    public String getQuery() {
        return null;
    }
}
