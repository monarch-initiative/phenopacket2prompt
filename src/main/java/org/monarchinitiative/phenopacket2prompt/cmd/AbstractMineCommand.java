package org.monarchinitiative.phenopacket2prompt.cmd;

import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenopacket2prompt.mining.Case;
import org.monarchinitiative.phenopacket2prompt.mining.CaseBundle;
import org.monarchinitiative.phenopacket2prompt.mining.CaseParser;
import org.monarchinitiative.phenopacket2prompt.mining.FenominalParser;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;
import org.monarchinitiative.phenopacket2prompt.output.PromptGenerator;
import org.phenopackets.schema.v2.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AbstractMineCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMineCommand.class);


    protected List<CaseBundle> getCaseBundleList(String inputFile, FenominalParser fenominalParser) {
        List<CaseBundle> caseBundleList = new ArrayList<>();
        CaseParser caseParser = new CaseParser(Path.of(inputFile));
        List<Case> caseList = caseParser.getCaseList();
        for (Case cs : caseList) {
            Phenopacket ppkt = fenominalParser.parse(cs.caseText());
            PpktIndividual individual = new PpktIndividual(ppkt);
            caseBundleList.add(new CaseBundle(cs, ppkt, individual));
        }
        System.out.printf("Got %d cases.\n", caseBundleList.size());
        return caseBundleList;
    }


    protected void outputPrompt(CaseBundle bundle, String output) {
        PpktIndividual individual = bundle.individual();
        PromptGenerator generator = PromptGenerator.english();
        String prompt = generator.createPrompt(individual);
        try  {
            Path path  = Path.of(output);
            Files.writeString(path, prompt);
        } catch (IOException e) {
            LOGGER.error("Could not write prompt: {}", e.getMessage());
            throw new PhenolRuntimeException(e);
        }
    }


}
