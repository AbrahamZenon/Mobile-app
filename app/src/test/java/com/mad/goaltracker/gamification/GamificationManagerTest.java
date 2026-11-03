package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Badge;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử đơn vị cho GamificationManager và 8 huy hiệu thành tích sử dụng FakeGamificationDataSource.
 */
public class GamificationManagerTest {

    private FakeGamificationDataSource fakeDataSource;
    private GamificationManager gamificationManager;

    @Before
    public void setUp() {
        fakeDataSource = new FakeGamificationDataSource();
        gamificationManager = new GamificationManager(fakeDataSource);
    }

    @Test
    public void test01_NoData_NoBadgesUnlocked() {
        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertNotNull(unlocked);
        assertTrue("No badges should be unlocked when no data exists", unlocked.isEmpty());
        assertEquals(0, fakeDataSource.getUnlockedBadgesCount());
    }

    @Test
    public void test02_FirstGoal_UnlocksFirstGoalBadge() {
        fakeDataSource.totalGoals = 1;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_GOAL, unlocked.get(0).getCode());
        assertTrue(unlocked.get(0).isUnlocked());
        assertEquals("2026-11-03 10:00:00", unlocked.get(0).getUnlockedAt());
    }

    @Test
    public void test03_FirstTaskCompleted_UnlocksFirstTaskBadge() {
        fakeDataSource.completedTasks = 1;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_TASK, unlocked.get(0).getCode());
        assertTrue(unlocked.get(0).isUnlocked());
    }

    @Test
    public void test04_FirstJournal_UnlocksFirstJournalBadge() {
        fakeDataSource.totalJournals = 1;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_JOURNAL, unlocked.get(0).getCode());
        assertTrue(unlocked.get(0).isUnlocked());
    }

    @Test
    public void test05_Streak3_UnlocksStreak3Badge() {
        // Chuỗi hoạt động 3 ngày liên tiếp
        fakeDataSource.activityDates.add("2026-11-01");
        fakeDataSource.activityDates.add("2026-11-02");
        fakeDataSource.activityDates.add("2026-11-03");

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_STREAK_3, unlocked.get(0).getCode());
    }

    @Test
    public void test06_Streak7_UnlocksBothStreak3AndStreak7() {
        // Chuỗi 7 ngày liên tiếp từ 2026-10-28 đến 2026-11-03
        for (int i = 28; i <= 31; i++) {
            fakeDataSource.activityDates.add("2026-10-" + i);
        }
        for (int i = 1; i <= 3; i++) {
            fakeDataSource.activityDates.add("2026-11-0" + i);
        }

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(2, unlocked.size());
        List<String> codes = new ArrayList<>();
        for (Badge b : unlocked) codes.add(b.getCode());

        assertTrue(codes.contains(GamificationManager.BADGE_STREAK_3));
        assertTrue(codes.contains(GamificationManager.BADGE_STREAK_7));
    }

    @Test
    public void test07_Streak30_UnlocksStreak30() {
        // Chuỗi 30 ngày trong quá khứ (tháng 9: 30 ngày)
        // Hiện tại không hoạt động -> currentStreak=0 nhưng longestStreak=30
        for (int i = 1; i <= 30; i++) {
            fakeDataSource.activityDates.add(String.format("2026-09-%02d", i));
        }

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        List<String> codes = new ArrayList<>();
        for (Badge b : unlocked) codes.add(b.getCode());

        assertTrue("Should unlock STREAK_3", codes.contains(GamificationManager.BADGE_STREAK_3));
        assertTrue("Should unlock STREAK_7", codes.contains(GamificationManager.BADGE_STREAK_7));
        assertTrue("Should unlock STREAK_30", codes.contains(GamificationManager.BADGE_STREAK_30));
    }

    @Test
    public void test08_GoalCompleted_UnlocksGoalCompletedBadge() {
        fakeDataSource.completedGoals = 1;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_GOAL_COMPLETED, unlocked.get(0).getCode());
    }

    @Test
    public void test09_Productivity10_UnlocksProductivity10Badge() {
        fakeDataSource.completedTasks = 10;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        List<String> codes = new ArrayList<>();
        for (Badge b : unlocked) codes.add(b.getCode());

        // 10 tasks đồng thời thỏa mãn FIRST_TASK và PRODUCTIVITY_10
        assertTrue(codes.contains(GamificationManager.BADGE_FIRST_TASK));
        assertTrue(codes.contains(GamificationManager.BADGE_PRODUCTIVITY_10));
    }

    @Test
    public void test10_MultipleConditionsMetSimultaneously() {
        fakeDataSource.totalGoals = 5;
        fakeDataSource.completedGoals = 2;
        fakeDataSource.completedTasks = 12;
        fakeDataSource.totalJournals = 3;
        fakeDataSource.activityDates.add("2026-11-01");
        fakeDataSource.activityDates.add("2026-11-02");
        fakeDataSource.activityDates.add("2026-11-03");

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 12:00:00");
        List<String> codes = new ArrayList<>();
        for (Badge b : unlocked) codes.add(b.getCode());

        assertTrue(codes.contains(GamificationManager.BADGE_FIRST_GOAL));
        assertTrue(codes.contains(GamificationManager.BADGE_FIRST_TASK));
        assertTrue(codes.contains(GamificationManager.BADGE_FIRST_JOURNAL));
        assertTrue(codes.contains(GamificationManager.BADGE_STREAK_3));
        assertTrue(codes.contains(GamificationManager.BADGE_GOAL_COMPLETED));
        assertTrue(codes.contains(GamificationManager.BADGE_PRODUCTIVITY_10));
        assertEquals(6, unlocked.size());
    }

    @Test
    public void test11_Idempotent_SecondEvaluationReturnsEmptyList() {
        fakeDataSource.totalGoals = 1;
        fakeDataSource.completedTasks = 1;

        // Lần 1: Mở 2 badge
        List<Badge> firstRun = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals(2, firstRun.size());

        // Lần 2: Giữ nguyên số liệu, gọi lại -> không có badge mới nào
        List<Badge> secondRun = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:05:00");
        assertTrue("Second evaluation should return empty list (idempotent)", secondRun.isEmpty());

        // Thời gian mở khóa không bị ghi đè
        Badge bg = fakeDataSource.getBadgeByCode(GamificationManager.BADGE_FIRST_GOAL);
        assertEquals("2026-11-03 10:00:00", bg.getUnlockedAt());
    }

    @Test
    public void test12_PreUnlockedBadgeNotReturnedInNewList() {
        // Đánh dấu FIRST_GOAL đã mở từ trước
        Badge bg = fakeDataSource.getBadgeByCode(GamificationManager.BADGE_FIRST_GOAL);
        bg.setUnlocked(true);
        bg.setUnlockedAt("2026-10-01 08:00:00");

        fakeDataSource.totalGoals = 2;
        fakeDataSource.totalJournals = 1;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertEquals("Only newly unlocked FIRST_JOURNAL should be returned", 1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_JOURNAL, unlocked.get(0).getCode());
        assertEquals("Pre-existing unlock time must be preserved", "2026-10-01 08:00:00", bg.getUnlockedAt());
    }

    @Test
    public void test13_TaskCountLessThan10_DoesNotUnlockProductivity10() {
        fakeDataSource.completedTasks = 9;

        List<Badge> unlocked = gamificationManager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        List<String> codes = new ArrayList<>();
        for (Badge b : unlocked) codes.add(b.getCode());

        assertTrue(codes.contains(GamificationManager.BADGE_FIRST_TASK));
        assertFalse(codes.contains(GamificationManager.BADGE_PRODUCTIVITY_10));
    }

    @Test
    public void test14_EmptyDataSource_NoCrash() {
        FakeGamificationDataSource emptyDs = new FakeGamificationDataSource();
        emptyDs.badges.clear(); // Không có badge nào trong DB

        GamificationManager gm = new GamificationManager(emptyDs);
        List<Badge> res = gm.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 10:00:00");
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    // =========================================================================
    // FAKE DATA SOURCE FOR TESTING
    // =========================================================================
    private static class FakeGamificationDataSource implements GamificationDataSource {
        int totalGoals = 0;
        int completedGoals = 0;
        int totalTasks = 0;
        int completedTasks = 0;
        int totalJournals = 0;
        List<String> activityDates = new ArrayList<>();
        Map<String, Badge> badges = new HashMap<>();

        FakeGamificationDataSource() {
            String[][] seed = {
                    {GamificationManager.BADGE_FIRST_GOAL, "Người Khởi Đầu", "Tạo mục tiêu đầu tiên", "ic_badge_start"},
                    {GamificationManager.BADGE_FIRST_TASK, "Hành Động Nhanh", "Hoàn thành nhiệm vụ đầu tiên", "ic_badge_task"},
                    {GamificationManager.BADGE_FIRST_JOURNAL, "Nhà Văn Kiên Định", "Viết nhật ký đầu tiên", "ic_badge_journal"},
                    {GamificationManager.BADGE_STREAK_3, "Kỷ Luật 3 Ngày", "Đạt chuỗi hoạt động 3 ngày liên tiếp", "ic_badge_streak3"},
                    {GamificationManager.BADGE_STREAK_7, "Kỷ Luật Thép", "Đạt chuỗi hoạt động 7 ngày liên tiếp", "ic_badge_streak7"},
                    {GamificationManager.BADGE_STREAK_30, "Chiến Binh 30 Ngày", "Đạt chuỗi hoạt động 30 ngày liên tiếp", "ic_badge_streak30"},
                    {GamificationManager.BADGE_GOAL_COMPLETED, "Người Chinh Phục", "Hoàn thành trọn vẹn một mục tiêu", "ic_badge_conquer"},
                    {GamificationManager.BADGE_PRODUCTIVITY_10, "Bậc Thầy Năng Suất", "Hoàn thành ít nhất 10 nhiệm vụ", "ic_badge_master"}
            };
            int id = 1;
            for (String[] row : seed) {
                Badge b = new Badge(id++, row[0], row[1], row[2], row[3], false, null);
                badges.put(row[0], b);
            }
        }

        @Override public int getTotalGoalsCount() { return totalGoals; }
        @Override public int getCompletedGoalsCount() { return completedGoals; }
        @Override public int getTotalTasksCount() { return totalTasks; }
        @Override public int getCompletedTasksCount() { return completedTasks; }
        @Override public int getTotalJournalsCount() { return totalJournals; }
        @Override public List<String> getAllActivityDates() { return new ArrayList<>(activityDates); }
        @Override public List<Badge> getAllBadges() { return new ArrayList<>(badges.values()); }
        @Override public Badge getBadgeByCode(String code) { return badges.get(code); }

        @Override
        public boolean unlockBadge(String code, String unlockedAt) {
            Badge b = badges.get(code);
            if (b != null && !b.isUnlocked()) {
                b.setUnlocked(true);
                b.setUnlockedAt(unlockedAt);
                return true;
            }
            return false;
        }

        @Override
        public int getUnlockedBadgesCount() {
            int count = 0;
            for (Badge b : badges.values()) {
                if (b.isUnlocked()) count++;
            }
            return count;
        }
    }
}
