package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PhenopacketSex;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;

public interface PhenopacketAgeSexGenerator {

    String getIndividualDescription(PpktIndividual individual);


    String heSheIndividual(PhenopacketSex psex);

    String atAge(PhenopacketAge ppktAge);

    //String ppktSex();



}
