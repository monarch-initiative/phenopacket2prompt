package org.monarchinitiative.phenopacket2prompt.nejm;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenopacket2prompt.nejm.NejmCaseReportImporter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Parse the text files with the parsed PDF articles. The ingestion code corrects various parsing
 * errors using the
 */
public class NejmCaseReportIngestor {

    /**
     * The case reports are not valid differential diagnostic exercises
     * 34496178: Discussing HIV prophylaxis
     *  33730458: primarily imaging, not enough text in initial presentation to be a fair comparison
     *  PMID:33913642: scant information in case presented by first discussant. Imaging plays prominent role in case.
     *  PMID:34587390: scant information in case presented by first discussant. Imaging plays prominent role in case.
     *  PMID:34670047: scant information in case presented by first discussant. Imaging plays prominent role in case.
     */
    private final Set<String> INVALID_CASE_REPORTS = Set.of("PMID:34496178", "PMID:33730458", "PMID:33913642",
            "PMID:34587390", "PMID:34670047");


    private final Ontology hpo;
    /**  key: identifier of PMID; value - lines of text */
    private final  Map<String, List<String>> id2lines;


    public NejmCaseReportIngestor(String nejmDirectory, Ontology hpo) {
        this.hpo = hpo;
        this.id2lines = new HashMap<>();
        init(nejmDirectory);
    }

    private void init(String nejmDirectory) {
        // raw text from PDF parsing of the NEJM cases
        Set<String> nejmCaseReportFiles = listNejmCaseReportFiles(nejmDirectory);
        for (String fname : nejmCaseReportFiles) {
            File fpath = new File(nejmDirectory + File.separator + fname);
            NejmCaseReportImporter importer = new NejmCaseReportImporter(fpath);
            List<String> lines = importer.getLines();
            String caseNameAsPmid = getCaseNameAsPmid(fname);
            // we skip five of the 80 raw files for reasons listed above
            if (INVALID_CASE_REPORTS.contains(caseNameAsPmid)) {
                continue;
            }
            id2lines.put(caseNameAsPmid, lines);
        }
    }

    /**
     * This function reads the txt files from a directory in which we put
     * texts parsed from the PDF files representing the NEJM case reports.
     * @param dir directory with txt files
     * @return list of file paths
     */
    private Set<String> listNejmCaseReportFiles(String dir) {
        File dirFile = new File(dir);
        if (dirFile.isDirectory()) {
            return Stream.of(dirFile.listFiles())
                    .filter(file -> !file.isDirectory())
                    .filter(file -> file.getAbsolutePath().endsWith(".txt"))
                    .map(File::getName)
                    .collect(Collectors.toSet());
        } else {
            throw new PhenolRuntimeException("input directory did not point to valid directory");
        }
    }


    /**
     *
     * @param filePath e.g., /User/rrabbit/data/34644476.txt
     * @return e.g. PMID:34644476
     */
    private String getCaseNameAsPmid(String filePath) {
        File f = new File(filePath);
        String bname = f.getName();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(filePath);
        if (m.find()) {
            return "PMID:" + m.group();
        }
        return bname;
    }

    public void restrictToTarget(String targetCase) {
        if (! id2lines.containsKey(targetCase)) {
            throw new PhenolRuntimeException("Invalid target case");
        }
        List<String> lines = id2lines.get(targetCase);
        id2lines.clear();
        id2lines.put(targetCase, lines);
        System.out.printf("[INFO] Restricting analysis to targetCase %s.\n", targetCase);
    }

    /**
     *
     * @return key: identifier of PMID; value - lines of text
     */
    public Map<String, List<String>> getId2lines() {
        return id2lines;
    }
}

