package org.monarchinitiative.phenopacket2prompt.querygen.qfactory;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePoint;
import org.monarchinitiative.phenopacket2prompt.querygen.TimePointParser;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TimeBasedPhenopacketOnlyQuery extends AbstractQueryGenerator {


    private final String promptText;

    public TimeBasedPhenopacketOnlyQuery(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo) {
        super(filterer, id, miner, hpo);
        String intro = getPersonIntroduction();
        String phenotext = getPlainPhenopacketText(filterer, id, miner, hpo);
        promptText = String.format("%s%s%s", QUERY_HEADER, intro, phenotext);
    }


    @Override
    public String getQuery() {
        return this.promptText;
    }
}
