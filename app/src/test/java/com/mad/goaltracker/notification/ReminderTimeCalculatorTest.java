package com.mad.goaltracker.notification;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị độc lập cho ReminderTimeCalculator (16 ca kiểm thử chi tiết).
 */
public class ReminderTimeCalculatorTest {

    private ReminderTimeCalculator calculator;
    private SimpleDateFormat sdf;

    @Before
    public void setUp() {
        calculator = new ReminderTimeCalculator();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    private Calendar createCalendar(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    @Test
    public void test01_MorningReminderBeforeTriggerTime_Today() {
        // Hiện tại: 2026-11-03 07:30:00 -> Nhắc nhở 08:00 -> Phải là hôm nay 2026-11-03 08:00:00
        Calendar now = createCalendar(2026, 11, 3, 7, 30, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-03 08:00:00", resultDate);
    }

    @Test
    public void test02_MorningReminderOneMinuteBefore_Today() {
        // Hiện tại: 2026-11-03 07:59:00 -> Nhắc nhở 08:00 -> Phải là hôm nay 2026-11-03 08:00:00
        Calendar now = createCalendar(2026, 11, 3, 7, 59, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-03 08:00:00", resultDate);
    }

    @Test
    public void test03_MorningReminderExactTriggerTime_Tomorrow() {
        // Hiện tại: 2026-11-03 08:00:00 -> Trùng giờ -> Phải là ngày mai 2026-11-04 08:00:00
        Calendar now = createCalendar(2026, 11, 3, 8, 0, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-04 08:00:00", resultDate);
    }

    @Test
    public void test04_MorningReminderAfterTriggerTime_Tomorrow() {
        // Hiện tại: 2026-11-03 09:00:00 -> Đã qua giờ -> Phải là ngày mai 2026-11-04 08:00:00
        Calendar now = createCalendar(2026, 11, 3, 9, 0, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-04 08:00:00", resultDate);
    }

    @Test
    public void test05_EveningReminderBeforeTriggerTime_Today() {
        // Hiện tại: 2026-11-03 20:30:00 -> Nhắc nhở 21:00 -> Phải là hôm nay 2026-11-03 21:00:00
        Calendar now = createCalendar(2026, 11, 3, 20, 30, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("21:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-03 21:00:00", resultDate);
    }

    @Test
    public void test06_EveningReminderExactTriggerTime_Tomorrow() {
        // Hiện tại: 2026-11-03 21:00:00 -> Trùng giờ -> Phải là ngày mai 2026-11-04 21:00:00
        Calendar now = createCalendar(2026, 11, 3, 21, 0, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("21:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-04 21:00:00", resultDate);
    }

    @Test
    public void test07_LateNightToMorningReminder_Tomorrow() {
        // Hiện tại: 2026-11-03 23:00:00 -> Nhắc nhở 08:00 -> Phải là sáng hôm sau 2026-11-04 08:00:00
        Calendar now = createCalendar(2026, 11, 3, 23, 0, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-04 08:00:00", resultDate);
    }

    @Test
    public void test08_MonthCrossover() {
        // Hiện tại: 2026-10-31 23:30:00 -> Nhắc nhở 08:00 -> Phải là ngày 2026-11-01 08:00:00
        Calendar now = createCalendar(2026, 10, 31, 23, 30, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-01 08:00:00", resultDate);
    }

    @Test
    public void test09_YearCrossover() {
        // Hiện tại: 2026-12-31 23:30:00 -> Nhắc nhở 08:00 -> Phải là ngày 2027-01-01 08:00:00
        Calendar now = createCalendar(2026, 12, 31, 23, 30, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2027-01-01 08:00:00", resultDate);
    }

    @Test
    public void test10_LeapYearFeb28ToFeb29() {
        // Năm nhuận 2024: Hiện tại 2024-02-28 23:30:00 -> Nhắc nhở 08:00 -> Phải là 2024-02-29 08:00:00
        Calendar now = createCalendar(2024, 2, 28, 23, 30, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("08:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2024-02-29 08:00:00", resultDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test11_InvalidTime_24_00() {
        assertFalse("24:00 is invalid", ReminderTimeCalculator.isValidTime("24:00"));
        Calendar now = createCalendar(2026, 11, 3, 10, 0, 0);
        calculator.calculateNextTriggerMillis("24:00", now);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test12_InvalidTime_08_60() {
        assertFalse("08:60 is invalid", ReminderTimeCalculator.isValidTime("08:60"));
        Calendar now = createCalendar(2026, 11, 3, 10, 0, 0);
        calculator.calculateNextTriggerMillis("08:60", now);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test13_InvalidTime_SingleDigitHour_8_00() {
        assertFalse("8:00 (single digit hour) is invalid, requires 08:00", ReminderTimeCalculator.isValidTime("8:00"));
        Calendar now = createCalendar(2026, 11, 3, 10, 0, 0);
        calculator.calculateNextTriggerMillis("8:00", now);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test14_NullAndEmptyTimeStrings() {
        assertFalse(ReminderTimeCalculator.isValidTime(null));
        assertFalse(ReminderTimeCalculator.isValidTime(""));
        assertFalse(ReminderTimeCalculator.isValidTime("   "));

        Calendar now = createCalendar(2026, 11, 3, 10, 0, 0);
        calculator.calculateNextTriggerMillis("", now);
    }

    @Test
    public void test15_ValidBoundaryTime_00_00() {
        assertTrue(ReminderTimeCalculator.isValidTime("00:00"));
        Calendar now = createCalendar(2026, 11, 3, 23, 0, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("00:00", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-04 00:00:00", resultDate);
    }

    @Test
    public void test16_ValidBoundaryTime_23_59() {
        assertTrue(ReminderTimeCalculator.isValidTime("23:59"));
        Calendar now = createCalendar(2026, 11, 3, 23, 0, 0);
        long triggerMillis = calculator.calculateNextTriggerMillis("23:59", now);

        String resultDate = sdf.format(new Date(triggerMillis));
        assertEquals("2026-11-03 23:59:00", resultDate);
    }
}
