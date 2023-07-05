package org.monarchinitiative.phenopacket2prompt.phenopacket;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimePointParserTest {

    private final TimePointParser timePointParser = new TimePointParser();

    @Test
    public void test1() {
        String input = "The patient had been well until 3 days before presentation, when pressurelike  pain developed " +
                "n the left side of the forehead and frontal scalp and the bilateral  maxillary sinuses and upper jaws.";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals(32, tp.start());
        assertEquals(58, tp.end());
    }

    @Test
    public void test2() {
        String input = "Two days before presentation, the patient noted erythema and small reddishbrown skin lesions on " +
                "the left side of the forehead ";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("Two days before presentation", tp.point());
        assertEquals(0, tp.start());
        assertEquals(28, tp.end());
    }

    @Test
    public void test3() {
        String input = "had pointed out a similar spot on the top of his scalp 2 weeks earlier.";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("2 weeks earlier", tp.point());
        assertEquals(55, tp.start());
        assertEquals(70, tp.end());
    }

    @Test
    public void test4() {
        String input = "In the emergency department, the patient reported no ocular or nasal discharge,";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("In the emergency department", tp.point());
        assertEquals(0, tp.start());
        assertEquals(27, tp.end());
    }

    @Test
    public void test5() {
        String input = "His ocular history included  bilateral mild ptosis; he had undergone bilateral  cataract extraction";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("His ocular history included", tp.point());
        assertEquals(0, tp.start());
        assertEquals(27, tp.end());
    }

    @Test
    public void test6() {
        String input = "The patient had been in her usual state of health until approximately 4 weeks before admission,";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("approximately 4 weeks before admission", tp.point());
        assertEquals(56, tp.start());
        assertEquals(94, tp.end());
    }

    @Test
    public void test7() {
        String input = "During the next 3 days, she had nausea and a poor appetite. ";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("During the next 3 days", tp.point());
        assertEquals(0, tp.start());
        assertEquals(22, tp.end());
    }

    @Test
    public void test8() {
        String input = "After 3 days of fever with a temperature of up to 38.5°C, she began  vomiting  ";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("After 3 days of fever", tp.point());
        assertEquals(0, tp.start());
        assertEquals(21, tp.end());
    }

    @Test
    public void test9() {
        String input = "Approximately two decades before the current admission, he was struck on the head ";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("Approximately two decades before the current admission", tp.point());
        assertEquals(0, tp.start());
        assertEquals(54, tp.end());
    }

    @Test
    public void test10() {
        String input = "Some text (4 years before the current admission) some text";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("4 years before the current admission", tp.point());
        assertEquals(11, tp.start());
        assertEquals(47, tp.end());
    }

    @Test
    public void test11() {
        String input = " been well until 20 hours before this admission, when diffuse  abdominal pain and nausea developed";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("20 hours before this admission", tp.point());
        assertEquals(17, tp.start());
        assertEquals(47, tp.end());
    }

    @Test
    public void test12() {
        String input = "After a 1-week admission, the dyspnea abated, ";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("After a 1-week admission", tp.point());
        assertEquals(0, tp.start());
        assertEquals(24, tp.end());
    }

    @Test
    public void test13() {
        String input = "The patient had been in his usual state of good health until 1 hour before evaluation, some other text.";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("1 hour before evaluation", tp.point());
        assertEquals(61, tp.start());
        assertEquals(85, tp.end());
    }
    @Test
    public void test14() {
        String input = "Approximately 2 weeks before the current admission, she sought evaluation at the emergency department of another  hospital.";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("Approximately 2 weeks before the current admission", tp.point());
        assertEquals(0, tp.start());
        assertEquals(50, tp.end());
    }

    @Test
    public void test15() {
        String input = "he patient had been in her usual state of health until 4 days before admission, when she awoke from sleep with swelling .";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("4 days before admission", tp.point());
        assertEquals(55, tp.start());
        assertEquals(78, tp.end());
    }

    @Test
    public void test16() {
        String input = "The patient had been in his usual state of health until 8 days before this admission, when constant aching pain developed in the left eye and the left side of the head.";
        List<TimePoint> tplist = timePointParser.getTimePoints(input);
        assertEquals(1, tplist.size());
        TimePoint tp = tplist.get(0);
        assertEquals("8 days before this admission", tp.point());
        assertEquals(56, tp.start());
        assertEquals(84, tp.end());
    }

//T
}
