package org.monarchinitiative.phenopacket2prompt.querygen;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;
import org.monarchinitiative.phenopacket2prompt.querygen.qfactory.QcQueryGenerator;
import org.monarchinitiative.phenopacket2prompt.querygen.qfactory.TextPlusManualGenerator;
import org.monarchinitiative.phenopacket2prompt.querygen.qfactory.TextWithoutDiscussionQuery;
import org.monarchinitiative.phenopacket2prompt.querygen.qfactory.TimeBasedPhenopacketOnlyQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryPromptFactory {
    Logger LOGGER = LoggerFactory.getLogger(QueryPromptFactory.class);

    /**
     * If the description segment of a time period is less than 5 characters, skip it.
     */
    private final static int MIN_DESCRIPTION_LENGTH = 5;
    private final NejmCaseReportFromPdfFilterer filterer;

    private final TermMiner miner;

    private final Ontology hpo;

    private final String caseId;

    private final String isoAge;

    private final String phenopacketSex;

    private final String person_string;
    private final boolean useManual;


    public QueryPromptFactory(NejmCaseReportFromPdfFilterer filterer, String id, TermMiner miner, Ontology hpo,
                              boolean useManual) {
        this.filterer = filterer;
        this.miner = miner;
        this.hpo = hpo;
        this.phenopacketSex = filterer.getPhenopacketSex();
        this.isoAge = filterer.getIsoAge();
        this.person_string = get_person_string();
        this.caseId = id;
        this.useManual = useManual;
    }

    public String getQuery(QueryOutputType outputType) {
        LOGGER.error("Getting query for {}", outputType.name());
        switch (outputType) {

            case TIME_BASED -> {
                TimeBasedPhenopacketOnlyQuery tbq = new TimeBasedPhenopacketOnlyQuery(filterer, caseId, miner, hpo);
                return tbq.getQuery();
            }
            case TEXT_WITHOUT_DISCUSSION -> {
                TextWithoutDiscussionQuery tbq = new TextWithoutDiscussionQuery(filterer, caseId, miner, hpo);
                return tbq.getQuery();
            }
            case QC -> {
                QcQueryGenerator qcg = new QcQueryGenerator(filterer, caseId, miner, hpo);
                return qcg.getQuery();
            }
            case TEXT_PLUS_MANUAL -> {
                TextPlusManualGenerator tpm = new TextPlusManualGenerator(filterer, caseId, miner, hpo);
                return tpm.getQuery();
            }
        }
        // should never happen
        throw new PhenolRuntimeException("COuld not find querty type");
    }



    private String get_person_string() {
        String sex = this.phenopacketSex.toLowerCase();
        final Pattern AGE_REGEX = Pattern.compile("P(\\d+)Y");
        Matcher m = AGE_REGEX.matcher(this.isoAge);
        if (m.find()) {
            String years = m.group(1);
            return "A " + years + "-year old " + sex;
        }
        final Pattern DAYS_REGEX = Pattern.compile("P0Y(\\d+)D");
        Matcher m2 = DAYS_REGEX.matcher(isoAge);
        if (m2.find()) {
            String years = m2.group(1);
            return "A " + years + "-day old " + sex + " newborn";
        }
        throw new PhenolRuntimeException("Could not extract person");
    }








}
