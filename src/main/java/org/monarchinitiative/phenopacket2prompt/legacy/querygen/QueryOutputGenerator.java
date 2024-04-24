package org.monarchinitiative.phenopacket2prompt.legacy.querygen;

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
            String outpath = outdirPath + File.separator + QueryOutputType.outputString(qtype);
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


    public void outputEntry(String pmidString, QueryPromptFactory timeBasedFactory) {
        String pmid = pmidString.replace(":", "_"); // avoid colon in file paths
        for (var otype : this.outputTypeList) {
            String outputString = QueryOutputType.outputString(otype);
            String outpath = this.outdirPath + File.separator + outputString + File.separator +
                    pmid + "-" + outputString + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outpath))) {
                writer.write(timeBasedFactory.getQuery(otype));
            } catch (IOException e) {
                throw new PhenolRuntimeException(e.getMessage());
            }
        }
    }




}
