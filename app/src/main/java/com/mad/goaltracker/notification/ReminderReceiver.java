package com.mad.goaltracker.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Reminder;

/**
 * BroadcastReceiver nhận alarm từ AlarmManager khi đến giờ nhắc nhở.
 * Kiểm tra lại trạng thái thực tế từ SQLite trước khi gửi thông báo và tự động đặt lịch cho ngày hôm sau.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        int reminderId = intent.getIntExtra(NotificationScheduler.EXTRA_REMINDER_ID, -1);
        if (reminderId == -1) {
            Log.w(TAG, "Nhận Intent nhắc nhở không có EXTRA_REMINDER_ID hợp lệ.");
            return;
        }

        Log.i(TAG, "Đã nhận alarm kích hoạt cho reminder ID=" + reminderId);

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        NotificationScheduler scheduler = new NotificationScheduler(context, dbHelper, new ReminderTimeCalculator());

        try {
            // Đọc lại trạng thái mới nhất từ Database (nguồn chân lý duy nhất)
            Reminder reminder = dbHelper.getReminderById(reminderId);

            if (reminder == null) {
                Log.w(TAG, "Reminder ID=" + reminderId + " không còn tồn tại trong Database. Hủy alarm.");
                scheduler.cancelReminder(reminderId);
                return;
            }

            if (!reminder.isEnabled()) {
                Log.i(TAG, "Reminder ID=" + reminderId + " đã bị tắt bởi người dùng. Không gửi thông báo và hủy alarm.");
                scheduler.cancelReminder(reminderId);
                return;
            }

            // Gửi thông báo an toàn nếu được phép
            ReminderNotificationHelper.sendNotification(context, reminder.getId(), reminder.getType());

            // Tự động lập lịch cho ngày kế tiếp nếu reminder vẫn đang bật
            scheduler.scheduleReminder(reminder.getId());

        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình xử lý ReminderReceiver: " + e.getMessage(), e);
        } finally {
            dbHelper.close();
        }
    }
}
