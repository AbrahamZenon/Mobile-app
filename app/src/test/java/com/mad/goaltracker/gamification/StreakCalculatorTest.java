package com.mad.goaltracker.gamification;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Kiểm thử đơn vị toàn diện cho StreakCalculator (16 ca kiểm thử chi tiết).
 */
public class StreakCalculatorTest {

    private StreakCalculator calculator;

    @Before
    public void setUp() {
        calculator = new StreakCalculator();
    }

    @Test
    public void test01_EmptyActivityDates() {
        StreakSummary summary = calculator.calculate(Collections.emptyList(), "2026-11-03");
        assertNotNull(summary);
        assertEquals("Empty list should result in currentStreak=0", 0, summary.getCurrentStreak());
        assertEquals("Empty list should result in longestStreak=0", 0, summary.getLongestStreak());
    }

    @Test
    public void test02_NullActivityDates() {
        StreakSummary summary = calculator.calculate(null, "2026-11-03");
        assertNotNull(summary);
        assertEquals("Null list should result in currentStreak=0", 0, summary.getCurrentStreak());
        assertEquals("Null list should result in longestStreak=0", 0, summary.getLongestStreak());
    }

    @Test
    public void test03_SingleDayActivityToday() {
        List<String> dates = Collections.singletonList("2026-11-03");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Current streak with activity today should be 1", 1, summary.getCurrentStreak());
        assertEquals("Longest streak should be 1", 1, summary.getLongestStreak());
    }

    @Test
    public void test04_ActivityTodayAndYesterday() {
        List<String> dates = Arrays.asList("2026-11-02", "2026-11-03");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Current streak should be 2 (yesterday + today)", 2, summary.getCurrentStreak());
        assertEquals("Longest streak should be 2", 2, summary.getLongestStreak());
    }

    @Test
    public void test05_ActivityYesterdayButNotToday() {
        // Hôm nay chưa làm, nhưng hôm qua có làm -> chuỗi vẫn duy trì
        List<String> dates = Collections.singletonList("2026-11-02");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Current streak starting from yesterday should be 1", 1, summary.getCurrentStreak());
        assertEquals("Longest streak should be 1", 1, summary.getLongestStreak());
    }

    @Test
    public void test06_ActivityTwoDaysAgo_BrokenStreak() {
        // Ngày gần nhất là 01/11, hôm nay là 03/11 -> chuỗi hiện tại đứt gãy
        List<String> dates = Collections.singletonList("2026-11-01");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Current streak should be 0 because last active was older than yesterday", 0, summary.getCurrentStreak());
        assertEquals("Longest streak in history is still 1", 1, summary.getLongestStreak());
    }

    @Test
    public void test07_DuplicateDatesDeduplication() {
        List<String> dates = Arrays.asList("2026-11-03", "2026-11-03", "2026-11-02", "2026-11-02", "2026-11-01");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Duplicate dates must not inflate current streak (01, 02, 03 = 3)", 3, summary.getCurrentStreak());
        assertEquals("Duplicate dates must not inflate longest streak", 3, summary.getLongestStreak());
    }

    @Test
    public void test08_UnsortedDatesOrder() {
        List<String> dates = Arrays.asList("2026-11-02", "2026-10-31", "2026-11-03", "2026-11-01");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Unsorted dates should be sorted and result in streak of 4 (31/10 -> 03/11)", 4, summary.getCurrentStreak());
        assertEquals("Longest streak should be 4", 4, summary.getLongestStreak());
    }

    @Test
    public void test09_InvalidDateFormatsFilteredOut() {
        List<String> dates = Arrays.asList(
                "invalid-date",
                "",
                null,
                "2026/11/03",
                "2026-02-31", // Ngày 31 tháng 2 không tồn tại
                "2026-13-01", // Tháng 13 không tồn tại
                "2026-11-02",
                "2026-11-03"
        );
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Only valid dates (02, 03) should be counted", 2, summary.getCurrentStreak());
        assertEquals("Longest streak should be 2", 2, summary.getLongestStreak());
    }

    @Test
    public void test10_FutureDatesDiscarded() {
        // Today là 2026-11-03, các ngày 04, 05 là tương lai
        List<String> dates = Arrays.asList("2026-11-02", "2026-11-03", "2026-11-04", "2026-11-05");
        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Future dates must be ignored; current streak up to today is 2", 2, summary.getCurrentStreak());
        assertEquals("Longest streak up to today is 2", 2, summary.getLongestStreak());
    }

    @Test
    public void test11_BrokenStreakInHistory() {
        // Lịch sử: 01, 02, 03 (3 ngày), ngắt quãng ngày 04, sau đó 05, 06, 07, 08 (4 ngày)
        List<String> dates = Arrays.asList(
                "2026-10-01", "2026-10-02", "2026-10-03",
                "2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08"
        );
        StreakSummary summary = calculator.calculate(dates, "2026-10-08");
        assertEquals("Current streak ending on 2026-10-08 should be 4 (05->08)", 4, summary.getCurrentStreak());
        assertEquals("Longest streak should be 4", 4, summary.getLongestStreak());
    }

    @Test
    public void test12_MonthCrossover() {
        // Chuyển tháng: 30/10 -> 31/10 -> 01/11 -> 02/11
        List<String> dates = Arrays.asList("2026-10-30", "2026-10-31", "2026-11-01", "2026-11-02");
        StreakSummary summary = calculator.calculate(dates, "2026-11-02");
        assertEquals("Streak across month boundary should be 4", 4, summary.getCurrentStreak());
        assertEquals("Longest streak across month boundary should be 4", 4, summary.getLongestStreak());
    }

    @Test
    public void test13_YearCrossover() {
        // Chuyển năm: 30/12/2026 -> 31/12/2026 -> 01/01/2027 -> 02/01/2027
        List<String> dates = Arrays.asList("2026-12-30", "2026-12-31", "2027-01-01", "2027-01-02");
        StreakSummary summary = calculator.calculate(dates, "2027-01-02");
        assertEquals("Streak across year boundary should be 4", 4, summary.getCurrentStreak());
        assertEquals("Longest streak across year boundary should be 4", 4, summary.getLongestStreak());
    }

    @Test
    public void test14_LeapYearFeb29() {
        // Năm nhuận 2024: 28/02/2024 -> 29/02/2024 -> 01/03/2024
        List<String> dates = Arrays.asList("2024-02-28", "2024-02-29", "2024-03-01");
        StreakSummary summary = calculator.calculate(dates, "2024-03-01");
        assertEquals("Streak across Feb 29 in leap year should be 3", 3, summary.getCurrentStreak());
        assertEquals("Longest streak in leap year should be 3", 3, summary.getLongestStreak());
    }

    @Test
    public void test15_CurrentStreakDiffersFromLongestStreak() {
        // Tháng 9: chuỗi 10 ngày (01 -> 10)
        // Nghỉ tháng 10
        // Tháng 11: mới làm ngày hôm nay (03/11)
        List<String> dates = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            dates.add(String.format("2026-09-%02d", i));
        }
        dates.add("2026-11-03");

        StreakSummary summary = calculator.calculate(dates, "2026-11-03");
        assertEquals("Current streak should be 1 (only today 03/11)", 1, summary.getCurrentStreak());
        assertEquals("Longest streak should be 10 (from September)", 10, summary.getLongestStreak());
    }

    @Test
    public void test16_SafeFallbackWhenTodayDateIsInvalidOrNull() {
        List<String> dates = Collections.singletonList("2020-01-01");
        StreakSummary s1 = calculator.calculate(dates, null);
        assertNotNull("Should not crash with null todayDate", s1);

        StreakSummary s2 = calculator.calculate(dates, "bad-date-format");
        assertNotNull("Should not crash with invalid todayDate", s2);
    }
}
