package org.monarchinitiative.phenopacket2prompt.output;

import java.util.List;

public interface PhenopacketTextGenerator {



    String QUERY_HEADER();



    /**
     * @param items a list of HPO labels, e.g., X and Y and Z
     * @return A string formatted as X, Y, and Z.
     */
    default String getOxfordCommaList(List<String> items, String andWord) {
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            String andWithSpace = String.format(" %s ", andWord);
            return String.join(andWithSpace, items) + ".";
        }
        StringBuilder sb = new StringBuilder();
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            String andWithSpaceAndComma = String.format(", %s ", andWord);
            symList = symList.substring(0, jj) + andWithSpaceAndComma + symList.substring(jj+2);
        }
        sb.append(symList).append(".");
        return sb.toString();
    }

}
