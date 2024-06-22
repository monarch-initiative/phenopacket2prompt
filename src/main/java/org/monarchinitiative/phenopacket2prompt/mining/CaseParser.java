package org.monarchinitiative.phenopacket2prompt.mining;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CaseParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaseParser.class);

    private final List<Case> caseList;

    public CaseParser(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty()) {
                LOGGER.error("Could not read case file from {} (EMPTY)", path.toFile().getAbsolutePath());
            }
            String line = lines.getFirst().trim();
            if (! line.equals("[source]")) {
                throw new PhenolRuntimeException("Malformed first case line:" + line);
            }
            if (lines.size() < 8) {
                throw new PhenolRuntimeException("Case report too short");
            }
            String pmid = getPMID(lines.get(1));
            String title = getTitle(lines.get(2));
            line = lines.get(3).trim();
            if (! line.equals("[diagnosis]")) {
                throw new PhenolRuntimeException("Malformed [diagnosis] line:" + line);
            }
            String disease_id = getDiseaseId (lines.get(4));
            String disease_label = getDiseaseLabel(lines.get(5));
            line = lines.get(6).trim();
            if (! line.equals("[text]")) {
                throw new PhenolRuntimeException("Malformed first [text] line:" + line);
            }
            List<String> vignetteList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            int i = 7;
            while (i < lines.size()) {
                line = lines.get(i);
                if (line.contains("[text]")) {
                    vignetteList.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    sb.append(line).append(" ");
                }
                i++;
            }
            if (! sb.isEmpty()) {
                vignetteList.add(sb.toString());
            }
            caseList = new ArrayList<>();
            for (String vignette : vignetteList ) {
                caseList.add(new Case(pmid, title, disease_id, disease_label, vignette));
            }

        } catch (IOException e) {
            LOGGER.error("Could not read case file from {}", path.toFile().getAbsolutePath());
            throw new PhenolRuntimeException(e);
        }

    }

    public List<Case> getCaseList() {
        return caseList;
    }

    private String getPMID(String line) {
        String err = String.format("Malformed PMID line: \"%s\"", line);
        if (!line.startsWith("pmid")) {
            throw new PhenolRuntimeException(err);
        }
        String [] fields = line.split("=");
        if (fields.length != 2) {
            throw new PhenolRuntimeException(err);
        }
        String pmid = fields[1].trim();
        if (! pmid.startsWith("PMID:")) {
            throw new PhenolRuntimeException(err);
        }
        return pmid;
    }

    private String getTitle(String line) {
        String err = String.format("Malformed Title line: \"%s\"", line);
        if (!line.startsWith("title")) {
            throw new PhenolRuntimeException(err);
        }
        String[] fields = line.split("=");
        if (fields.length != 2) {
            throw new PhenolRuntimeException(err);
        }
        String title = fields[1].trim();
        if (title.length() < 5) {
            throw new PhenolRuntimeException(err);
        }
        return title;
    }

    private String getDiseaseId(String line) {
        String err = String.format("Malformed disease_id line: \"%s\"", line);
        if (!line.startsWith("disease_id")) {
            throw new PhenolRuntimeException(err);
        }
        String[] fields = line.split("=");
        if (fields.length != 2) {
            throw new PhenolRuntimeException(err);
        }
        String disease_id = fields[1].trim();
        if (! disease_id.startsWith("OMIM:") ||disease_id.startsWith("MONDO:")) {
            throw new PhenolRuntimeException(err);
        }
        return disease_id;
    }

    private String getDiseaseLabel(String line) {
        String err = String.format("Malformed disease_label line: \"%s\"", line);
        if (!line.startsWith("disease_label")) {
            throw new PhenolRuntimeException(err);
        }
        String[] fields = line.split("=");
        if (fields.length != 2) {
            throw new PhenolRuntimeException(err);
        }
        String disease_label = fields[1].trim();
        if (disease_label.length() < 5) {
            throw new PhenolRuntimeException(err);
        }
        return disease_label;
    }

}
