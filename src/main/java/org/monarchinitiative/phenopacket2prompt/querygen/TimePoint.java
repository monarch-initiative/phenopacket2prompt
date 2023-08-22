package org.monarchinitiative.phenopacket2prompt.querygen;

public record TimePoint(String point, int start, int end) implements Comparable<TimePoint>{


    @Override
    public int compareTo(TimePoint other) {
        return this.start - other.start;
    }


    @Override
    public String toString() {
        return String.format("%s (%d-%d)", point, start, end);
    }
}
