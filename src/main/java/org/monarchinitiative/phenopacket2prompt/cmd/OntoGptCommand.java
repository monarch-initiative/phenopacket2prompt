package org.monarchinitiative.phenopacket2prompt.cmd;


import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportIngestor;
import org.monarchinitiative.phenopacket2prompt.querygen.*;
import picocli.CommandLine;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

import static org.monarchinitiative.phenopacket2prompt.querygen.QueryOutputType.*;


@CommandLine.Command(name = "gpt-time", aliases = {"T"},
        mixinStandardHelpOptions = true,
        description = "Create GPT time-course prompt")
public class OntoGptCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-n", "--nejm"},
            required = true,
            description = "path to directory with NEJM text files")
    private String nejmDirectoryPath;

    @CommandLine.Option(names = {"--hp"},
            description = "path to HP json file")
    private String hpoJsonPath = "data/hp.json";

    @CommandLine.Option(names = {"-o", "--out"},
            description = "path to output dir (created if necessary)")
    private String outDir = "gptOut";

    @CommandLine.Option(names = {"-c", "--case"},
            description = "case ID (just analyze this case)" )
    private String targetCase = null;




    @Override
    public Integer call() {
        // 1. Ingest the NEJM case report texts. Clean up the original text (PDF parse oddities)
        // but otherwise leave the processing for subsequent steps
        Ontology hpo = OntologyLoader.loadOntology(new File(hpoJsonPath));
        NejmCaseReportIngestor nejmIngestor = new NejmCaseReportIngestor(this.nejmDirectoryPath);
        // If run with the --targetCase argument, just the targetCase is processed.
        // if targetCase == null, that means we are processing all files
        if (targetCase != null) {
            nejmIngestor.restrictToTarget(targetCase);
        }
        // key: identifier of PMID; value - lines of text
        Map<String, List<String>> id2lines = nejmIngestor.getId2lines();
        System.out.printf("[INFO] Parsed %d cases.\n", id2lines.size());

        // 2. Create factory objects from the above lines. The factory objects know how to create
        // the various output
        PhenopacketFactoryIngestor ppIngestor = new PhenopacketFactoryIngestor(id2lines, hpo);
        Map<String, QueryPromptFactory> id2timeCourseFactory = ppIngestor.getId2timeCourseFactory();
        System.out.printf("[INFO] Factory map has %d cases.\n", id2timeCourseFactory.size());
        System.out.printf("[INFO] We parsed %d cases.\n", id2lines.entrySet().size());

        // CREATE THE OUTPUT DIRECTORIES IF NEEDED.
        List<QueryOutputType> outputTypes = List.of(TIME_BASED,
                QC,
                TEXT_WITHOUT_DISCUSSION,
                TEXT_PLUS_MANUAL);
        // CREATE THE OUTPUT DIRECTORIES IF NEEDED.
        QueryOutputGenerator outputGenerator = new QueryOutputGenerator(outputTypes, outDir);
        // output individual query prompts to the corresponding directories
        int n_output = 0;
        // OUTOUT THE QUERY FILES
        for (var entry : id2timeCourseFactory.entrySet()) {
            outputGenerator.outputEntry(entry.getKey(), entry.getValue());
            n_output++;
        }
        System.out.printf("We output %d cases.\n", n_output);
        return 0;
    }


}
