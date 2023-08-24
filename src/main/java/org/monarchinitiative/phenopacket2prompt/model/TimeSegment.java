package org.monarchinitiative.phenopacket2prompt.model;

public class TimeSegment {

    private final String timeDesgination;
    private final String payload;

    public TimeSegment(String timeDesgination, String payload) {
        this.timeDesgination = timeDesgination;
        this.payload = payload;
    }


    public String getTimeDesgination() {
        return timeDesgination;
    }

    public String getPayload() {
        return payload;
    }
}
