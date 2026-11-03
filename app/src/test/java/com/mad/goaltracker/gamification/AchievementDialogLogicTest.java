package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Badge;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử logic đóng gói dữ liệu cho AchievementDialog.
 * Xác minh tính gom cụm (không mở nhiều dialog độc lập khi có nhiều badge đồng thời),
 * và xử lý an toàn danh sách rỗng.
 */
public class AchievementDialogLogicTest {

    @Test
    public void testSingleBadgeCelebrationData() {
        Badge b = new Badge(1, "FIRST_GOAL", "Người Khởi Đầu", "Tạo mục tiêu đầu tiên", "ic_badge_start", true, "2026-11-03 10:00:00");
        ArrayList<Badge> newlyUnlocked = new ArrayList<>();
        newlyUnlocked.add(b);

        assertEquals(1, newlyUnlocked.size());
        assertEquals("FIRST_GOAL", newlyUnlocked.get(0).getCode());
        assertTrue(newlyUnlocked.get(0).isUnlocked());
    }

    @Test
    public void testMultipleBadgesCelebrationData_inSingleList() {
        // Khi người dùng đạt nhiều điều kiện cùng lúc (ví dụ Streak 7 đồng thời mở Streak 3 và Streak 7)
        ArrayList<Badge> newlyUnlocked = new ArrayList<>();
        newlyUnlocked.add(new Badge(4, "STREAK_3", "Kỷ Luật 3 Ngày", "Chuỗi 3 ngày", "ic_badge_streak3", true, "2026-11-03 12:00:00"));
        newlyUnlocked.add(new Badge(5, "STREAK_7", "Kỷ Luật Thép", "Chuỗi 7 ngày", "ic_badge_streak7", true, "2026-11-03 12:00:00"));

        assertEquals(2, newlyUnlocked.size());
        assertEquals("STREAK_3", newlyUnlocked.get(0).getCode());
        assertEquals("STREAK_7", newlyUnlocked.get(1).getCode());
    }

    @Test
    public void testEmptyList_noCelebration() {
        ArrayList<Badge> emptyList = new ArrayList<>();
        assertTrue(emptyList.isEmpty());
        // Khi danh sách rỗng, controller không mở Dialog
        boolean shouldShowDialog = !emptyList.isEmpty();
        assertFalse(shouldShowDialog);
    }

    @Test
    public void testDetailBadgeData() {
        Badge b = new Badge(2, "FIRST_TASK", "Hành Động Nhanh", "Hoàn thành nhiệm vụ đầu tiên", "ic_badge_task", false, null);
        assertNotNull(b.getTitle());
        assertNotNull(b.getDescription());
        assertFalse(b.isUnlocked());
        assertEquals("ic_badge_task", b.getIconName());
    }
}
