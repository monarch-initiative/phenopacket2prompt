package org.monarchinitiative.phenopacket2prompt.model.ppkt;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.phenopackets.schema.v2.Phenopacket;
import com.google.protobuf.util.JsonFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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



}
