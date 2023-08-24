package org.monarchinitiative.phenopacket2prompt.nejm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Some of the lines in the original text end with a hyphen, because a word is spread across two lines
 * Here, we fix this by joining such words and ensuring that all lines are trimmed (do not start or end with whitespace).
 */
public class Dehyphenizer {


    public static List<String> dehyphenizeLines(List<String> lines) {
        List<String> cleansedLines = new ArrayList<>();
        boolean previousLineHadHyphen = false;
        String previousLinePrefix = "";
        for (String line : lines) {
            String currentLine;
            // get next line and add prefix from previous line if there was one
            if (previousLineHadHyphen) {
                currentLine = previousLinePrefix + line.strip();
                previousLinePrefix = "";
            } else {
                currentLine = line;
            }
            if (currentLine.endsWith("-")) {
                String[] tokens = currentLine.split("\\s+");
                String prefix = tokens[tokens.length - 1];
                tokens = Arrays.copyOf(tokens, tokens.length - 1);
                currentLine = String.join(" ", tokens);
                // remove hyphen
                previousLinePrefix = prefix.substring(0, prefix.length() - 1);
                previousLineHadHyphen = true;
            } else {
                previousLineHadHyphen = false;
            }
            cleansedLines.add(currentLine.trim());
        }
        return cleansedLines;
    }

}
