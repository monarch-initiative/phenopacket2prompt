package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;

public interface PhenopacketSexGenerator {


    String ppktSex(PpktIndividual individual);


}
