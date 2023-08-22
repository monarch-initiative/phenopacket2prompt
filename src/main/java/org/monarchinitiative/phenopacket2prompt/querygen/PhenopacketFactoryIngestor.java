package org.monarchinitiative.phenopacket2prompt.querygen;

import org.monarchinitiative.fenominal.core.TermMiner;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportFromPdfFilterer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhenopacketFactoryIngestor {


    private final Map<String, QueryPromptFactory> id2timeCourseFactory;

    public PhenopacketFactoryIngestor(Map<String, List<String>> id2lines,
                                      Ontology hpo,
                                      boolean useManual,
                                      boolean useDiagnostic,
                                      boolean useTreatment) {
        int validParsedCases = 0;
        this.id2timeCourseFactory = new HashMap<>();
        final TermMiner miner = TermMiner.defaultNonFuzzyMapper(hpo);
        for (var entry : id2lines.entrySet()) {
            String caseNameAsPmid = entry.getKey();
            System.out.printf("[INFO] Creating prompt for %s.\n", caseNameAsPmid);
            try {
                NejmCaseReportFromPdfFilterer filterer = new NejmCaseReportFromPdfFilterer(caseNameAsPmid, entry.getValue());
                if (!filterer.validParse()) {
                    System.out.printf("ChatGptFilterer -- %s: Not Valid.\n", entry.getKey());
                    continue;
                }
                QueryPromptFactory factory = new QueryPromptFactory(filterer, caseNameAsPmid, miner, hpo, useManual);
                id2timeCourseFactory.put(caseNameAsPmid, factory);
            } catch (Exception e) {
                System.out.printf("Exception with %s: %s.\n", entry.getKey(), e.getMessage());
                System.exit(1);
            }
            validParsedCases++;
        }
        System.out.printf("[INFO] Factory map has %d cases.\n", id2timeCourseFactory.size());
        System.out.printf("We parsed %d cases, of which %d were valid.\n", id2lines.entrySet().size(), validParsedCases);
    }

    public Map<String, QueryPromptFactory> getId2timeCourseFactory() {
        return id2timeCourseFactory;
    }
}
