package org.monarchinitiative.phenopacket2prompt.output.impl.czech;

record Adjective(String nominativMale, String nominativFemale, String nominativNeutrum, String genitivMale, String genitivFemale, String genitivNeutrum) {

    String nominativ(Genus genus) {
        return switch (genus) {
            case HE -> nominativMale;
            case SHE -> nominativFemale;
            case IT -> nominativNeutrum;
        };
    }

    String genitiv(Genus genus) {
        return switch (genus) {
            case HE -> genitivMale;
            case SHE -> genitivFemale;
            case IT -> genitivNeutrum;
        };
    }
}
