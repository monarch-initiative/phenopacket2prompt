package org.monarchinitiative.phenopacket2prompt.model;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.phenopackets.schema.v2.Phenopacket;
import com.google.protobuf.util.JsonFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.phenopackets.schema.v2.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class PpktIndividual {
    private static final Logger LOGGER = LoggerFactory.getLogger(PpktIndividual.class);

    private final Phenopacket ppkt;

    private final String phenopacketId;

    private final List<OntologyTerm> phenotypicFeaturesAtOnset;
    /** features that were observed after the onset but at a specified age. We output a separate vignette for
     * each of these ages.
     */
    private final Map<PhenopacketAge, List<OntologyTerm>> phenotypicFeaturesAtSpecifiedAge;

    private final List<OntologyTerm> phenotypicFeaturesAtOnsetWithoutSpecifiedAge;


    public PpktIndividual(Phenopacket ppkt) {
        this.ppkt = ppkt;
        this.phenopacketId = ppkt.getId();
        phenotypicFeaturesAtSpecifiedAge = extractSpecifiedAgePhenotypicFeatures();
        List<OntologyTerm> onsetTerms = extractPhenotypicFeaturesAtOnset();
        List<OntologyTerm> unspecifiedTerms = extractPhenotypicFeaturesWithNoSpecifiedAge();
        if (onsetTerms.isEmpty()) {
            phenotypicFeaturesAtOnset = List.copyOf(unspecifiedTerms);
            phenotypicFeaturesAtOnsetWithoutSpecifiedAge = List.of();
        } else {
            // For now, we will put all unspecified terms at the onset
            onsetTerms.addAll(unspecifiedTerms);
            phenotypicFeaturesAtOnset = List.copyOf(onsetTerms);
            phenotypicFeaturesAtOnsetWithoutSpecifiedAge = List.of();
        }


    }

    public static PpktIndividual fromFile(File ppktJsonFile) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(ppktJsonFile));
            JSONObject jsonObject = (JSONObject) obj;
            String phenopacketJsonString = jsonObject.toJSONString();
            Phenopacket.Builder phenoPacketBuilder = Phenopacket.newBuilder();
            JsonFormat.parser().merge(phenopacketJsonString, phenoPacketBuilder);
            Phenopacket ppkt = phenoPacketBuilder.build();
            return new PpktIndividual(ppkt);
        } catch (IOException | ParseException e1) {
            LOGGER.error("Could not ingest phenopacket: {}", e1.getMessage());
            throw new PhenolRuntimeException("Could not load phenopacket at " + ppktJsonFile);
        }
    }

    public static PpktIndividual fromPhenopacket(Phenopacket ppkt) {
        return new PpktIndividual(ppkt);
    }



    public String getPhenopacketId() {
        return phenopacketId;
    }

    public PhenopacketSex getSex() {
        Sex sex = ppkt.getSubject().getSex();
        return switch (sex) {
            case MALE -> PhenopacketSex.MALE;
            case FEMALE -> PhenopacketSex.FEMALE;
            case OTHER_SEX -> PhenopacketSex.OTHER;
            default -> PhenopacketSex.UNKNOWN;
        };
    }

    private Optional<PhenopacketAge> getAgeFromTimeElement(TimeElement telem) {
        if (telem.hasAge()) {
            return Optional.of(new Iso8601Age(telem.getAge().getIso8601Duration()));
        } else if (telem.hasOntologyClass()) {
            OntologyClass clz = telem.getOntologyClass();
            return Optional.of(new HpoOnsetAge(clz.getId(), clz.getLabel()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<PhenopacketAge> getAgeAtLastExamination() {
        if (ppkt.getSubject().hasTimeAtLastEncounter()) {
            TimeElement telem = ppkt.getSubject().getTimeAtLastEncounter();
            return getAgeFromTimeElement(telem);
        }
        return Optional.empty();
    }


    public Optional<PhenopacketAge> getAgeAtOnset() {
        if (ppkt.getDiseasesCount() == 1) {
            Disease dx = ppkt.getDiseases(0);
            if (dx.hasOnset()) {
                TimeElement telem = dx.getOnset();
                return getAgeFromTimeElement(telem);
            }
        }
        return Optional.empty();
    }


    public List<PhenopacketDisease> getDiseases() {
        List<PhenopacketDisease> diseases = new ArrayList<>();
        for (Disease d : ppkt.getDiseasesList()) {
            if (d.getExcluded()) continue;
            diseases.add(new PhenopacketDisease(d.getTerm().getId(), d.getTerm().getLabel()));
        }
        return diseases;
    }



    private List<OntologyTerm> extractPhenotypicFeaturesWithNoSpecifiedAge() {
        List<OntologyTerm> unspecifiedFeatures = new ArrayList<>();
        for (var pf : ppkt.getPhenotypicFeaturesList()) {
            OntologyClass clz = pf.getType();
            if (clz.getId().isEmpty()) {
                System.err.println("Warning, empty ontology term");
                continue;
            }
            TermId hpoId = TermId.of(pf.getType().getId());
            String label = pf.getType().getLabel();
            boolean excluded = pf.getExcluded();
            if (pf.hasOnset()) {
                continue;
            } else {
                unspecifiedFeatures.add(new OntologyTerm(hpoId, label, excluded));
            }
        }
        return unspecifiedFeatures;
    }


    private boolean agesEqual(PhenopacketAge ageOne, PhenopacketAge ageTwo) {
        if (ageOne.ageType().equals(ageTwo.ageType())) {
            if (ageOne.ageType().equals(PhenopacketAgeType.ISO8601_AGE_TYPE)) {
                Iso8601Age isoOne = (Iso8601Age) ageOne;
                Iso8601Age isoTwo = (Iso8601Age) ageTwo;
                return isoOne.getDays() == isoTwo.getDays() &&
                        isoOne.getMonths() == isoTwo.getMonths() &&
                        isoOne.getYears() == isoTwo.getYears();
            } else if (ageOne.ageType().equals(PhenopacketAgeType.HPO_ONSET_AGE_TYPE)) {
                HpoOnsetAge onsetOne = (HpoOnsetAge) ageOne;
                HpoOnsetAge onsetTwo = (HpoOnsetAge) ageTwo;
                return onsetOne.getTid().equals(onsetTwo.getTid());
            }
        }
        return false;
    }

    /**
     * @return List of Phenotypic features that were observed at the age of onset.
     */
    private List<OntologyTerm> extractPhenotypicFeaturesAtOnset() {
        Optional<PhenopacketAge> opt = getAgeAtOnset();
        if (opt.isEmpty()) {
            return new ArrayList<>(); //
        }
        List<OntologyTerm> onsetFeatures = new ArrayList<>();
        PhenopacketAge onsetAge = opt.get();

        for (var pf : ppkt.getPhenotypicFeaturesList()) {
            OntologyClass clz = pf.getType();
            if (clz.getId().isEmpty()) {
                System.err.println("Warning, empty ontology term");
                continue;
            }
            TermId hpoId = TermId.of(pf.getType().getId());
            String label = pf.getType().getLabel();
            boolean excluded = pf.getExcluded();
            if (pf.hasOnset()) {
                TimeElement telem = pf.getOnset();
                Optional<PhenopacketAge> ageOpt = getAgeFromTimeElement(telem);
                if (ageOpt.isPresent()) {
                    if (agesEqual(onsetAge, ageOpt.get())) {
                        onsetFeatures.add(new OntologyTerm(hpoId, label, excluded, onsetAge));
                    }
                }
            }
        }
        return onsetFeatures;
    }



    /**
     * This code does not include features with unspecified onset (for that, use {@code getPhenotypicFeaturesWithNoSpecifiedAge}) or terms at the age of onset
     * @return map of phenotypic features with specified onset after the age of onset
     */
    public Map<PhenopacketAge, List<OntologyTerm>> extractSpecifiedAgePhenotypicFeatures() {
        Map<PhenopacketAge, List<OntologyTerm>> ageToFeatureMap = new HashMap<>();
        Optional<PhenopacketAge> onsetOpt = getAgeAtOnset();
        for (var pf : ppkt.getPhenotypicFeaturesList()) {
            OntologyClass clz = pf.getType();
            if (clz.getId().isEmpty()) {
                System.err.println("Warning, empty ontology term");
                continue;
            }
            TermId hpoId = TermId.of(pf.getType().getId());
            String label = pf.getType().getLabel();
            boolean excluded = pf.getExcluded();
            Optional<PhenopacketAge> ageOpt = Optional.empty();
            if (pf.hasOnset()) {
                TimeElement telem = pf.getOnset();
                ageOpt = getAgeFromTimeElement(telem);
            }
            // skip features that occur at age of onset
            if (ageOpt.isPresent() && onsetOpt.isPresent()) {
                if (agesEqual(ageOpt.get(), onsetOpt.get())) {
                    continue;
                }
            }
            // only add features with specified onset here.
            if (ageOpt.isPresent()) {
                ageToFeatureMap.putIfAbsent(ageOpt.get(), new ArrayList<>());
                ageToFeatureMap.get(ageOpt.get()).add(new OntologyTerm(hpoId, label, excluded, ageOpt.get()));
            }
        }
        return ageToFeatureMap;
    }

    public int annotationCount() {
        return ppkt.getPhenotypicFeaturesCount();
    }

    public List<OntologyTerm> getPhenotypicFeaturesAtOnset() {
        return phenotypicFeaturesAtOnset;
    }

    public List<OntologyTerm> getObservedPhenotypicFeaturesAtOnset() {
        return phenotypicFeaturesAtOnset.stream().filter(Predicate.not(OntologyTerm::isExcluded)).toList();
    }

    public List<OntologyTerm> getExcludedPhenotypicFeaturesAtOnset() {
        return phenotypicFeaturesAtOnset.stream().filter(OntologyTerm::isExcluded).toList();
    }

    public boolean hasObservedPhenotypeFeatureAtOnset() {
        return phenotypicFeaturesAtOnset.stream().anyMatch(Predicate.not(OntologyTerm::isExcluded));
    }

    public boolean hasExcludedPhenotypeFeatureAtOnset() {
        return phenotypicFeaturesAtOnset.stream().anyMatch(OntologyTerm::isExcluded);
    }

    public Map<PhenopacketAge, List<OntologyTerm>> getPhenotypicFeaturesAtSpecifiedAge() {
        return phenotypicFeaturesAtSpecifiedAge;
    }

   /* public List<OntologyTerm> getPhenotypicFeaturesAtOnsetWithoutSpecifiedAge() {
        return phenotypicFeaturesAtOnsetWithoutSpecifiedAge;
    }*/
}
