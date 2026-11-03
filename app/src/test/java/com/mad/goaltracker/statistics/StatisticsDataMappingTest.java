package com.mad.goaltracker.statistics;

import com.mad.goaltracker.models.StatisticsData;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị cho StatisticsData.
 * Xác minh ánh xạ 6 chỉ số Overview KPIs, chuẩn hóa 5 danh mục chuẩn,
 * tính toán tỷ lệ phần trăm chống chia cho 0, và trạng thái empty state.
 */
public class StatisticsDataMappingTest {

    @Test
    public void testStatisticsData_overviewKpiMapping() {
        StatisticsData data = new StatisticsData(
                10, 6, 2,
                25, 18, 72.4f,
                null, null, null
        );

        assertEquals(10, data.getTotalGoals());
        assertEquals(6, data.getCompletedGoals());
        assertEquals(2, data.getOverdueGoals());
        assertEquals(25, data.getTotalTasks());
        assertEquals(18, data.getCompletedTasks());
        assertEquals(72.4f, data.getAverageProgress(), 0.001f);
        assertEquals("72.4%", data.getFormattedAverageProgress());
        assertTrue(data.hasGoals());
    }

    @Test
    public void testCategoryNormalization_allStandardCategoriesPresent() {
        Map<String, Integer> partialMap = new HashMap<>();
        partialMap.put("Học tập", 5);
        partialMap.put("Công việc", 3);

        StatisticsData data = new StatisticsData(
                8, 2, 0,
                15, 5, 40.0f,
                partialMap, null, null
        );

        Map<String, Integer> counts = data.getCategoryCounts();
        assertNotNull(counts);
        assertEquals(5, counts.size());

        // Kiểm tra đủ 5 danh mục chuẩn
        assertEquals(Integer.valueOf(0), counts.get("Sức khỏe"));
        assertEquals(Integer.valueOf(3), counts.get("Công việc"));
        assertEquals(Integer.valueOf(5), counts.get("Học tập"));
        assertEquals(Integer.valueOf(0), counts.get("Tài chính"));
        assertEquals(Integer.valueOf(0), counts.get("Sở thích"));
    }

    @Test
    public void testCategoryNormalization_nullCategoryMap() {
        StatisticsData data = new StatisticsData(
                0, 0, 0,
                0, 0, 0.0f,
                null, null, null
        );

        Map<String, Integer> counts = data.getCategoryCounts();
        assertNotNull(counts);
        assertEquals(5, counts.size());
        for (String cat : StatisticsData.STANDARD_CATEGORIES) {
            assertEquals(Integer.valueOf(0), counts.get(cat));
        }
    }

    @Test
    public void testCategoryPercentage_zeroDivisionGuard() {
        // Khi totalGoals = 0, getCategoryPercentage phải trả về 0.0f an toàn
        StatisticsData data = new StatisticsData(
                0, 0, 0,
                0, 0, 0.0f,
                Collections.singletonMap("Học tập", 5), null, null
        );

        assertFalse(data.hasGoals());
        assertEquals(0.0f, data.getCategoryPercentage("Học tập"), 0.001f);
        assertEquals(0.0f, data.getCategoryPercentage("Sức khỏe"), 0.001f);
    }

    @Test
    public void testCategoryPercentage_accurateCalculation() {
        // 10 goals: 5 Học tập (50%), 3 Công việc (30%), 2 Sức khỏe (20%), 0 Tài chính (0%), 0 Sở thích (0%)
        Map<String, Integer> map = new HashMap<>();
        map.put("Học tập", 5);
        map.put("Công việc", 3);
        map.put("Sức khỏe", 2);

        StatisticsData data = new StatisticsData(
                10, 4, 1,
                20, 10, 50.0f,
                map, null, null
        );

        assertEquals(50.0f, data.getCategoryPercentage("Học tập"), 0.001f);
        assertEquals(30.0f, data.getCategoryPercentage("Công việc"), 0.001f);
        assertEquals(20.0f, data.getCategoryPercentage("Sức khỏe"), 0.001f);
        assertEquals(0.0f, data.getCategoryPercentage("Tài chính"), 0.001f);
        assertEquals(0.0f, data.getCategoryPercentage("Sở thích"), 0.001f);
    }

    @Test
    public void testHasGoals_behavior() {
        StatisticsData emptyData = new StatisticsData(0, 0, 0, 0, 0, 0.0f, null, null, null);
        assertFalse(emptyData.hasGoals());

        StatisticsData hasData = new StatisticsData(1, 0, 0, 0, 0, 0.0f, null, null, null);
        assertTrue(hasData.hasGoals());
    }

    @Test
    public void testHasWeeklyCompletedTasks_andSum() {
        List<String> dates = Arrays.asList(
                "2026-10-28", "2026-10-29", "2026-10-30",
                "2026-10-31", "2026-11-01", "2026-11-02", "2026-11-03"
        );

        // Case 1: Không có task nào hoàn thành trong 7 ngày
        Map<String, Integer> emptyWeekly = new HashMap<>();
        for (String d : dates) {
            emptyWeekly.put(d, 0);
        }

        StatisticsData dataEmpty = new StatisticsData(
                5, 1, 0, 10, 2, 20.0f,
                null, emptyWeekly, dates
        );
        assertFalse(dataEmpty.hasWeeklyCompletedTasks());
        assertEquals(0, dataEmpty.getTotalWeeklyCompletedTasks());

        // Case 2: Có 3 task ngày hôm nay và 2 task hôm qua
        Map<String, Integer> activeWeekly = new HashMap<>(emptyWeekly);
        activeWeekly.put("2026-11-03", 3);
        activeWeekly.put("2026-11-02", 2);

        StatisticsData dataActive = new StatisticsData(
                5, 1, 0, 10, 2, 20.0f,
                null, activeWeekly, dates
        );
        assertTrue(dataActive.hasWeeklyCompletedTasks());
        assertEquals(5, dataActive.getTotalWeeklyCompletedTasks());
    }

    @Test
    public void testNegativeInputs_sanitizedToZero() {
        StatisticsData data = new StatisticsData(
                -5, -2, -1,
                -10, -4, -20.0f,
                null, null, null
        );

        assertEquals(0, data.getTotalGoals());
        assertEquals(0, data.getCompletedGoals());
        assertEquals(0, data.getOverdueGoals());
        assertEquals(0, data.getTotalTasks());
        assertEquals(0, data.getCompletedTasks());
        assertEquals(0.0f, data.getAverageProgress(), 0.001f);
    }
}
