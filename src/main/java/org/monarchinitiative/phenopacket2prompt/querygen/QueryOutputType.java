package org.monarchinitiative.phenopacket2prompt.querygen;

public enum QueryOutputType {

    TIME_BASED,
    QC,
    TEXT_WITHOUT_DISCUSSION,
    TEXT_PLUS_MANUAL;


    public static String outputString(QueryOutputType qtype) {
        return switch (qtype) {
            case TIME_BASED -> "phenopacket_time_based_queries";
            case QC -> "QC";
            case TEXT_WITHOUT_DISCUSSION -> "txt_without_discussion";
            case TEXT_PLUS_MANUAL -> "txt_with_manual_annots";
        };
    }




}
