package org.monarchinitiative.phenopacket2prompt.output.impl.english;

import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.List;

public class PpktPhenotypicFeatureEnglish implements PpktPhenotypicFeatureGenerator  {


    private String getOxfordCommaList(List<String> items) {
        if (items.size() == 1) {
            return items.getFirst();
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            return String.join(" and ", items);
        }
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            symList = symList.substring(0, jj) + ", and " + symList.substring(jj+2);
        }
        return symList;
    }

    /**
     * format features
     * The proband was a 39-year old woman who presented at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
     * The patient presented with [list of symptoms]. However, [excluded symptoms] were not observed."
     */
    @Override
    public String formatFeatures(List<OntologyTerm> ontologyTerms) {
        List<String> observed = getObservedFeaturesAsStr(ontologyTerms);
        List<String> excluded = getExcludedFeaturesAsStr(ontologyTerms);
        if (observed.isEmpty() && excluded.isEmpty()) {
            return "no phenotypic abnormalities."; // should never happen, actually!
        } else if (excluded.isEmpty()) {
            return getOxfordCommaList(observed) + ". ";
        } else if (observed.isEmpty()) {
            return "the following manifestations that were excluded: " + getOxfordCommaList(excluded) + ". ";
        } else {
            String exclusion = String.format("However, %s %s excluded.", getOxfordCommaList(excluded), excluded.size() > 1 ? " were" : "was");
            return getOxfordCommaList(observed) + ". " + exclusion;
        }
    }


}
