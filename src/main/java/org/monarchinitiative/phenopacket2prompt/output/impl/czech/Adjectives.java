package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

class Adjectives {

    static Adjective MALE = new Adjective("mužský", "mužská", "mužské", "mužského", "mužskej", "mužského");
    static Adjective FEMALE = new Adjective("ženský", "ženská", "ženské", "ženského", "ženskej", "ženského");
    static Adjective UNSPECIFIED = new Adjective("neuvedený", "neuvedená", "neuvedené", "neuvedeného", "neuvedenej", "neuvedeného");

    private Adjectives() {
    }
}
