package com.mad.goaltracker.notification;

import com.mad.goaltracker.models.Reminder;
import com.mad.goaltracker.notification.NotificationSettingsModel.ExactAlarmStatus;
import com.mad.goaltracker.notification.NotificationSettingsModel.NotificationPermissionStatus;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test Suite kiểm tra toàn bộ logic nghiệp vụ, chuẩn hóa dữ liệu,
 * tính độc lập giữa hai reminder và ma trận quyền runtime cho NotificationSettings (Prompt 7).
 */
public class NotificationSettingsDataTest {

    private Reminder morningReminder;
    private Reminder eveningReminder;
    private NotificationSettingsModel settingsModel;

    @Before
    public void setUp() {
        morningReminder = new Reminder(1, "MORNING", "08:00", true);
        eveningReminder = new Reminder(2, "EVENING", "21:00", true);
        settingsModel = new NotificationSettingsModel(morningReminder, eveningReminder);
    }

    // =========================================================================
    // 1. KIỂM TRA MAPPING VÀ TÍNH TOÀN VẸN DỮ LIỆU REMINDER
    // =========================================================================

    @Test
    public void testRemindersInitialDataMapping() {
        assertNotNull(settingsModel.getMorningReminder());
        assertNotNull(settingsModel.getEveningReminder());

        assertEquals(1, settingsModel.getMorningReminder().getId());
        assertEquals("MORNING", settingsModel.getMorningReminder().getType());
        assertEquals("08:00", settingsModel.getMorningReminder().getTimeString());
        assertTrue(settingsModel.getMorningReminder().isEnabled());

        assertEquals(2, settingsModel.getEveningReminder().getId());
        assertEquals("EVENING", settingsModel.getEveningReminder().getType());
        assertEquals("21:00", settingsModel.getEveningReminder().getTimeString());
        assertTrue(settingsModel.getEveningReminder().isEnabled());
    }

    @Test
    public void testRemindersNullHandlingWithoutFakeFallback() {
        NotificationSettingsModel emptyModel = new NotificationSettingsModel(null, null);
        assertNull(emptyModel.getMorningReminder());
        assertNull(emptyModel.getEveningReminder());
    }

    // =========================================================================
    // 2. KIỂM TRA CHUẨN HÓA VÀ VALIDATE THỜI GIAN (HH:mm)
    // =========================================================================

    @Test
    public void testFormatTimeStandardCases() {
        assertEquals("08:00", NotificationSettingsModel.formatTime(8, 0));
        assertEquals("00:00", NotificationSettingsModel.formatTime(0, 0));
        assertEquals("13:05", NotificationSettingsModel.formatTime(13, 5));
        assertEquals("21:30", NotificationSettingsModel.formatTime(21, 30));
        assertEquals("23:59", NotificationSettingsModel.formatTime(23, 59));
        assertEquals("09:09", NotificationSettingsModel.formatTime(9, 9));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatTimeInvalidHourNegative() {
        NotificationSettingsModel.formatTime(-1, 30);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatTimeInvalidHourOver23() {
        NotificationSettingsModel.formatTime(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatTimeInvalidMinuteNegative() {
        NotificationSettingsModel.formatTime(10, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatTimeInvalidMinuteOver59() {
        NotificationSettingsModel.formatTime(10, 60);
    }

    @Test
    public void testIsValidTimeValidFormats() {
        assertTrue(NotificationSettingsModel.isValidTime("00:00"));
        assertTrue(NotificationSettingsModel.isValidTime("08:00"));
        assertTrue(NotificationSettingsModel.isValidTime("13:05"));
        assertTrue(NotificationSettingsModel.isValidTime("21:30"));
        assertTrue(NotificationSettingsModel.isValidTime("23:59"));
    }

    @Test
    public void testIsValidTimeInvalidFormats() {
        assertFalse(NotificationSettingsModel.isValidTime(null));
        assertFalse(NotificationSettingsModel.isValidTime(""));
        assertFalse(NotificationSettingsModel.isValidTime("   "));
        assertFalse(NotificationSettingsModel.isValidTime("8:00"));     // Thiếu 0 dẫn đầu
        assertFalse(NotificationSettingsModel.isValidTime("08:0"));     // Thiếu phút
        assertFalse(NotificationSettingsModel.isValidTime("24:00"));    // Giờ 24 không hợp lệ
        assertFalse(NotificationSettingsModel.isValidTime("08:60"));    // Phút 60 không hợp lệ
        assertFalse(NotificationSettingsModel.isValidTime("25:10"));
        assertFalse(NotificationSettingsModel.isValidTime("-01:00"));
        assertFalse(NotificationSettingsModel.isValidTime("morning"));
    }

    // =========================================================================
    // 3. KIỂM TRA TÍNH ĐỘC LẬP KHI BẬT / TẮT (TOGGLE ISOLATION)
    // =========================================================================

    @Test
    public void testToggleMorningDoesNotAffectEvening() {
        // Tắt Morning
        morningReminder.setEnabled(false);
        assertFalse(morningReminder.isEnabled());
        // Evening vẫn phải BẬT và giữ nguyên cấu hình
        assertTrue(eveningReminder.isEnabled());
        assertEquals("21:00", eveningReminder.getTimeString());
        assertEquals("EVENING", eveningReminder.getType());
    }

    @Test
    public void testToggleEveningDoesNotAffectMorning() {
        // Tắt Evening
        eveningReminder.setEnabled(false);
        assertFalse(eveningReminder.isEnabled());
        // Morning vẫn phải BẬT và giữ nguyên cấu hình
        assertTrue(morningReminder.isEnabled());
        assertEquals("08:00", morningReminder.getTimeString());
        assertEquals("MORNING", morningReminder.getType());
    }

    @Test
    public void testTogglePreservesTimeAndType() {
        morningReminder.setEnabled(false);
        assertEquals("08:00", morningReminder.getTimeString());
        assertEquals("MORNING", morningReminder.getType());
        assertEquals(1, morningReminder.getId());

        morningReminder.setEnabled(true);
        assertEquals("08:00", morningReminder.getTimeString());
        assertEquals("MORNING", morningReminder.getType());
        assertEquals(1, morningReminder.getId());
    }

    @Test
    public void testChangeTimePreservesEnabledState() {
        // Reminder đang disabled
        morningReminder.setEnabled(false);
        morningReminder.setTimeString("07:15");

        assertEquals("07:15", morningReminder.getTimeString());
        assertFalse("Đổi giờ không được tự động bật reminder nếu đang tắt", morningReminder.isEnabled());
    }

    // =========================================================================
    // 4. KIỂM TRA ĐÁNH GIÁ MA TRẬN QUYỀN POST_NOTIFICATIONS
    // =========================================================================

    @Test
    public void testNotificationPermissionPreApi33Enabled() {
        // API 24-32, notifications enabled -> NOT_REQUIRED
        NotificationPermissionStatus status = NotificationSettingsModel.evaluateNotificationPermission(
                24, true, false, false
        );
        assertEquals(NotificationPermissionStatus.NOT_REQUIRED, status);

        status = NotificationSettingsModel.evaluateNotificationPermission(
                32, true, false, false
        );
        assertEquals(NotificationPermissionStatus.NOT_REQUIRED, status);
    }

    @Test
    public void testNotificationPermissionApi33Granted() {
        // API 33+, runtime granted, notifications enabled -> GRANTED
        NotificationPermissionStatus status = NotificationSettingsModel.evaluateNotificationPermission(
                33, true, true, false
        );
        assertEquals(NotificationPermissionStatus.GRANTED, status);

        status = NotificationSettingsModel.evaluateNotificationPermission(
                34, true, true, false
        );
        assertEquals(NotificationPermissionStatus.GRANTED, status);
    }

    @Test
    public void testNotificationPermissionApi33Denied() {
        // API 33+, runtime NOT granted, notifications enabled -> DENIED
        NotificationPermissionStatus status = NotificationSettingsModel.evaluateNotificationPermission(
                33, true, false, false
        );
        assertEquals(NotificationPermissionStatus.DENIED, status);
    }

    @Test
    public void testNotificationPermissionAppNotificationsDisabled() {
        // Bất kỳ API nào nếu areNotificationsEnabled == false -> BLOCKED_IN_SETTINGS
        NotificationPermissionStatus status = NotificationSettingsModel.evaluateNotificationPermission(
                24, false, true, false
        );
        assertEquals(NotificationPermissionStatus.BLOCKED_IN_SETTINGS, status);

        status = NotificationSettingsModel.evaluateNotificationPermission(
                34, false, true, false
        );
        assertEquals(NotificationPermissionStatus.BLOCKED_IN_SETTINGS, status);
    }

    @Test
    public void testNotificationPermissionChannelBlocked() {
        // Channel bị tắt trong Cài đặt hệ thống (IMPORTANCE_NONE) -> BLOCKED_IN_SETTINGS
        NotificationPermissionStatus status = NotificationSettingsModel.evaluateNotificationPermission(
                26, true, true, true
        );
        assertEquals(NotificationPermissionStatus.BLOCKED_IN_SETTINGS, status);

        status = NotificationSettingsModel.evaluateNotificationPermission(
                34, true, true, true
        );
        assertEquals(NotificationPermissionStatus.BLOCKED_IN_SETTINGS, status);
    }

    // =========================================================================
    // 5. KIỂM TRA ĐÁNH GIÁ QUYỀN EXACT ALARM (SCHEDULE_EXACT_ALARM)
    // =========================================================================

    @Test
    public void testExactAlarmPreApi31() {
        // API 24-30 không yêu cầu SCHEDULE_EXACT_ALARM -> NOT_REQUIRED
        ExactAlarmStatus status = NotificationSettingsModel.evaluateExactAlarmPermission(24, false);
        assertEquals(ExactAlarmStatus.NOT_REQUIRED, status);

        status = NotificationSettingsModel.evaluateExactAlarmPermission(30, false);
        assertEquals(ExactAlarmStatus.NOT_REQUIRED, status);
    }

    @Test
    public void testExactAlarmApi31Granted() {
        // API 31+, canScheduleExactAlarms == true -> GRANTED
        ExactAlarmStatus status = NotificationSettingsModel.evaluateExactAlarmPermission(31, true);
        assertEquals(ExactAlarmStatus.GRANTED, status);

        status = NotificationSettingsModel.evaluateExactAlarmPermission(34, true);
        assertEquals(ExactAlarmStatus.GRANTED, status);
    }

    @Test
    public void testExactAlarmApi31DeniedFallbackInexact() {
        // API 31+, canScheduleExactAlarms == false -> INEXACT_FALLBACK
        ExactAlarmStatus status = NotificationSettingsModel.evaluateExactAlarmPermission(31, false);
        assertEquals(ExactAlarmStatus.INEXACT_FALLBACK, status);

        status = NotificationSettingsModel.evaluateExactAlarmPermission(34, false);
        assertEquals(ExactAlarmStatus.INEXACT_FALLBACK, status);
    }
}
