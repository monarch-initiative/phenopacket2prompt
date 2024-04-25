package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;
import org.monarchinitiative.phenopacket2prompt.model.PpktIndividual;

public interface PhenopacketAgeSexGenerator {

    String individualWithAge(PhenopacketAge ppktAge);

    String atAge(PhenopacketAge ppktAge);

    String ppktSex();



}
