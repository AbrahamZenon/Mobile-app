package com.mad.goaltracker;

import android.app.Application;
import android.util.Log;

import com.mad.goaltracker.notification.NotificationScheduler;
import com.mad.goaltracker.notification.ReminderNotificationHelper;

/**
 * Application class khởi tạo các thành phần hệ thống của ứng dụng GoalTracker.
 * Đảm bảo Notification Channel và lịch nhắc nhở mặc định được thiết lập ngay từ lần đầu mở ứng dụng.
 */
public class GoalTrackerApp extends Application {

    private static final String TAG = "GoalTrackerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // 1. Tạo Notification Channel ổn định (API 26+)
            ReminderNotificationHelper.createNotificationChannel(this);

            // 2. Khởi tạo NotificationScheduler và lập lịch cho các reminder đang bật
            NotificationScheduler scheduler = new NotificationScheduler(this);
            scheduler.scheduleAllEnabledReminders();

            Log.i(TAG, "GoalTrackerApp đã khởi tạo hoàn tất hệ thống Notification và Scheduler.");
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo Notification/Scheduler trong Application: " + e.getMessage(), e);
        }
    }
}
