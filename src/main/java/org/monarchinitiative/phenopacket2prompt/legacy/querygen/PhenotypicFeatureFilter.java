package org.monarchinitiative.phenopacket2prompt.legacy.querygen;


import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v2.core.OntologyClass;
import org.phenopackets.schema.v2.core.PhenotypicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PhenotypicFeatureFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhenotypicFeatureFilter.class);
    final Set<PhenotypicFeature> finalFeatures;


    private final static TermId negativism = TermId.of("HP:0410291"); // common mistake (negative)
    private final static TermId allergy = TermId.of("HP:0012393"); // Only allow specific allergy terms. Allergy HP:0012393 is getting picked up for questions

    private final static TermId neoplasm = TermId.of("HP:0002664"); // Only allow specific cancer terms.Neoplasm HP:0002664 is only used for family history etc

    private final static TermId asthenia = TermId.of("HP:0025406"); //Asthenia HP:0025406 -- FP call for Weakness
    private final static Set<TermId> termsToAvoid = Set.of(negativism, allergy, neoplasm);

    public PhenotypicFeatureFilter(Set<PhenotypicFeature> phenotypicFeaturesSet, Ontology ontology) {
        // remove terms that are both observed and excluded -- presumably there is some error
        Map<OntologyClass, PhenotypicFeature> observed = phenotypicFeaturesSet.stream()
                .filter(Predicate.not(PhenotypicFeature::getExcluded))
                .collect(Collectors.toMap(PhenotypicFeature::getType, Function.identity()));

        Map<OntologyClass, PhenotypicFeature> excluded = phenotypicFeaturesSet.stream()
                .filter(PhenotypicFeature::getExcluded)
                .collect(Collectors.toMap(PhenotypicFeature::getType, Function.identity()));
        HashSet<OntologyClass> termsToExclude = new HashSet<>();
        for (OntologyClass tid  : observed.keySet() ) {
            if (excluded.containsKey(tid)) {
                termsToExclude.add(tid);
                LOGGER.info("Excluding {}/{} because it was in both observed and excluded", tid.getId(), tid.getLabel());
            }
        }
        // now transform survinng terms into TermIds
        Map<TermId, PhenotypicFeature> map2 = new HashMap<>();
        for (PhenotypicFeature  pf: phenotypicFeaturesSet) {
            if (termsToExclude.contains(pf.getType())) {
                continue;
            }
            TermId tid = TermId.of(pf.getType().getId());
            map2.put(tid, pf);
        }
        // remove the ancestors of any term, keeping only the most specific
        Set<TermId> ancestorsToExclude = new HashSet<>();
        for (TermId t : map2.keySet()) {
            // get all ancestors, do not include current term
            Set<TermId> ancs = ontology.getAncestorTermIds(t,false);
            for (TermId anc : ancs) {
                if (!anc.equals(t) && map2.containsKey(anc)) {
                    ancestorsToExclude.add(anc);
                }
            }
        }
        finalFeatures = new HashSet<>();
        for (Map.Entry<TermId, PhenotypicFeature> e : map2.entrySet()) {
            if (ancestorsToExclude.contains(e.getKey())) {
                LOGGER.info("Skipping ancestor {}", e.getKey().getValue());
            } else if (termsToAvoid.contains(e.getKey())) {
                LOGGER.info("Skipping term to exclude {}", e.getKey().getValue());
            } else {
                finalFeatures.add(e.getValue());
            }
        }
    }

    public static boolean isOmittedTerm(TermId tid) {
        return termsToAvoid.contains(tid);
    }

    public Set<PhenotypicFeature> getFinalFeatures() {
        return finalFeatures;
    }
}
