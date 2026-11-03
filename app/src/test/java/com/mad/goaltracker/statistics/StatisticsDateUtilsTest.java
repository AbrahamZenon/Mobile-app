package com.mad.goaltracker.statistics;

import com.mad.goaltracker.utils.StatisticsDateUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị cho StatisticsDateUtils.
 * Xác minh tính toán chuỗi 7 ngày liên tiếp, chuyển tháng, năm, năm nhuận,
 * nhãn thứ tiếng Việt và định dạng tiến độ phần trăm.
 */
public class StatisticsDateUtilsTest {

    @Test
    public void testLast7Days_orderAndSize() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 3); // 2026-11-03

        List<String> days = StatisticsDateUtils.getLast7Days(cal);
        assertNotNull(days);
        assertEquals(7, days.size());
        assertEquals("2026-11-03", days.get(6));
        assertEquals("2026-10-28", days.get(0));
    }

    @Test
    public void testLast7Days_monthTransition() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 3); // 2026-11-03

        List<String> days = StatisticsDateUtils.getLast7Days(cal);
        assertEquals("2026-10-28", days.get(0));
        assertEquals("2026-10-29", days.get(1));
        assertEquals("2026-10-30", days.get(2));
        assertEquals("2026-10-31", days.get(3));
        assertEquals("2026-11-01", days.get(4));
        assertEquals("2026-11-02", days.get(5));
        assertEquals("2026-11-03", days.get(6));
    }

    @Test
    public void testLast7Days_yearTransition() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JANUARY, 3); // 2026-01-03

        List<String> days = StatisticsDateUtils.getLast7Days(cal);
        assertEquals(7, days.size());
        assertEquals("2025-12-28", days.get(0));
        assertEquals("2025-12-29", days.get(1));
        assertEquals("2025-12-30", days.get(2));
        assertEquals("2025-12-31", days.get(3));
        assertEquals("2026-01-01", days.get(4));
        assertEquals("2026-01-02", days.get(5));
        assertEquals("2026-01-03", days.get(6));
    }

    @Test
    public void testLast7Days_leapYearTransition() {
        // Năm nhuận 2024: tháng 2 có 29 ngày
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 2); // 2024-03-02

        List<String> days = StatisticsDateUtils.getLast7Days(cal);
        assertEquals(7, days.size());
        assertEquals("2024-02-25", days.get(0));
        assertEquals("2024-02-26", days.get(1));
        assertEquals("2024-02-27", days.get(2));
        assertEquals("2024-02-28", days.get(3));
        assertEquals("2024-02-29", days.get(4)); // Ngày 29/02 năm nhuận
        assertEquals("2024-03-01", days.get(5));
        assertEquals("2024-03-02", days.get(6));
    }

    @Test
    public void testLast7Days_nullAnchor() {
        List<String> days = StatisticsDateUtils.getLast7Days(null);
        assertNotNull(days);
        assertEquals(7, days.size());
        assertEquals(StatisticsDateUtils.getTodayDateString(), days.get(6));
    }

    @Test
    public void testGetDayOfWeekLabel_allDays() {
        // Tuần đầu tháng 11 năm 2026:
        // 2026-11-02 là Thứ Hai -> T2
        // 2026-11-03 là Thứ Ba -> T3
        // 2026-11-04 là Thứ Tư -> T4
        // 2026-11-05 là Thứ Năm -> T5
        // 2026-11-06 là Thứ Sáu -> T6
        // 2026-11-07 là Thứ Bảy -> T7
        // 2026-11-08 là Chủ Nhật -> CN
        assertEquals("T2", StatisticsDateUtils.getDayOfWeekLabel("2026-11-02"));
        assertEquals("T3", StatisticsDateUtils.getDayOfWeekLabel("2026-11-03"));
        assertEquals("T4", StatisticsDateUtils.getDayOfWeekLabel("2026-11-04"));
        assertEquals("T5", StatisticsDateUtils.getDayOfWeekLabel("2026-11-05"));
        assertEquals("T6", StatisticsDateUtils.getDayOfWeekLabel("2026-11-06"));
        assertEquals("T7", StatisticsDateUtils.getDayOfWeekLabel("2026-11-07"));
        assertEquals("CN", StatisticsDateUtils.getDayOfWeekLabel("2026-11-08"));
    }

    @Test
    public void testGetDayOfWeekLabel_invalidAndEdgeCases() {
        assertEquals("", StatisticsDateUtils.getDayOfWeekLabel(null));
        assertEquals("", StatisticsDateUtils.getDayOfWeekLabel(""));
        assertEquals("", StatisticsDateUtils.getDayOfWeekLabel("   "));
        assertEquals("", StatisticsDateUtils.getDayOfWeekLabel("invalid-date-string"));
        assertEquals("", StatisticsDateUtils.getDayOfWeekLabel("2026-02-30")); // Ngày không tồn tại
        assertEquals("", StatisticsDateUtils.getDayOfWeekLabel("2026/11/03")); // Sai format
    }

    @Test
    public void testFormatProgress_standardValues() {
        assertEquals("68.5%", StatisticsDateUtils.formatProgress(68.5f));
        assertEquals("0.0%", StatisticsDateUtils.formatProgress(0.0f));
        assertEquals("100.0%", StatisticsDateUtils.formatProgress(100.0f));
        assertEquals("33.3%", StatisticsDateUtils.formatProgress(33.333f));
    }

    @Test
    public void testFormatProgress_edgeCasesAndGuards() {
        assertEquals("0.0%", StatisticsDateUtils.formatProgress(Float.NaN));
        assertEquals("0.0%", StatisticsDateUtils.formatProgress(Float.POSITIVE_INFINITY));
        assertEquals("0.0%", StatisticsDateUtils.formatProgress(Float.NEGATIVE_INFINITY));
        assertEquals("0.0%", StatisticsDateUtils.formatProgress(-10.0f));
        assertEquals("100.0%", StatisticsDateUtils.formatProgress(150.0f));
    }

    @Test
    public void testGetTodayDateString_format() {
        String today = StatisticsDateUtils.getTodayDateString();
        assertNotNull(today);
        assertTrue(today.matches("^\\d{4}-\\d{2}-\\d{2}$"));
    }
}
