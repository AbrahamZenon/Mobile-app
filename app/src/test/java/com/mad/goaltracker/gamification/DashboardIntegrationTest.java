package com.mad.goaltracker.gamification;

import com.mad.goaltracker.activities.GamificationActivity;
import com.mad.goaltracker.activities.NotificationSettingsActivity;
import com.mad.goaltracker.activities.SecurityPinActivity;
import com.mad.goaltracker.activities.StatisticsActivity;
import com.mad.goaltracker.models.GamificationDashboardData;
import com.mad.goaltracker.models.Quote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test Suite kiểm tra các hợp đồng tích hợp Dashboard và điều hướng (Prompt 8).
 */
public class DashboardIntegrationTest {

    @Test
    public void testDashboardNavigationTargetClasses() {
        // Kiểm tra đúng Target Activity Classes theo quy tắc Intent Type-Safe
        assertEquals("StatisticsActivity", StatisticsActivity.class.getSimpleName());
        assertEquals("GamificationActivity", GamificationActivity.class.getSimpleName());
        assertEquals("NotificationSettingsActivity", NotificationSettingsActivity.class.getSimpleName());
        assertEquals("SecurityPinActivity", SecurityPinActivity.class.getSimpleName());

        assertEquals("com.mad.goaltracker.activities.StatisticsActivity", StatisticsActivity.class.getName());
        assertEquals("com.mad.goaltracker.activities.GamificationActivity", GamificationActivity.class.getName());
        assertEquals("com.mad.goaltracker.activities.NotificationSettingsActivity", NotificationSettingsActivity.class.getName());
        assertEquals("com.mad.goaltracker.activities.SecurityPinActivity", SecurityPinActivity.class.getName());
    }

    @Test
    public void testDashboardDataFormattingNormalCase() {
        Quote quote = new Quote("Hành trình vạn dặm bắt đầu từ một bước chân.", "Lão Tử");
        GamificationDashboardData data = new GamificationDashboardData(5, 12, quote, 3, 8);

        assertEquals(5, data.getCurrentStreak());
        assertEquals(12, data.getLongestStreak());
        assertEquals(3, data.getUnlockedBadgesCount());
        assertEquals(8, data.getTotalBadgesCount());
        assertTrue(data.hasActiveStreak());
        assertEquals(37.5f, data.getBadgeProgressPercentage(), 0.01f);

        // Chuẩn hóa hiển thị theo format chuỗi của Dashboard
        String streakText = String.format("%d ngày liên tiếp", data.getCurrentStreak());
        String badgeText = String.format("%d / %d huy hiệu", data.getUnlockedBadgesCount(), data.getTotalBadgesCount());
        String quoteAuthor = "— " + data.getDailyQuote().getAuthor();

        assertEquals("5 ngày liên tiếp", streakText);
        assertEquals("3 / 8 huy hiệu", badgeText);
        assertEquals("— Lão Tử", quoteAuthor);
        assertEquals("Hành trình vạn dặm bắt đầu từ một bước chân.", data.getDailyQuote().getContent());
    }

    @Test
    public void testDashboardDataFormattingZeroState() {
        Quote quote = new Quote("Bắt đầu từ những gì bạn có.", "Arthur Ashe");
        GamificationDashboardData data = new GamificationDashboardData(0, 0, quote, 0, 8);

        assertEquals(0, data.getCurrentStreak());
        assertEquals(0, data.getUnlockedBadgesCount());
        assertEquals(8, data.getTotalBadgesCount());

        String streakText = String.format("%d ngày liên tiếp", data.getCurrentStreak());
        String badgeText = String.format("%d / %d huy hiệu", data.getUnlockedBadgesCount(), data.getTotalBadgesCount());

        assertEquals("0 ngày liên tiếp", streakText);
        assertEquals("0 / 8 huy hiệu", badgeText);
    }

    @Test
    public void testBadgeConditionConstantsIntegrity() {
        // Bảo đảm 8 hằng số badge định danh không bị thay đổi mã code
        assertEquals("FIRST_GOAL", GamificationManager.BADGE_FIRST_GOAL);
        assertEquals("FIRST_TASK", GamificationManager.BADGE_FIRST_TASK);
        assertEquals("FIRST_JOURNAL", GamificationManager.BADGE_FIRST_JOURNAL);
        assertEquals("STREAK_3", GamificationManager.BADGE_STREAK_3);
        assertEquals("STREAK_7", GamificationManager.BADGE_STREAK_7);
        assertEquals("STREAK_30", GamificationManager.BADGE_STREAK_30);
        assertEquals("GOAL_COMPLETED", GamificationManager.BADGE_GOAL_COMPLETED);
        assertEquals("PRODUCTIVITY_10", GamificationManager.BADGE_PRODUCTIVITY_10);
    }
}
