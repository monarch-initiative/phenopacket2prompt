package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.international.HpInternational;
import org.monarchinitiative.phenopacket2prompt.model.OntologyTerm;
import org.monarchinitiative.phenopacket2prompt.output.PpktPhenotypicFeatureGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PpktPhenotypicFeatureCzech implements PpktPhenotypicFeatureGenerator  {

    private final HpInternational czech;
    private final Set<String> missingTranslations;

    public PpktPhenotypicFeatureCzech(HpInternational international) {
        czech = international;
        missingTranslations = new HashSet<>();
    }

    /**
     * This method is called right after the demographic information are given, e.g.,
     * The proband was a 32-year-old female who presented at the age of 2 years[...]"
     * @param ontologyTerms
     * @return
     */
    public String featuresAtPresentation(String person, List<OntologyTerm> ontologyTerms) {
        List<String>  observed = getObservedFeaturesAsStr(ontologyTerms);
        List<String>  excluded = getExcludedFeaturesAsStr(ontologyTerms);
        if (! observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("s %s. In contrast, %s %s excluded. ",
                    getOxfordCommaList(observed),
                    getOxfordCommaList(excluded),
                    excluded.size() > 1 ? "were" : "byl");

        } else if (!excluded.isEmpty()) {
            return String.format(" s %s. ",
                    getOxfordCommaList(observed));
        } else if (!observed.isEmpty()) {
            return String.format(". %s %s excluded. ",
                    getOxfordCommaList(excluded),
                    excluded.size() > 1 ? "were" : "was");
        } else {
            throw new PhenolRuntimeException("No phenotypic features passed");
        }
    }


    /**
     * This method is called right after each subsequent age. For instance,
     * At the age of 7 years, she[...]"
     * @param ontologyTerms
     * @return
     */
    public String featuresForVignette(List<OntologyTerm> ontologyTerms) {
        List<String> observed = getObservedFeaturesAsStr(ontologyTerms);
        List<String> excluded = getExcludedFeaturesAsStr(ontologyTerms);
        if (! observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format(" presented with %s. In contrast, %s %s excluded. ",
                    getOxfordCommaList(observed),
                    getOxfordCommaList(excluded),
                    excluded.size() > 1 ? "were" : "was");

        } else if (!excluded.isEmpty()) {
            return String.format(" presented with %s. ",
                    getOxfordCommaList(observed));
        } else if (!observed.isEmpty()) {
            return String.format(" . %s %s excluded. ",
                    getOxfordCommaList(excluded),
                    excluded.size() > 1 ? "were" : "was");
        } else {
            throw new PhenolRuntimeException("No phenotypic features passed");
        }
    }




    private String getOxfordCommaList(List<String> items) {
        if (items.size() == 1) {
            return items.getFirst();
        }
        if (items.size() == 2) {
            // no comma if we just have two items.
            // one item will work with the below code
            return String.join(" a ", items);
        }
        String symList = String.join(", ", items);
        int jj = symList.lastIndexOf(", ");
        if (jj > 0) {
            symList = symList.substring(0, jj) + " a " + symList.substring(jj+2);
        }
        return symList;
    }

    /**
     * format features
     * The proband was a 39-year-old woman who presented at the age of 12 years with HPO1, HPO2, and HPO3. HPO4 and HPO5 were excluded.
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
            return "následující symptomy byly vyloučeny: " + getOxfordCommaList(excluded) + ". ";
        } else {
            String exclusion = String.format("Avšak, symptomy %s %s %s.",
                    excluded.size() > 1 ? "symptomy" : "symptom",
                    getOxfordCommaList(excluded),
                    excluded.size() > 1 ? "boly vyloučeny" : "bol vyloučen"
        );
            return getOxfordCommaList(observed) + ", " + exclusion;
        }
    }

    @Override
    public String featuresAtOnset(String personString, List<OntologyTerm> ontologyTerms) {
        List<String> observed = getObservedFeaturesAsStr(ontologyTerms);
        List<String> excluded = getExcludedFeaturesAsStr(ontologyTerms);
        var observedStr = getOxfordCommaList(observed);
        var excludedStr = getOxfordCommaList(excluded);
        if (!observed.isEmpty() && ! excluded.isEmpty()) {
            return String.format("%s se prezentoval s %s. Následující symptomy byly vyloučeny: %s.",
                    personString, observedStr, excludedStr);
        } else if (!observed.isEmpty()) {
            return String.format("%s se prezentoval s následujícimi symptomy: %s.", personString, observedStr);
        } else if (!excluded.isEmpty()) {
            return String.format("%s při nástupu onemocnění nevykazoval žádné abnormality a následující symptomy byly vyloučeny: %s.",
                    personString, excludedStr);
        } else {
            return "Při nástupu onemocnění nebyly pozorované žádné klinické příznaky.";
        }
    }
}
