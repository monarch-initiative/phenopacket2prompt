package org.monarchinitiative.phenopacket2prompt.nejm;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NejmCaseReportImporter {
    private final List<String> lines;
    /** Remove HTML tags */
    final Pattern CLEAN_HTML_TAG = Pattern.compile("<.*?>");
    /** Skip line if it begins with one of these tokens */
    final Set<String> STARTS_WTH_FILTER_TOKENS = Set.of("The New England Journal of Medicine",
            "Downloaded from nejm.org",
            "n engl j med",
            "new engl and jour nal",
            "N Engl J Med",
            "engl j med",
            "Copyright",
            "Case Records of the Massachusetts",
            "at NEJM.org",
            "Massachusetts General Hospital",
            "my nejm in the journal online",
            "Individual subscribers can store articles",
            "From the Department",
            "Founded by Richard C. Cabot"
    );
    final Set<String> EQUALS_FILTER_TOKENS = Set.of("the", "medicine", "Case Records", "of the" );


    public NejmCaseReportImporter(File gptFilePath) {
        lines = new ArrayList<>();
        try {
            CodingErrorAction codingErrorAction = CodingErrorAction.IGNORE;
            Charset charset = Charset.defaultCharset();
            CharsetDecoder charsetDecoder = charset.newDecoder();
            charsetDecoder.onMalformedInput(codingErrorAction);
            InputStream is = new FileInputStream(gptFilePath);
            InputStreamReader reader = new InputStreamReader(is, charsetDecoder);
            BufferedReader br = new BufferedReader(reader);
            String line;
            // the first two lines contain age and sex, always save the
            lines.add(br.readLine());
            lines.add(br.readLine());
            while ((line = br.readLine()) != null) {
                if (line.length() < 3) continue; // empty or very short lines should be skipped
                String processed = cleanLine(line);
                if (processed.contains("  ")) {
                    throw new PhenolRuntimeException("Double white space found in line"+line);
                }
                if (isValid(processed)) {
                    lines.add(processed);
                    // System.out.println(processed);
                }
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not read Gpt file: " + e.getLocalizedMessage());
        }
    }


    private boolean isValid(String line) {
        for (String token : STARTS_WTH_FILTER_TOKENS) {
            if (line.toLowerCase().startsWith(token.toLowerCase()))
                return false;
        }
        for (String token : EQUALS_FILTER_TOKENS) {
            if (line.equalsIgnoreCase(token))
                return false;
        }

        // count letter characters.
        int n_char = 0;
        for (int i=0; i<line.length();i++) {
            if (Character.isAlphabetic(line.charAt(i))) {
                n_char++;
            }
        }
        // Require more than 3 alphabetic characters for a valid line
        return n_char >= 4;
    }

    private String cleanLine(String line) {
        // remove duplicated whitespace
        line = line.replaceAll("\\s+", " ");
        int lastIndex = 0;
        // remove trailing dash, which is a sign that the word at the end of the line was hypenated
        // and now is spread over two lines
        if (line.endsWith("-")) {
            line = line.substring(0, line.length()-1);
        }
        Matcher htmlCleaner = CLEAN_HTML_TAG.matcher(line);
        StringBuilder output = new StringBuilder();
        while (htmlCleaner.find()) {
            output.append(line, lastIndex, htmlCleaner.start());
            lastIndex = htmlCleaner.end();
        }
        if (lastIndex < line.length()) {
            output.append(line, lastIndex, line.length());
        }
        String processed = output.toString();

        processed = processed.replace("-| ", "");
        processed = processed.replace("|", "");
        processed = processed.replaceAll("\\s+", " ");
        if (processed.contains("  ")) {
            throw new PhenolRuntimeException("Double space in processed: " + processed);
        }
        return processed.trim();
    }





    public List<String> getLines() {
        return this.lines;
    }


}
