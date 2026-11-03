package com.mad.goaltracker.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Trình điều phối và lập lịch nhắc nhở sử dụng Android AlarmManager.
 * Quản lý vòng đời alarm cho MORNING và EVENING reminders.
 */
public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static final String ACTION_REMINDER_TRIGGER = "com.mad.goaltracker.ACTION_REMINDER_TRIGGER";
    public static final String EXTRA_REMINDER_ID = "EXTRA_REMINDER_ID";
    public static final String EXTRA_REMINDER_TYPE = "EXTRA_REMINDER_TYPE";

    private final Context context;
    private final AlarmManager alarmManager;
    private final DatabaseHelper dbHelper;
    private final ReminderTimeCalculator timeCalculator;

    public NotificationScheduler(Context context) {
        this(context, new DatabaseHelper(context), new ReminderTimeCalculator());
    }

    public NotificationScheduler(Context context, DatabaseHelper dbHelper, ReminderTimeCalculator timeCalculator) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        this.dbHelper = dbHelper;
        this.timeCalculator = timeCalculator != null ? timeCalculator : new ReminderTimeCalculator();
    }

    /**
     * Lập lịch cho một reminder cụ thể theo ID.
     * Tự động đọc dữ liệu mới nhất từ SQLite Database.
     */
    public void scheduleReminder(int reminderId) {
        Reminder reminder = dbHelper.getReminderById(reminderId);

        if (reminder == null) {
            Log.w(TAG, "Không tìm thấy reminder ID=" + reminderId + " trong cơ sở dữ liệu. Hủy alarm.");
            cancelReminder(reminderId);
            return;
        }

        if (!reminder.isEnabled()) {
            Log.i(TAG, "Reminder ID=" + reminderId + " (" + reminder.getType() + ") đang tắt. Hủy alarm.");
            cancelReminder(reminderId);
            return;
        }

        String timeString = reminder.getTimeString();
        if (!ReminderTimeCalculator.isValidTime(timeString)) {
            Log.e(TAG, "Thời gian nhắc nhở không hợp lệ ('" + timeString + "') cho reminder ID=" + reminderId);
            cancelReminder(reminderId);
            return;
        }

        long nextTriggerMillis;
        try {
            nextTriggerMillis = timeCalculator.calculateNextTriggerMillis(timeString);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tính toán thời điểm kích hoạt cho reminder ID=" + reminderId + ": " + e.getMessage());
            cancelReminder(reminderId);
            return;
        }

        // Hủy alarm cũ nếu có trước khi đặt alarm mới để tránh trùng lặp
        PendingIntent pendingIntent = createPendingIntent(reminderId, reminder.getType());
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(nextTriggerMillis));
        Log.i(TAG, "Đang lập lịch reminder ID=" + reminderId + " (" + reminder.getType() + ") vào: " + formattedDate);

        scheduleAlarm(nextTriggerMillis, pendingIntent);
    }

    /**
     * Hủy alarm của một reminder theo ID.
     */
    public void cancelReminder(int reminderId) {
        PendingIntent pendingIntent = createPendingIntent(reminderId, null);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pendingIntent.cancel();
        Log.i(TAG, "Đã hủy alarm cho reminder ID=" + reminderId);
    }

    /**
     * Đồng bộ và lập lịch cho toàn bộ reminders đang bật trong Database.
     * Hàm có tính idempotent, an toàn khi gọi nhiều lần.
     */
    public void scheduleAllEnabledReminders() {
        List<Reminder> reminders = dbHelper.getAllReminders();
        if (reminders == null || reminders.isEmpty()) {
            Log.i(TAG, "Không có reminder nào trong Database.");
            return;
        }

        for (Reminder reminder : reminders) {
            if (reminder.isEnabled()) {
                scheduleReminder(reminder.getId());
            } else {
                cancelReminder(reminder.getId());
            }
        }
    }

    /**
     * Làm mới lịch reminder sau khi người dùng thay đổi thời gian hoặc trạng thái bật/tắt trong UI/Settings.
     */
    public void refreshReminder(int reminderId) {
        scheduleReminder(reminderId);
    }

    /**
     * Hủy toàn bộ alarm của tất cả reminders.
     */
    public void cancelAllReminders() {
        List<Reminder> reminders = dbHelper.getAllReminders();
        if (reminders != null) {
            for (Reminder reminder : reminders) {
                cancelReminder(reminder.getId());
            }
        }
    }

    /**
     * Kiểm tra xem ứng dụng có quyền đặt exact alarm hay không.
     * Android 12 (API 31)+ yêu cầu canScheduleExactAlarms(). Các API thấp hơn luôn có quyền.
     */
    public boolean hasExactAlarmPermission() {
        if (alarmManager == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * Kiểm tra xem quyền thông báo đã được cấp hay chưa.
     */
    public boolean isNotificationPermissionGranted() {
        return ReminderNotificationHelper.canPostNotifications(context);
    }

    /**
     * Thực thi đặt alarm với phương án Exact (nếu có quyền) hoặc Fallback Inexact (nếu không có quyền).
     */
    private void scheduleAlarm(long triggerAtMillis, PendingIntent pendingIntent) {
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager là null, không thể lập lịch.");
            return;
        }

        boolean canExact = hasExactAlarmPermission();

        if (canExact) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
                Log.i(TAG, "Đã đặt lịch EXACT alarm thành công.");
                return;
            } catch (SecurityException e) {
                Log.w(TAG, "SecurityException khi đặt exact alarm. Chuyển sang phương án Fallback Inexact: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "Ứng dụng không có quyền đặt exact alarm. Sử dụng phương án Fallback Inexact.");
        }

        // Fallback an toàn (Inexact alarm)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
        Log.i(TAG, "Đã đặt lịch FALLBACK INEXACT alarm thành công.");
    }

    /**
     * Tạo PendingIntent duy nhất dựa trên ID và Data URI để ngăn chặn việc ghi đè PendingIntent giữa các reminders.
     */
    public PendingIntent createPendingIntent(int reminderId, String reminderType) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ACTION_REMINDER_TRIGGER);
        intent.setData(Uri.parse("goaltracker://reminder/" + reminderId)); // Định danh duy nhất
        intent.putExtra(EXTRA_REMINDER_ID, reminderId);
        if (reminderType != null) {
            intent.putExtra(EXTRA_REMINDER_TYPE, reminderType);
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getBroadcast(context, reminderId, intent, flags);
    }
}
