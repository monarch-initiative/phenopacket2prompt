package org.monarchinitiative.phenopacket2prompt.output;

import org.monarchinitiative.phenopacket2prompt.model.PhenopacketAge;

public interface PhenopacketAgeGenerator {

    String age(PhenopacketAge ppktAge);

    String atAge(PhenopacketAge ppktAge);


}
