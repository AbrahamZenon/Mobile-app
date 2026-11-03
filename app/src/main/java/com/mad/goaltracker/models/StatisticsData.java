package com.mad.goaltracker.models;

import com.mad.goaltracker.utils.StatisticsDateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Model đóng gói toàn bộ số liệu thống kê cho màn hình StatisticsActivity.
 */
public class StatisticsData implements Serializable {

    // 5 danh mục chuẩn cố định của GoalTracker
    public static final List<String> STANDARD_CATEGORIES = Collections.unmodifiableList(
            Arrays.asList("Sức khỏe", "Công việc", "Học tập", "Tài chính", "Sở thích")
    );

    private final int totalGoals;
    private final int completedGoals;
    private final int overdueGoals;
    private final int totalTasks;
    private final int completedTasks;
    private final float averageProgress;
    private final Map<String, Integer> categoryCounts;
    private final Map<String, Integer> weeklyCounts;
    private final List<String> weeklyDates;

    public StatisticsData(int totalGoals, int completedGoals, int overdueGoals,
                          int totalTasks, int completedTasks, float averageProgress,
                          Map<String, Integer> categoryCounts,
                          Map<String, Integer> weeklyCounts,
                          List<String> weeklyDates) {
        this.totalGoals = Math.max(0, totalGoals);
        this.completedGoals = Math.max(0, completedGoals);
        this.overdueGoals = Math.max(0, overdueGoals);
        this.totalTasks = Math.max(0, totalTasks);
        this.completedTasks = Math.max(0, completedTasks);
        this.averageProgress = Float.isNaN(averageProgress) ? 0.0f : Math.max(0.0f, Math.min(100.0f, averageProgress));

        // Chuẩn hóa Category Map: Đảm bảo luôn đủ 5 danh mục chuẩn dù Database trả thiếu
        Map<String, Integer> normalizedCategories = new LinkedHashMap<>();
        for (String cat : STANDARD_CATEGORIES) {
            int count = (categoryCounts != null && categoryCounts.containsKey(cat))
                    ? Math.max(0, categoryCounts.get(cat))
                    : 0;
            normalizedCategories.put(cat, count);
        }
        this.categoryCounts = Collections.unmodifiableMap(normalizedCategories);

        // Chuẩn hóa Weekly Dates và Weekly Counts
        List<String> normDates = (weeklyDates != null) ? new ArrayList<>(weeklyDates) : Collections.emptyList();
        this.weeklyDates = Collections.unmodifiableList(normDates);

        Map<String, Integer> normWeekly = new LinkedHashMap<>();
        for (String d : this.weeklyDates) {
            int c = (weeklyCounts != null && weeklyCounts.containsKey(d))
                    ? Math.max(0, weeklyCounts.get(d))
                    : 0;
            normWeekly.put(d, c);
        }
        this.weeklyCounts = Collections.unmodifiableMap(normWeekly);
    }

    public int getTotalGoals() {
        return totalGoals;
    }

    public int getCompletedGoals() {
        return completedGoals;
    }

    public int getOverdueGoals() {
        return overdueGoals;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public float getAverageProgress() {
        return averageProgress;
    }

    public String getFormattedAverageProgress() {
        return StatisticsDateUtils.formatProgress(averageProgress);
    }

    public Map<String, Integer> getCategoryCounts() {
        return categoryCounts;
    }

    public int getCategoryCount(String category) {
        return categoryCounts.getOrDefault(category, 0);
    }

    /**
     * Tính phần trăm của danh mục trên tổng số mục tiêu.
     * Chống chia cho 0: trả về 0.0f nếu tổng mục tiêu = 0.
     */
    public float getCategoryPercentage(String category) {
        if (totalGoals == 0) {
            return 0.0f;
        }
        int count = getCategoryCount(category);
        return ((float) count / totalGoals) * 100.0f;
    }

    public boolean hasGoals() {
        return totalGoals > 0;
    }

    public Map<String, Integer> getWeeklyCounts() {
        return weeklyCounts;
    }

    public List<String> getWeeklyDates() {
        return weeklyDates;
    }

    /**
     * Kiểm tra xem trong 7 ngày gần nhất có ít nhất 1 task hoàn thành hay không.
     */
    public boolean hasWeeklyCompletedTasks() {
        for (int count : weeklyCounts.values()) {
            if (count > 0) return true;
        }
        return false;
    }

    public int getTotalWeeklyCompletedTasks() {
        int sum = 0;
        for (int count : weeklyCounts.values()) {
            sum += count;
        }
        return sum;
    }
}
