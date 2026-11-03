package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.GamificationDashboardData;
import com.mad.goaltracker.models.Quote;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị cho Data Model GamificationDashboardData.
 * Đảm bảo tính bất biến, xử lý giá trị âm, bảo vệ chia cho 0 và tỷ lệ tiến độ.
 */
public class GamificationDashboardDataTest {

    @Test
    public void testDashboardData_validInputs() {
        Quote quote = new Quote("Kiên trì là chìa khóa", "Tác giả");
        GamificationDashboardData data = new GamificationDashboardData(7, 14, quote, 4, 8);

        assertEquals(7, data.getCurrentStreak());
        assertEquals(14, data.getLongestStreak());
        assertNotNull(data.getDailyQuote());
        assertEquals("Kiên trì là chìa khóa", data.getDailyQuote().getContent());
        assertEquals("Tác giả", data.getDailyQuote().getAuthor());
        assertEquals(4, data.getUnlockedBadgesCount());
        assertEquals(8, data.getTotalBadgesCount());
        assertTrue(data.hasActiveStreak());
        assertEquals(50.0f, data.getBadgeProgressPercentage(), 0.001f);
    }

    @Test
    public void testDashboardData_negativeInputsSanitized() {
        GamificationDashboardData data = new GamificationDashboardData(-5, -10, null, -2, -8);

        assertEquals(0, data.getCurrentStreak());
        assertEquals(0, data.getLongestStreak());
        assertNotNull(data.getDailyQuote()); // Tự động fallback quote hợp lệ
        assertEquals(0, data.getUnlockedBadgesCount());
        assertEquals(0, data.getTotalBadgesCount());
        assertFalse(data.hasActiveStreak());
    }

    @Test
    public void testDashboardData_zeroStreak() {
        GamificationDashboardData data = new GamificationDashboardData(0, 5, null, 0, 8);

        assertEquals(0, data.getCurrentStreak());
        assertEquals(5, data.getLongestStreak());
        assertFalse(data.hasActiveStreak());
    }

    @Test
    public void testBadgeProgressPercentage_zeroTotalGuard() {
        GamificationDashboardData data = new GamificationDashboardData(0, 0, null, 0, 0);
        assertEquals(0.0f, data.getBadgeProgressPercentage(), 0.001f);
    }

    @Test
    public void testBadgeProgressPercentage_allUnlocked() {
        GamificationDashboardData data = new GamificationDashboardData(30, 30, null, 8, 8);
        assertEquals(100.0f, data.getBadgeProgressPercentage(), 0.001f);
    }
}
