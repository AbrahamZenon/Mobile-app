package com.mad.goaltracker.models;

import java.io.Serializable;

/**
 * Data Model bất biến (immutable) phục vụ tích hợp giữa Gamification Engine
 * và Dashboard của MainActivity (Thành viên 1).
 */
public class GamificationDashboardData implements Serializable {

    private final int currentStreak;
    private final int longestStreak;
    private final Quote dailyQuote;
    private final int unlockedBadgesCount;
    private final int totalBadgesCount;

    public GamificationDashboardData(int currentStreak, int longestStreak,
                                     Quote dailyQuote, int unlockedBadgesCount,
                                     int totalBadgesCount) {
        this.currentStreak = Math.max(0, currentStreak);
        this.longestStreak = Math.max(0, longestStreak);
        this.dailyQuote = (dailyQuote != null)
                ? dailyQuote
                : new Quote("Mỗi bước đi nhỏ hôm nay đều đưa bạn đến gần hơn với mục tiêu lớn ngày mai.", "GoalTracker");
        this.unlockedBadgesCount = Math.max(0, unlockedBadgesCount);
        this.totalBadgesCount = Math.max(0, totalBadgesCount);
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public Quote getDailyQuote() {
        return dailyQuote;
    }

    public int getUnlockedBadgesCount() {
        return unlockedBadgesCount;
    }

    public int getTotalBadgesCount() {
        return totalBadgesCount;
    }

    public boolean hasActiveStreak() {
        return currentStreak > 0;
    }

    /**
     * Tính tỷ lệ phần trăm huy hiệu đã mở khóa, có bảo vệ chia cho 0.
     */
    public float getBadgeProgressPercentage() {
        if (totalBadgesCount == 0) {
            return 0.0f;
        }
        return Math.min(100.0f, ((float) unlockedBadgesCount / totalBadgesCount) * 100.0f);
    }
}
