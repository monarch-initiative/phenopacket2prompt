package org.monarchinitiative.phenopacket2prompt.phenopacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePointParser {



    /** gets 2 dats before presentation, Two days before presentation etc.*/
    private final Pattern pattern1 = Pattern.compile("\\b\\w+\\b\\s+days? before presentation",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern2 = Pattern.compile("\\b\\w+\\b\\s+weeks earlier",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern3 = Pattern.compile("(approximately)?\\s?\\b\\w+\\b (weeks?|years?|decades?) before (the current )?admission",Pattern.CASE_INSENSITIVE);


    /** e.g. his ocular history included */
    private final Pattern pattern4 = Pattern.compile("\\b\\w+\\b\\s+\\b\\w+\\b\\s+history included",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern5 = Pattern.compile("During the next \\b\\w+\\b (days|weeks)",Pattern.CASE_INSENSITIVE);
    /**
     * e.g., After 3 days of fever
     */
    private final Pattern pattern6 = Pattern.compile("After \\b\\w+\\b (days|weeks) of \\b\\w+\\b",Pattern.CASE_INSENSITIVE);

    //During the next 3 days
    /** Note we do all searching in lower case */
    private final Set<String> fixedPatterns = Set.of("in the emergency department", "on examination");



    private final List<Pattern> patternList;

    public TimePointParser() {
        patternList = new ArrayList<>();
        patternList.add(pattern1);
        patternList.add(pattern2);
        patternList.add(pattern3);
        patternList.add(pattern4);
        patternList.add(pattern5);
        patternList.add(pattern6);
    }

    public List<TimePoint> getTimePoints(String input) {
        List<TimePoint> timepoints = new ArrayList<>();
        patternList.forEach(p -> {
            Matcher m = p.matcher(input);
            while (m.find()) {
                int s = m.start();
                int e = m.end();
                String txt = m.group();
                timepoints.add(new TimePoint(txt, s, e));
            }
        });
        // simpler method for String matches.
        for (String item : fixedPatterns) {
            int lastIndex = 0;
            String inputLower = input.toLowerCase();
            while(lastIndex != -1) {
                lastIndex = inputLower.indexOf(item,lastIndex);
                if(lastIndex != -1){
                    int end = lastIndex + item.length();
                    String originalItem = input.substring(lastIndex, end); // original capitalization
                    timepoints.add(new TimePoint(originalItem, lastIndex, end));
                    lastIndex += 1;
                }
            }
        }


        return timepoints;
    }

}
