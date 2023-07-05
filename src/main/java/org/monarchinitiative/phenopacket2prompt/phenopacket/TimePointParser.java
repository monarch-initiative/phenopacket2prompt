package org.monarchinitiative.phenopacket2prompt.phenopacket;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePointParser {



    /** gets 2 dats before presentation, Two days before presentation etc.*/
    private final Pattern pattern1 = Pattern.compile("\\b\\w+\\b\\s+(hours?|days?|weeks?|months?|years?|decades?) before presentation",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern2 = Pattern.compile("\\b\\w+\\b\\s+(hours?|days?|weeks?|months?|years?|decades?) earlier",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern3 = Pattern.compile("(approximately)?\\s?\\b\\w+\\b (hours?|days?|weeks?|months?|years?|decades?) before (the current )?admission",Pattern.CASE_INSENSITIVE);
//Five hours before this admission
    private final Pattern pattern3a = Pattern.compile("(approximately)?\\s?\\b\\w+\\b (hours?|days?|weeks?|months?|years?|decades?) before this admission",Pattern.CASE_INSENSITIVE);


    /** e.g. his ocular history included */
    private final Pattern pattern4 = Pattern.compile("\\b\\w+\\b\\s+\\b\\w+\\b\\s+history included",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern5 = Pattern.compile("During the next \\b\\w+\\b (days|weeks)",Pattern.CASE_INSENSITIVE);
    /**
     * e.g., After 3 days of fever
     */
    private final Pattern pattern6 = Pattern.compile("After \\b\\w+\\b (days|weeks) of \\b\\w+\\b",Pattern.CASE_INSENSITIVE);

    private final Pattern pattern7 = Pattern.compile("\\b\\w+\\b (hours?|days?|weeks?|months?|years?) before (this )?evaluation",Pattern.CASE_INSENSITIVE);
    private final Pattern pattern8 = Pattern.compile("\\b\\w+\\b (days?|weeks?|months?|years?) later",Pattern.CASE_INSENSITIVE);
    private final Pattern pattern9 = Pattern.compile("After a \\b\\w+\\b[ -](weeks?|days?|months?|years?) admission",Pattern.CASE_INSENSITIVE);
    private final Pattern pattern10 = Pattern.compile("On admission to (the other|another) hospital",Pattern.CASE_INSENSITIVE);
    private final Pattern pattern11 = Pattern.compile("Over the next \\b\\w+\\b (hours?|days?|weeks?|months?|years?)",Pattern.CASE_INSENSITIVE);


    /** Note we do all searching in lower case */
    private final Set<String> fixedPatterns = Set.of("in the emergency department", "on examination", "in childhood", "examination was notable for",
            "the night before the current evaluation","on arrival at the emergency department");



    private final List<Pattern> patternList;

    public TimePointParser() {
        patternList = new ArrayList<>();
        patternList.add(pattern1);
        patternList.add(pattern2);
        patternList.add(pattern3);
        patternList.add(pattern3a);
        patternList.add(pattern4);
        patternList.add(pattern5);
        patternList.add(pattern6);
        patternList.add(pattern7);
        patternList.add(pattern8);
        patternList.add(pattern9);
        patternList.add(pattern10);
       // patternList.add(pattern11);

    }

    public List<TimePoint> getTimePoints(String input) {
        Set<TimePoint> timePointSet = new HashSet<>();
        patternList.forEach(p -> {
            Matcher m = p.matcher(input);
            while (m.find()) {
                int s = m.start();
                int e = m.end();
                String txt = m.group();
                if (txt.startsWith(" ")) {
                    txt = txt.substring(1);
                    s = s + 1;
                }
                /// remove stray whitespace
               // txt = txt.replaceAll("\\s+", " ");
                timePointSet.add(new TimePoint(txt, s, e));
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
                    timePointSet.add(new TimePoint(originalItem, lastIndex, end));
                    lastIndex += 1;
                }
            }
        }
        List<TimePoint> tpList = new ArrayList<>(timePointSet);
        Collections.sort(tpList);
        return tpList;
    }

}
