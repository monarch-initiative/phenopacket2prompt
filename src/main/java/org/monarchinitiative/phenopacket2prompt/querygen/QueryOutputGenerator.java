package org.monarchinitiative.phenopacket2prompt.querygen;

import jdk.jshell.spi.ExecutionControl;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class QueryOutputGenerator {


    private final List<QueryOutputType> outputTypeList;

    private final String outdirPath;

    public QueryOutputGenerator(List<QueryOutputType> outputTypeList, String outdirPath) {
        this.outputTypeList = outputTypeList;
        this.outdirPath = outdirPath;
        createOutputDirectoryIfNeeded(outdirPath);
        for (var qtype: outputTypeList) {
            String outpath = outdirPath + File.separator + QueryOutputType.dirpath(qtype);
            createOutputDirectoryIfNeeded(outpath);
        }
    }
    /** CREATE THE OUTPUT DIRECTORIES IF NEEDED. */
    private void createOutputDirectoryIfNeeded(String outpath) {
        File outdirfile = new File(outpath);
        if (! outdirfile.isDirectory()) {
            boolean dirCreated = outdirfile.mkdir();
            if (!dirCreated) {
                throw new PhenolRuntimeException("Could not create outdirfile directory");
            }
        }
    }


    public void outputEntry(String pmidString, TimeBasedFactory timeBasedFactory) {
        String pmid = pmidString.replace(":", "_"); // avoid colon in file paths
        for (var otype : this.outputTypeList) {
            switch (otype) {
                case TIME_BASED -> outputTimeBased(pmid, timeBasedFactory);
                case QC -> outputQC(pmid, timeBasedFactory);
                case TEXT_WITHOUT_DISCUSSION -> outputTextWithoutDiscussion(pmid, timeBasedFactory);
                case TEXT_PLUS_MANUAL ->  outputTextPlusManual(pmid, timeBasedFactory);
            }
        }
    }

    private void outputTimeBased(String pmidString, TimeBasedFactory timeBasedFactory) {
        String outpath = this.outdirPath + File.separator + pmidString +
                QueryOutputType.dirpath(QueryOutputType.TIME_BASED);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
            writer.write(timeBasedFactory.getPhenopacketBasedQuery());
        } catch (IOException e) {
            throw new PhenolRuntimeException(e.getMessage());
        }
    }

    private void outputQC(String pmidString, TimeBasedFactory timeBasedFactory) {
        String outpath = this.outdirPath + File.separator + pmidString +
                QueryOutputType.dirpath(QueryOutputType.QC);
        String textBased = timeBasedFactory.getPhenopacketTextOnly();
        String original = timeBasedFactory.getOriginalVignetteText();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
            writer.write("### query text### \n\n");
            writer.write(textBased);
            writer.write("\n\n### original text ###\n\n");
            writer.write(original);
        } catch (IOException e) {
            throw new PhenolRuntimeException(e.getMessage());
        }
    }

    private void outputTextPlusManual(String pmidString, TimeBasedFactory timeBasedFactory) {
        throw new PhenolRuntimeException("NOT IMPLEMENTED");
    }

    private void outputTextWithoutDiscussion(String pmidString, TimeBasedFactory timeBasedFactory) {
        throw new PhenolRuntimeException("NOT IMPLEMENTED");
    }

}
