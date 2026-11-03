package com.mad.goaltracker.notification;

import com.mad.goaltracker.models.Reminder;

import java.util.Locale;

/**
 * Model quản lý trạng thái, chuẩn hóa và đánh giá logic quyền/cấu hình cho màn hình NotificationSettings.
 * Hoàn toàn độc lập với Android UI Framework, hỗ trợ kiểm thử đơn vị tự động.
 */
public class NotificationSettingsModel {

    public enum NotificationPermissionStatus {
        GRANTED,             // Đã được cấp quyền thông báo
        DENIED,              // Bị từ chối runtime permission (API 33+)
        BLOCKED_IN_SETTINGS, // Bị tắt toàn ứng dụng hoặc tắt channel trong Cài đặt hệ thống
        NOT_REQUIRED         // API < 33 và thông báo hệ thống đang bật (không cần runtime permission)
    }

    public enum ExactAlarmStatus {
        GRANTED,             // Có quyền đặt exact alarm (API 31+)
        INEXACT_FALLBACK,    // Không có quyền exact alarm, chuyển sang dùng Inexact Alarm fallback
        NOT_REQUIRED         // API < 31, hệ thống không yêu cầu quyền đặc biệt cho exact alarm
    }

    private Reminder morningReminder;
    private Reminder eveningReminder;

    public NotificationSettingsModel() {
    }

    public NotificationSettingsModel(Reminder morningReminder, Reminder eveningReminder) {
        this.morningReminder = morningReminder;
        this.eveningReminder = eveningReminder;
    }

    public Reminder getMorningReminder() {
        return morningReminder;
    }

    public void setMorningReminder(Reminder morningReminder) {
        this.morningReminder = morningReminder;
    }

    public Reminder getEveningReminder() {
        return eveningReminder;
    }

    public void setEveningReminder(Reminder eveningReminder) {
        this.eveningReminder = eveningReminder;
    }

    /**
     * Chuẩn hóa giờ và phút thành chuỗi định dạng HH:mm theo Locale.US.
     */
    public static String formatTime(int hourOfDay, int minute) {
        if (hourOfDay < 0 || hourOfDay > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Giờ (0-23) hoặc phút (0-59) không hợp lệ: " + hourOfDay + ":" + minute);
        }
        return String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
    }

    /**
     * Kiểm tra chuỗi thời gian có hợp lệ (HH:mm, từ 00:00 đến 23:59).
     */
    public static boolean isValidTime(String timeString) {
        return ReminderTimeCalculator.isValidTime(timeString);
    }

    /**
     * Đánh giá trạng thái quyền thông báo hệ thống dựa trên API level và các cờ trạng thái.
     *
     * @param sdkInt                    Phiên bản Android SDK hiện tại (Build.VERSION.SDK_INT)
     * @param areNotificationsEnabled   Trạng thái thông báo toàn ứng dụng (NotificationManager.areNotificationsEnabled())
     * @param isRuntimePermissionGranted Quyền POST_NOTIFICATIONS đã được cấp hay chưa (API 33+)
     * @param isChannelBlocked          NotificationChannel có bị tắt (IMPORTANCE_NONE) hay không (API 26+)
     * @return NotificationPermissionStatus
     */
    public static NotificationPermissionStatus evaluateNotificationPermission(
            int sdkInt,
            boolean areNotificationsEnabled,
            boolean isRuntimePermissionGranted,
            boolean isChannelBlocked) {

        // Nếu thông báo bị tắt ở cấp độ ứng dụng hoặc Notification Channel bị vô hiệu hóa
        if (!areNotificationsEnabled || isChannelBlocked) {
            return NotificationPermissionStatus.BLOCKED_IN_SETTINGS;
        }

        // Trên Android 13 (API 33)+, kiểm tra quyền runtime POST_NOTIFICATIONS
        if (sdkInt >= 33) {
            return isRuntimePermissionGranted
                    ? NotificationPermissionStatus.GRANTED
                    : NotificationPermissionStatus.DENIED;
        }

        // Trên Android < 33, nếu areNotificationsEnabled == true thì không cần quyền runtime
        return NotificationPermissionStatus.NOT_REQUIRED;
    }

    /**
     * Đánh giá trạng thái quyền exact alarm (SCHEDULE_EXACT_ALARM).
     *
     * @param sdkInt                 Phiên bản Android SDK hiện tại
     * @param canScheduleExactAlarms Trạng thái trả về từ AlarmManager.canScheduleExactAlarms()
     * @return ExactAlarmStatus
     */
    public static ExactAlarmStatus evaluateExactAlarmPermission(
            int sdkInt,
            boolean canScheduleExactAlarms) {

        // Trên Android < 31 (Android 11 trở xuống), hệ thống không yêu cầu quyền SCHEDULE_EXACT_ALARM
        if (sdkInt < 31) {
            return ExactAlarmStatus.NOT_REQUIRED;
        }

        // Trên Android 12 (API 31)+
        return canScheduleExactAlarms
                ? ExactAlarmStatus.GRANTED
                : ExactAlarmStatus.INEXACT_FALLBACK;
    }
}
