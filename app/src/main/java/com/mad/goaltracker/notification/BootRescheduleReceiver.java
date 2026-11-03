package com.mad.goaltracker.notification;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * BroadcastReceiver khôi phục lịch nhắc nhở sau khi:
 * - Thiết bị khởi động lại (BOOT_COMPLETED)
 * - Người dùng thay đổi giờ hệ thống (TIME_SET / TIME_CHANGED)
 * - Người dùng thay đổi múi giờ (TIMEZONE_CHANGED)
 * - Ứng dụng được cập nhật (MY_PACKAGE_REPLACED)
 * - Quyền exact alarm được cấp (SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED trên Android 12+)
 */
public class BootRescheduleReceiver extends BroadcastReceiver {

    private static final String TAG = "BootRescheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        String action = intent.getAction();
        Log.i(TAG, "Nhận broadcast hệ thống: " + action);

        boolean isHandledAction = false;

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            isHandledAction = true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.equals(action)) {
                isHandledAction = true;
            }
        }

        if (!isHandledAction) {
            Log.d(TAG, "Bỏ qua broadcast action không cần xử lý: " + action);
            return;
        }

        try {
            // Đảm bảo notification channel đã sẵn sàng
            ReminderNotificationHelper.createNotificationChannel(context);

            // Đồng bộ và đặt lại toàn bộ các reminder đang bật
            NotificationScheduler scheduler = new NotificationScheduler(context);
            scheduler.scheduleAllEnabledReminders();

            Log.i(TAG, "Đã khôi phục thành công toàn bộ lịch nhắc nhở sau sự kiện: " + action);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khôi phục lịch nhắc nhở: " + e.getMessage(), e);
        }
    }
}
