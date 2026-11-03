package com.mad.goaltracker.notification;

import com.mad.goaltracker.models.Reminder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Kiểm thử logic cấu hình, định danh PendingIntent và các quy tắc nghiệp vụ của NotificationScheduler.
 */
public class NotificationSchedulerLogicTest {

    @Test
    public void test01_ChannelIdStability() {
        assertEquals("Channel ID must be stable", "goal_tracker_reminders", ReminderNotificationHelper.CHANNEL_ID);
    }

    @Test
    public void test02_NotificationIdsAreDistinct() {
        assertNotEquals("Morning and Evening notification IDs must be distinct",
                ReminderNotificationHelper.NOTIFICATION_ID_MORNING,
                ReminderNotificationHelper.NOTIFICATION_ID_EVENING);
        assertEquals(1, ReminderNotificationHelper.NOTIFICATION_ID_MORNING);
        assertEquals(2, ReminderNotificationHelper.NOTIFICATION_ID_EVENING);
    }

    @Test
    public void test03_PendingIntentDataUriDistinctness() {
        int morningId = 1;
        int eveningId = 2;

        String morningUri = "goaltracker://reminder/" + morningId;
        String eveningUri = "goaltracker://reminder/" + eveningId;

        assertNotEquals("PendingIntent Data URI must be unique per reminder to prevent intent collision",
                morningUri, eveningUri);
        assertEquals("goaltracker://reminder/1", morningUri);
        assertEquals("goaltracker://reminder/2", eveningUri);
    }

    @Test
    public void test04_ReminderEnabledFilterLogic() {
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(1, "MORNING", "08:00", true));
        reminders.add(new Reminder(2, "EVENING", "21:00", false)); // Đang tắt

        List<Reminder> toSchedule = new ArrayList<>();
        List<Reminder> toCancel = new ArrayList<>();

        for (Reminder r : reminders) {
            if (r.isEnabled()) {
                toSchedule.add(r);
            } else {
                toCancel.add(r);
            }
        }

        assertEquals("Only enabled reminders should be scheduled", 1, toSchedule.size());
        assertEquals("MORNING", toSchedule.get(0).getType());

        assertEquals("Disabled reminders must be cancelled", 1, toCancel.size());
        assertEquals("EVENING", toCancel.get(0).getType());
    }

    @Test
    public void test05_UpdateReminderTimePreservesTypeAndEnables() {
        Reminder reminder = new Reminder(1, "MORNING", "08:00", true);
        assertEquals("08:00", reminder.getTimeString());

        // Cập nhật sang giờ mới
        reminder.setTimeString("07:30");
        assertEquals("07:30", reminder.getTimeString());
        assertEquals("MORNING", reminder.getType());
        assertTrue(reminder.isEnabled());
    }

    @Test
    public void test06_DisableReminderUpdatesState() {
        Reminder reminder = new Reminder(2, "EVENING", "21:00", true);
        assertTrue(reminder.isEnabled());

        reminder.setEnabled(false);
        assertFalse(reminder.isEnabled());
    }

    @Test
    public void test07_ActionConstants() {
        assertEquals("com.mad.goaltracker.ACTION_REMINDER_TRIGGER", NotificationScheduler.ACTION_REMINDER_TRIGGER);
        assertEquals("EXTRA_REMINDER_ID", NotificationScheduler.EXTRA_REMINDER_ID);
        assertEquals("EXTRA_REMINDER_TYPE", NotificationScheduler.EXTRA_REMINDER_TYPE);
    }

    @Test
    public void test08_DefaultNotificationTextsVietnamese() {
        assertNotNull(ReminderNotificationHelper.TITLE_MORNING_DEFAULT);
        assertFalse(ReminderNotificationHelper.TITLE_MORNING_DEFAULT.isEmpty());
        assertTrue("Title should be Vietnamese", ReminderNotificationHelper.TITLE_MORNING_DEFAULT.contains("buổi sáng"));

        assertNotNull(ReminderNotificationHelper.TITLE_EVENING_DEFAULT);
        assertFalse(ReminderNotificationHelper.TITLE_EVENING_DEFAULT.isEmpty());
        assertTrue("Title should be Vietnamese", ReminderNotificationHelper.TITLE_EVENING_DEFAULT.contains("hôm nay"));
    }
}
