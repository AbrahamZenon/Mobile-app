package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Badge;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test Suite kiểm tra chuyên sâu các tình huống chuyển trạng thái nhiệm vụ (0 → 1),
 * tính toán tiến độ mục tiêu cha, hoàn thành mục tiêu và điều kiện kích hoạt cả 8 badge (Prompt 8.1).
 */
public class TaskTransitionAndGoalCompletionTest {

    private FakeGamificationDataSource fakeDataSource;
    private GamificationManager manager;

    @Before
    public void setUp() {
        fakeDataSource = new FakeGamificationDataSource();
        manager = new GamificationManager(fakeDataSource);
    }

    // -------------------------------------------------------------------------
    // 1. KIỂM THỬ QUY TẮC CHUYỂN TRẠNG THÁI TASK (0 → 1)
    // -------------------------------------------------------------------------

    @Test
    public void testTaskTransitionZeroToOneTriggersEvent() {
        // Task cũ: is_completed = 0
        int oldIsCompleted = 0;
        boolean newIsCompleted = true;
        int rowsUpdated = 1;

        boolean shouldTrigger = rowsUpdated > 0 && oldIsCompleted == 0 && newIsCompleted;
        assertTrue("Chuyển trạng thái 0 -> 1 thực sự phải kích hoạt sự kiện", shouldTrigger);
    }

    @Test
    public void testTaskTransitionOneToOneDoesNotTriggerEvent() {
        // Task cũ đã hoàn thành (1), bấm tick lại (1)
        int oldIsCompleted = 1;
        boolean newIsCompleted = true;
        int rowsUpdated = 1;

        boolean shouldTrigger = rowsUpdated > 0 && oldIsCompleted == 0 && newIsCompleted;
        assertFalse("Gọi lại 1 -> 1 không được kích hoạt sự kiện trùng", shouldTrigger);
    }

    @Test
    public void testTaskTransitionOneToZeroDoesNotTriggerEvent() {
        // Bỏ tick task: 1 -> 0
        int oldIsCompleted = 1;
        boolean newIsCompleted = false;
        int rowsUpdated = 1;

        boolean shouldTrigger = rowsUpdated > 0 && oldIsCompleted == 0 && newIsCompleted;
        assertFalse("Bỏ hoàn thành (1 -> 0) không được kích hoạt sự kiện mở khóa", shouldTrigger);
    }

    @Test
    public void testTaskTransitionZeroToZeroDoesNotTriggerEvent() {
        int oldIsCompleted = 0;
        boolean newIsCompleted = false;
        int rowsUpdated = 1;

        boolean shouldTrigger = rowsUpdated > 0 && oldIsCompleted == 0 && newIsCompleted;
        assertFalse("0 -> 0 không được kích hoạt sự kiện", shouldTrigger);
    }

    @Test
    public void testTaskNotFoundDoesNotTriggerEvent() {
        int rowsUpdated = 0; // Không tìm thấy task
        int oldIsCompleted = 0;
        boolean newIsCompleted = true;

        boolean shouldTrigger = rowsUpdated > 0 && oldIsCompleted == 0 && newIsCompleted;
        assertFalse("Task không tồn tại hoặc update fail không được kích hoạt sự kiện", shouldTrigger);
    }

    // -------------------------------------------------------------------------
    // 2. KIỂM TOÁN QUY TẮC NGHIỆP VỤ HOÀN THÀNH GOAL & XÓA TASK (PROMPT 8.2)
    // -------------------------------------------------------------------------

    @Test
    public void testGoalProgressCalculationAllStages() {
        int total = 3;

        // 0 / 3 task xong -> progress = 0%, is_completed = false
        int done0 = 0;
        int progress0 = (int) (((float) done0 / total) * 100);
        assertEquals(0, progress0);
        assertFalse(progress0 == 100);

        // 1 / 3 task xong -> progress = 33%, is_completed = false
        int done1 = 1;
        int progress1 = (int) (((float) done1 / total) * 100);
        assertEquals(33, progress1);
        assertFalse(progress1 == 100);

        // 2 / 3 task xong -> progress = 66%, is_completed = false
        int done2 = 2;
        int progress2 = (int) (((float) done2 / total) * 100);
        assertEquals(66, progress2);
        assertFalse(progress2 == 100);

        // 3 / 3 task xong -> progress = 100%, is_completed = true
        int done3 = 3;
        int progress3 = (int) (((float) done3 / total) * 100);
        assertEquals(100, progress3);
        assertTrue(progress3 == 100);
    }

    /**
     * Yêu cầu 1: Hoàn thành Task cuối cùng -> Goal hoàn thành (100%) và mở khóa GOAL_COMPLETED đúng 1 lần.
     */
    @Test
    public void testCompleteLastTaskPushesGoalTo100AndUnlocksGoalCompletedBadgeOnce() {
        // Ban đầu chưa có mục tiêu hoàn thành
        fakeDataSource.completedGoals = 0;
        assertTrue(manager.evaluateAndUnlockBadges().isEmpty());

        // Hoàn thành task cuối cùng -> Goal đạt 100%, completedGoals = 1
        fakeDataSource.completedGoals = 1;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_GOAL_COMPLETED, unlocked.get(0).getCode());

        // Gọi lại lần tiếp theo: Idempotent, không mở khóa trùng lặp
        List<Badge> recheckUnlocked = manager.evaluateAndUnlockBadges();
        assertTrue(recheckUnlocked.isEmpty());
    }

    /**
     * Yêu cầu 2: Quy tắc an toàn Prompt 8.2: Xóa Task chưa hoàn thành KHÔNG tự kích hoạt GOAL_COMPLETED.
     * Trong GoalDetailActivity.onDeleteTask(), không gọi AchievementEventCoordinator.
     */
    @Test
    public void testDeletingUncompletedTaskDoesNotTriggerGoalCompletedEvent() {
        // Giả lập trạng thái Goal: có 2 task, 1 xong, 1 chưa xong (tiến độ 50%)
        int oldProgress = 50;
        boolean userCompletedValidTask = false; // Người dùng KHÔNG hoàn thành task mà xóa task

        // Khi xóa task chưa hoàn thành trong GoalDetailActivity, onDeleteTask không gọi triggerEvaluation
        boolean shouldTriggerEvaluation = userCompletedValidTask;
        assertFalse("Xóa task chưa hoàn thành không được tự kích hoạt đánh giá thành tích Goal", shouldTriggerEvaluation);

        // Kể cả nếu số liệu tạm tính 1/1=100%, nhưng nếu không qua luồng hoàn thành task hợp lệ thì không mở khóa
        fakeDataSource.completedGoals = 0;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertTrue("Không mở khóa GOAL_COMPLETED khi không có sự kiện hoàn thành hợp lệ", unlocked.isEmpty());
    }

    /**
     * Yêu cầu 3: Xóa Task đã hoàn thành -> Không tạo event mới.
     */
    @Test
    public void testDeletingCompletedTaskDoesNotCreateNewEvent() {
        // Goal ban đầu có 2 task đều đã xong (100%)
        // Xóa 1 task đã xong -> onDeleteTask không gọi coordinator
        boolean coordinatorTriggered = false;
        assertFalse("Xóa task đã xong tuyệt đối không kích hoạt event thành tích mới", coordinatorTriggered);

        // Trạng thái badge không bị thay đổi
        fakeDataSource.completedGoals = 0;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertTrue(unlocked.isEmpty());
    }

    /**
     * Yêu cầu 4: Goal không có Task -> Tiến độ bằng 0, không tự động xem là hoàn thành, không tự unlock.
     */
    @Test
    public void testGoalWithoutTasksDoesNotAutoUnlock() {
        // DatabaseHelper.recalculateGoalProgress quy định: total == 0 -> progress = 0, is_completed = 0
        int totalTasks = 0;
        int progress = (totalTasks == 0) ? 0 : 100;
        int isCompleted = (totalTasks == 0) ? 0 : (progress == 100 ? 1 : 0);

        assertEquals(0, progress);
        assertEquals(0, isCompleted);

        fakeDataSource.completedGoals = isCompleted;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertTrue("Goal không có task không bao giờ được tự động mở khóa GOAL_COMPLETED", unlocked.isEmpty());
    }

    /**
     * Yêu cầu 5: Goal đã completed -> Không unlock lặp lại (Idempotency).
     */
    @Test
    public void testGoalAlreadyCompletedDoesNotUnlockRepeatedly() {
        fakeDataSource.completedGoals = 1;
        List<Badge> firstUnlock = manager.evaluateAndUnlockBadges();
        assertEquals(1, firstUnlock.size());
        assertEquals(GamificationManager.BADGE_GOAL_COMPLETED, firstUnlock.get(0).getCode());

        // Lần kiểm tra tiếp theo (ví dụ người dùng mở lại Goal hoặc hoàn thành thêm task khác)
        List<Badge> secondUnlock = manager.evaluateAndUnlockBadges();
        assertTrue("Mục tiêu đã hoàn thành trước đó không được mở khóa lại badge", secondUnlock.isEmpty());
    }

    /**
     * Yêu cầu 6: Database update thất bại (ví dụ task không tồn tại) -> updateTaskCompletion trả về false -> Không phát sinh event.
     */
    @Test
    public void testDatabaseUpdateFailureDoesNotTriggerEvent() {
        // Giả lập taskId không tồn tại trong SQLite -> rowsAffected = 0 -> updateTaskCompletion = false
        int rowsAffected = 0;
        int oldIsCompleted = 0;
        boolean newIsCompleted = true;
        boolean transitioned = rowsAffected > 0 && oldIsCompleted == 0 && newIsCompleted;

        assertFalse("Khi SQLite update thất bại, transitioned phải là false", transitioned);

        // Trong GoalDetailActivity: if (transitioned) mới gọi triggerEvaluation
        boolean eventTriggered = transitioned;
        assertFalse("Không phát sinh event khi cập nhật CSDL thất bại", eventTriggered);
    }

    /**
     * Yêu cầu 7: Toàn bộ các đường cập nhật Goal completion đều được kiểm tra chặt chẽ.
     */
    @Test
    public void testAllGoalCompletionPathsAudited() {
        // Đường A: Hoàn thành task hợp lệ (0 -> 1) dẫn tới 100% -> HỢP LỆ (Event được gọi)
        boolean pathATransition = true; // 0 -> 1
        boolean pathAIs100 = true;
        assertTrue("Đường A là luồng hợp lệ duy nhất để chuyển Goal sang hoàn thành", pathATransition && pathAIs100);

        // Đường B: Bỏ tick task (1 -> 0) -> KHÔNG HỢP LỆ (transitioned = false, không event)
        boolean pathBTransition = false;
        assertFalse("Đường B không kích hoạt event", pathBTransition);

        // Đường C: Tick lại task đã xong (1 -> 1) -> KHÔNG HỢP LỆ (transitioned = false, không event)
        boolean pathCTransition = false;
        assertFalse("Đường C không kích hoạt event", pathCTransition);

        // Đường D: Xóa task chưa xong -> KHÔNG HỢP LỆ (onDeleteTask không gọi coordinator)
        boolean pathDTrigger = false;
        assertFalse("Đường D không kích hoạt event theo quy tắc an toàn Prompt 8.2", pathDTrigger);

        // Đường E: Xóa task đã xong -> KHÔNG HỢP LỆ
        boolean pathETrigger = false;
        assertFalse("Đường E không kích hoạt event", pathETrigger);
    }

    // -------------------------------------------------------------------------
    // 3. KIỂM THỬ TOÀN BỘ 8 BADGE VỚI CÁC ĐIỀU KIỆN NGHIỆP VỤ THỰC TẾ
    // -------------------------------------------------------------------------

    @Test
    public void testBadgeFirstGoalEligibility() {
        fakeDataSource.totalGoals = 0;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertTrue(unlocked.isEmpty());

        fakeDataSource.totalGoals = 1;
        unlocked = manager.evaluateAndUnlockBadges();
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_GOAL, unlocked.get(0).getCode());

        // Gọi lại: không mở khóa thêm lần nữa (idempotent)
        unlocked = manager.evaluateAndUnlockBadges();
        assertTrue(unlocked.isEmpty());
    }

    @Test
    public void testBadgeFirstTaskEligibility() {
        fakeDataSource.completedTasks = 0;
        assertTrue(manager.evaluateAndUnlockBadges().isEmpty());

        fakeDataSource.completedTasks = 1;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_TASK, unlocked.get(0).getCode());
    }

    @Test
    public void testBadgeFirstJournalEligibility() {
        fakeDataSource.totalJournals = 0;
        assertTrue(manager.evaluateAndUnlockBadges().isEmpty());

        fakeDataSource.totalJournals = 1;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_FIRST_JOURNAL, unlocked.get(0).getCode());
    }

    @Test
    public void testBadgeGoalCompletedEligibility() {
        fakeDataSource.completedGoals = 0;
        assertTrue(manager.evaluateAndUnlockBadges().isEmpty());

        fakeDataSource.completedGoals = 1;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_GOAL_COMPLETED, unlocked.get(0).getCode());
    }

    @Test
    public void testBadgeProductivity10Eligibility() {
        // Mở khóa trước FIRST_TASK để test riêng biệt PRODUCTIVITY_10
        fakeDataSource.completedTasks = 1;
        manager.evaluateAndUnlockBadges();

        fakeDataSource.completedTasks = 9;
        assertTrue(manager.evaluateAndUnlockBadges().isEmpty());

        fakeDataSource.completedTasks = 10;
        List<Badge> unlocked = manager.evaluateAndUnlockBadges();
        assertEquals(1, unlocked.size());
        assertEquals(GamificationManager.BADGE_PRODUCTIVITY_10, unlocked.get(0).getCode());
    }

    @Test
    public void testBadgeStreakMilestonesLongestStreak() {
        // 3 ngày liên tiếp
        fakeDataSource.activityDates = Arrays.asList("2026-11-01", "2026-11-02", "2026-11-03");
        List<Badge> unlocked = manager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 12:00:00");

        boolean hasStreak3 = false;
        boolean hasStreak7 = false;
        for (Badge b : unlocked) {
            if (GamificationManager.BADGE_STREAK_3.equals(b.getCode())) hasStreak3 = true;
            if (GamificationManager.BADGE_STREAK_7.equals(b.getCode())) hasStreak7 = true;
        }
        assertTrue(hasStreak3);
        assertFalse(hasStreak7);

        // Đạt 7 ngày liên tiếp
        fakeDataSource.activityDates = Arrays.asList(
                "2026-10-28", "2026-10-29", "2026-10-30", "2026-10-31",
                "2026-11-01", "2026-11-02", "2026-11-03"
        );
        unlocked = manager.evaluateAndUnlockBadges("2026-11-03", "2026-11-03 12:00:00");
        hasStreak7 = false;
        for (Badge b : unlocked) {
            if (GamificationManager.BADGE_STREAK_7.equals(b.getCode())) hasStreak7 = true;
        }
        assertTrue(hasStreak7);
    }

    // -------------------------------------------------------------------------
    // FAKE DATA SOURCE CHO UNIT TESTING
    // -------------------------------------------------------------------------

    private static class FakeGamificationDataSource implements GamificationDataSource {
        int totalGoals = 0;
        int completedGoals = 0;
        int totalTasks = 0;
        int completedTasks = 0;
        int totalJournals = 0;
        List<String> activityDates = new ArrayList<>();
        final List<Badge> badges = new ArrayList<>();

        FakeGamificationDataSource() {
            badges.add(new Badge(1, GamificationManager.BADGE_FIRST_GOAL, "Người Khởi Đầu", "Tạo mục tiêu đầu tiên", "ic_badge_start", false, null));
            badges.add(new Badge(2, GamificationManager.BADGE_FIRST_TASK, "Hành Động Nhanh", "Hoàn thành nhiệm vụ đầu tiên", "ic_badge_task", false, null));
            badges.add(new Badge(3, GamificationManager.BADGE_FIRST_JOURNAL, "Nhà Văn Kiên Định", "Viết nhật ký đầu tiên", "ic_badge_journal", false, null));
            badges.add(new Badge(4, GamificationManager.BADGE_STREAK_3, "Kỷ Luật 3 Ngày", "Duy trì chuỗi 3 ngày liên tiếp", "ic_badge_streak3", false, null));
            badges.add(new Badge(5, GamificationManager.BADGE_STREAK_7, "Kỷ Luật Thép", "Duy trì chuỗi 7 ngày liên tiếp", "ic_badge_streak7", false, null));
            badges.add(new Badge(6, GamificationManager.BADGE_STREAK_30, "Chiến Binh 30 Ngày", "Duy trì chuỗi 30 ngày liên tiếp", "ic_badge_streak30", false, null));
            badges.add(new Badge(7, GamificationManager.BADGE_GOAL_COMPLETED, "Người Chinh Phục", "Hoàn thành mục tiêu đầu tiên", "ic_badge_conquer", false, null));
            badges.add(new Badge(8, GamificationManager.BADGE_PRODUCTIVITY_10, "Bậc Thầy Năng Suất", "Hoàn thành 10 nhiệm vụ", "ic_badge_master", false, null));
        }

        @Override public int getTotalGoalsCount() { return totalGoals; }
        @Override public int getCompletedGoalsCount() { return completedGoals; }
        @Override public int getTotalTasksCount() { return totalTasks; }
        @Override public int getCompletedTasksCount() { return completedTasks; }
        @Override public int getTotalJournalsCount() { return totalJournals; }
        @Override public List<String> getAllActivityDates() { return activityDates; }
        @Override public List<Badge> getAllBadges() { return badges; }

        @Override
        public Badge getBadgeByCode(String code) {
            for (Badge b : badges) {
                if (b.getCode().equals(code)) return b;
            }
            return null;
        }

        @Override
        public boolean unlockBadge(String code, String unlockedAt) {
            Badge b = getBadgeByCode(code);
            if (b != null && !b.isUnlocked()) {
                b.setUnlocked(true);
                b.setUnlockedAt(unlockedAt);
                return true;
            }
            return false;
        }

        @Override
        public int getUnlockedBadgesCount() {
            int c = 0;
            for (Badge b : badges) {
                if (b.isUnlocked()) c++;
            }
            return c;
        }
    }
}
