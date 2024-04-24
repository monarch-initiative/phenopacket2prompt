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

public class PpktIndividual {
    Logger LOGGER = LoggerFactory.getLogger(PpktIndividual.class);

    private final Phenopacket ppkt;

    private final String phenopacketId;


    public PpktIndividual(File ppktJsonFile) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(ppktJsonFile));
            JSONObject jsonObject = (JSONObject) obj;
            String phenopacketJsonString = jsonObject.toJSONString();
            Phenopacket.Builder phenoPacketBuilder = Phenopacket.newBuilder();
            JsonFormat.parser().merge(phenopacketJsonString, phenoPacketBuilder);
            this.ppkt = phenoPacketBuilder.build();
        } catch (IOException | ParseException e1) {
            LOGGER.error("Could not ingest phenopacket: {}", e1.getMessage());
            throw new PhenolRuntimeException("Could not load phenopacket at " + ppktJsonFile);
        }
        this.phenopacketId = ppkt.getId();
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

    public Map<PhenopacketAge, List<OntologyTerm>> getPhenotypicFeatures() {
        Map<PhenopacketAge, List<OntologyTerm>> ageToFeatureMap = new HashMap<>();
        PhenopacketAge notSpecified = new AgeNotSpecified();
        ageToFeatureMap.put(notSpecified, new ArrayList<>());
        for (var pf : ppkt.getPhenotypicFeaturesList()) {
            OntologyClass clz = pf.getType();
            if (clz.getId().isEmpty()) {
                System.err.println("Warning, empty ontology term");
                continue;
            }
            TermId hpoId = TermId.of(pf.getType().getId());
            String label = pf.getType().getLabel();
            boolean excluded = pf.getExcluded();
            Optional<PhenopacketAge> opt = Optional.empty();
            if (pf.hasOnset()) {
                TimeElement telem = pf.getOnset();
                opt = getAgeFromTimeElement(telem);
            }
            if (opt.isPresent()) {
                ageToFeatureMap.putIfAbsent(opt.get(), new ArrayList<>());
                ageToFeatureMap.get(opt.get()).add(new OntologyTerm(hpoId, label, excluded, opt.get()));
            } else {
                ageToFeatureMap.get(notSpecified).add(new OntologyTerm(hpoId, label, excluded));
            }
        }
        return ageToFeatureMap;
    }
}
