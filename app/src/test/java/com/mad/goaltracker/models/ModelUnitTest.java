package com.mad.goaltracker.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class ModelUnitTest {

    @Test
    public void testTaskCreationAndCompletedAt() {
        // Constructor cũ không có completedAt
        Task taskOld = new Task(1, 10, "Task Title", "Cao", "Hàng ngày", "2026-11-05", false);
        assertEquals(1, taskOld.getId());
        assertEquals(10, taskOld.getGoalId());
        assertEquals("Task Title", taskOld.getTitle());
        assertEquals("Cao", taskOld.getPriority());
        assertEquals("Hàng ngày", taskOld.getPlanType());
        assertEquals("2026-11-05", taskOld.getDueDate());
        assertFalse(taskOld.isCompleted());
        assertNull(taskOld.getCompletedAt());

        // Constructor mới có completedAt
        Task taskNew = new Task(2, 10, "Task 2", "Thấp", "Hàng tuần", "2026-11-10", true, "2026-11-02 20:15:00");
        assertTrue(taskNew.isCompleted());
        assertEquals("2026-11-02 20:15:00", taskNew.getCompletedAt());

        // Setter
        taskOld.setCompleted(true);
        taskOld.setCompletedAt("2026-11-02 21:00:00");
        assertTrue(taskOld.isCompleted());
        assertEquals("2026-11-02 21:00:00", taskOld.getCompletedAt());
    }

    @Test
    public void testBadgeModel() {
        Badge badge = new Badge(1, "FIRST_GOAL", "Người Khởi Đầu", "Tạo mục tiêu đầu tiên", "ic_badge_start", false, null);
        assertEquals(1, badge.getId());
        assertEquals("FIRST_GOAL", badge.getCode());
        assertEquals("Người Khởi Đầu", badge.getTitle());
        assertEquals("Tạo mục tiêu đầu tiên", badge.getDescription());
        assertEquals("ic_badge_start", badge.getIconName());
        assertFalse(badge.isUnlocked());
        assertNull(badge.getUnlockedAt());

        // Mở khóa badge
        badge.setUnlocked(true);
        badge.setUnlockedAt("2026-11-02 21:30:00");
        assertTrue(badge.isUnlocked());
        assertEquals("2026-11-02 21:30:00", badge.getUnlockedAt());
    }

    @Test
    public void testReminderModel() {
        Reminder reminder = new Reminder(1, "MORNING", "08:00", true);
        assertEquals(1, reminder.getId());
        assertEquals("MORNING", reminder.getType());
        assertEquals("08:00", reminder.getTimeString());
        assertTrue(reminder.isEnabled());

        reminder.setTimeString("07:30");
        reminder.setEnabled(false);
        assertEquals("07:30", reminder.getTimeString());
        assertFalse(reminder.isEnabled());
    }

    @Test
    public void testQuoteModel() {
        Quote quote = new Quote("Hành trình vạn dặm bắt đầu bằng một bước chân.", "Lão Tử");
        assertEquals("Hành trình vạn dặm bắt đầu bằng một bước chân.", quote.getContent());
        assertEquals("Lão Tử", quote.getAuthor());

        quote.setContent("Kỷ luật là cầu nối giữa mục tiêu và thành tựu.");
        quote.setAuthor("Jim Rohn");
        assertEquals("Kỷ luật là cầu nối giữa mục tiêu và thành tựu.", quote.getContent());
        assertEquals("Jim Rohn", quote.getAuthor());
    }
}
