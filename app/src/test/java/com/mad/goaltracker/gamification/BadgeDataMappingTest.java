package com.mad.goaltracker.gamification;

import com.mad.goaltracker.R;
import com.mad.goaltracker.models.Badge;
import com.mad.goaltracker.utils.BadgeIconHelper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị cho việc ánh xạ dữ liệu Badge, thứ tự hiển thị và chuyển đổi icon an toàn.
 */
public class BadgeDataMappingTest {

    @Test
    public void testIconMapping_standardIcons() {
        assertEquals(R.drawable.ic_badge_start, BadgeIconHelper.getBadgeDrawableResId("ic_badge_start"));
        assertEquals(R.drawable.ic_badge_task, BadgeIconHelper.getBadgeDrawableResId("ic_badge_task"));
        assertEquals(R.drawable.ic_badge_journal, BadgeIconHelper.getBadgeDrawableResId("ic_badge_journal"));
        assertEquals(R.drawable.ic_badge_streak3, BadgeIconHelper.getBadgeDrawableResId("ic_badge_streak3"));
        assertEquals(R.drawable.ic_badge_streak7, BadgeIconHelper.getBadgeDrawableResId("ic_badge_streak7"));
        assertEquals(R.drawable.ic_badge_streak30, BadgeIconHelper.getBadgeDrawableResId("ic_badge_streak30"));
        assertEquals(R.drawable.ic_badge_conquer, BadgeIconHelper.getBadgeDrawableResId("ic_badge_conquer"));
        assertEquals(R.drawable.ic_badge_master, BadgeIconHelper.getBadgeDrawableResId("ic_badge_master"));
    }

    @Test
    public void testIconMapping_fallbackOnNullOrUnknown() {
        assertEquals(R.drawable.ic_badge_default, BadgeIconHelper.getBadgeDrawableResId(null));
        assertEquals(R.drawable.ic_badge_default, BadgeIconHelper.getBadgeDrawableResId(""));
        assertEquals(R.drawable.ic_badge_default, BadgeIconHelper.getBadgeDrawableResId("   "));
        assertEquals(R.drawable.ic_badge_default, BadgeIconHelper.getBadgeDrawableResId("ic_unknown_icon"));
        assertEquals(R.drawable.ic_badge_default, BadgeIconHelper.getBadgeDrawableResId("badge_xyz"));
    }

    @Test
    public void testBadgeLockedState_noFakeUnlockedDate() {
        Badge lockedBadge = new Badge("FIRST_GOAL", "Người Khởi Đầu", "Tạo mục tiêu đầu tiên", "ic_badge_start");
        assertFalse(lockedBadge.isUnlocked());
        assertNull(lockedBadge.getUnlockedAt());
        assertNotNull(lockedBadge.getTitle());
        assertFalse(lockedBadge.getTitle().trim().isEmpty());
    }

    @Test
    public void testBadgeUnlockedState_validTimestamp() {
        Badge unlockedBadge = new Badge(1, "FIRST_GOAL", "Người Khởi Đầu", "Tạo mục tiêu đầu tiên",
                "ic_badge_start", true, "2026-11-03 10:15:30");
        assertTrue(unlockedBadge.isUnlocked());
        assertEquals("2026-11-03 10:15:30", unlockedBadge.getUnlockedAt());
        assertEquals("2026-11-03", unlockedBadge.getUnlockedAt().split(" ")[0]);
    }

    @Test
    public void testStableBadgeListOrdering() {
        List<Badge> badgeList = new ArrayList<>();
        badgeList.add(new Badge(1, "FIRST_GOAL", "Người Khởi Đầu", "Mô tả 1", "ic_badge_start", true, "2026-11-01 10:00:00"));
        badgeList.add(new Badge(2, "FIRST_TASK", "Hành Động Nhanh", "Mô tả 2", "ic_badge_task", false, null));
        badgeList.add(new Badge(3, "FIRST_JOURNAL", "Nhà Văn Kiên Định", "Mô tả 3", "ic_badge_journal", false, null));
        badgeList.add(new Badge(4, "STREAK_3", "Kỷ Luật 3 Ngày", "Mô tả 4", "ic_badge_streak3", false, null));
        badgeList.add(new Badge(5, "STREAK_7", "Kỷ Luật Thép", "Mô tả 5", "ic_badge_streak7", false, null));
        badgeList.add(new Badge(6, "STREAK_30", "Chiến Binh 30 Ngày", "Mô tả 6", "ic_badge_streak30", false, null));
        badgeList.add(new Badge(7, "GOAL_COMPLETED", "Người Chinh Phục", "Mô tả 7", "ic_badge_conquer", false, null));
        badgeList.add(new Badge(8, "PRODUCTIVITY_10", "Bậc Thầy Năng Suất", "Mô tả 8", "ic_badge_master", false, null));

        assertEquals(8, badgeList.size());
        assertEquals("FIRST_GOAL", badgeList.get(0).getCode());
        assertEquals("FIRST_TASK", badgeList.get(1).getCode());
        assertEquals("FIRST_JOURNAL", badgeList.get(2).getCode());
        assertEquals("STREAK_3", badgeList.get(3).getCode());
        assertEquals("STREAK_7", badgeList.get(4).getCode());
        assertEquals("STREAK_30", badgeList.get(5).getCode());
        assertEquals("GOAL_COMPLETED", badgeList.get(6).getCode());
        assertEquals("PRODUCTIVITY_10", badgeList.get(7).getCode());
    }
}
