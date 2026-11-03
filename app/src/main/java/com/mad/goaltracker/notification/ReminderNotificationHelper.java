package com.mad.goaltracker.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

/**
 * Trợ giúp quản lý Notification Channel, kiểm tra quyền thông báo và tạo Notification hiển thị.
 * Sử dụng thuần Android Framework API (minSdk 24, targetSdk 34), độc lập với UI.
 */
public class ReminderNotificationHelper {

    private static final String TAG = "ReminderNotifHelper";

    public static final String CHANNEL_ID = "goal_tracker_reminders";
    public static final String CHANNEL_NAME_DEFAULT = "Nhắc nhở GoalTracker";
    public static final String CHANNEL_DESC_DEFAULT = "Thông báo nhắc nhở mục tiêu và thói quen hằng ngày.";

    public static final int NOTIFICATION_ID_MORNING = 1;
    public static final int NOTIFICATION_ID_EVENING = 2;

    public static final String TITLE_MORNING_DEFAULT = "Chào buổi sáng!";
    public static final String CONTENT_MORNING_DEFAULT = "Hãy bắt đầu ngày mới bằng một bước tiến nhỏ cho mục tiêu của bạn.";

    public static final String TITLE_EVENING_DEFAULT = "Đã đến lúc nhìn lại hôm nay!";
    public static final String CONTENT_EVENING_DEFAULT = "Bạn đã tiến gần hơn đến mục tiêu của mình như thế nào hôm nay?";

    public static final String TITLE_FALLBACK_DEFAULT = "GoalTracker Nhắc Nhở";
    public static final String CONTENT_FALLBACK_DEFAULT = "Đừng quên theo dõi và cập nhật tiến độ mục tiêu của bạn nhé!";

    /**
     * Tạo Notification Channel cho Android 8.0 (API 26) trở lên.
     * Hàm có tính idempotent, an toàn khi gọi nhiều lần mà không ghi đè cấu hình người dùng.
     */
    public static void createNotificationChannel(Context context) {
        if (context == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel existing = nm.getNotificationChannel(CHANNEL_ID);
                if (existing == null) {
                    String name = getStringResource(context, "notification_channel_name", CHANNEL_NAME_DEFAULT);
                    String description = getStringResource(context, "notification_channel_desc", CHANNEL_DESC_DEFAULT);

                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            name,
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    channel.setDescription(description);
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    nm.createNotificationChannel(channel);
                    Log.i(TAG, "Notification Channel '" + CHANNEL_ID + "' đã được khởi tạo thành công.");
                }
            }
        }
    }

    /**
     * Kiểm tra xem ứng dụng có đủ quyền để gửi thông báo hay không:
     * - Android 13+ (API 33): Kiểm tra quyền runtime POST_NOTIFICATIONS
     * - Mọi phiên bản (minSdk 24+): Kiểm tra nm.areNotificationsEnabled()
     * - Android 8.0+ (API 26+): Kiểm tra xem Channel có bị tắt (IMPORTANCE_NONE) hay không
     */
    public static boolean canPostNotifications(Context context) {
        if (context == null) return false;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return false;

        // 1. Kiểm tra trạng thái bật/tắt thông báo tổng thể của ứng dụng (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!nm.areNotificationsEnabled()) {
                Log.w(TAG, "Thông báo của ứng dụng đang bị tắt trong cài đặt hệ thống.");
                return false;
            }
        }

        // 2. Android 13 (API 33)+ kiểm tra quyền runtime POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= 33) {
            int permissionCheck = context.checkSelfPermission("android.permission.POST_NOTIFICATIONS");
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Quyền POST_NOTIFICATIONS chưa được cấp.");
                return false;
            }
        }

        // 3. Android 8.0 (API 26)+ kiểm tra trạng thái channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = nm.getNotificationChannel(CHANNEL_ID);
            if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                Log.w(TAG, "Channel '" + CHANNEL_ID + "' đang bị tắt trong cài đặt.");
                return false;
            }
        }

        return true;
    }

    /**
     * Hiển thị thông báo nhắc nhở tương ứng với reminder.
     *
     * @param context      Context
     * @param reminderId   ID định danh của reminder (đồng thời là Notification ID)
     * @param reminderType Loại nhắc nhở ("MORNING", "EVENING" hoặc khác)
     * @return true nếu gửi thành công, false nếu không đủ quyền hoặc gặp lỗi
     */
    public static boolean sendNotification(Context context, int reminderId, String reminderType) {
        if (context == null) return false;

        if (!canPostNotifications(context)) {
            Log.w(TAG, "Không thể gửi thông báo: Quyền thông báo chưa được cấp hoặc bị chặn.");
            return false;
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            Log.e(TAG, "NotificationManager không khả dụng.");
            return false;
        }

        // Đảm bảo Channel đã tồn tại trước khi bắn thông báo
        createNotificationChannel(context);

        String title;
        String content;

        if ("MORNING".equalsIgnoreCase(reminderType)) {
            title = getStringResource(context, "notification_morning_title", TITLE_MORNING_DEFAULT);
            content = getStringResource(context, "notification_morning_content", CONTENT_MORNING_DEFAULT);
        } else if ("EVENING".equalsIgnoreCase(reminderType)) {
            title = getStringResource(context, "notification_evening_title", TITLE_EVENING_DEFAULT);
            content = getStringResource(context, "notification_evening_content", CONTENT_EVENING_DEFAULT);
        } else {
            title = getStringResource(context, "notification_default_title", TITLE_FALLBACK_DEFAULT);
            content = getStringResource(context, "notification_default_content", CONTENT_FALLBACK_DEFAULT);
        }

        // Intent mở MainActivity khi người dùng chạm vào thông báo
        Intent intent = new Intent();
        intent.setClassName(context.getPackageName(), "com.mad.goaltracker.activities.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context,
                reminderId,
                intent,
                pendingIntentFlags
        );

        int iconResId = getDrawableResource(context, "ic_notification", android.R.drawable.ic_dialog_info);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }

        builder.setSmallIcon(iconResId)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new Notification.BigTextStyle().bigText(content))
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent);

        try {
            nm.notify(reminderId, builder.build());
            Log.i(TAG, "Đã gửi thông báo thành công cho reminder ID=" + reminderId + " (" + reminderType + ")");
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException khi gọi notify(): " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi không xác định khi gửi thông báo: " + e.getMessage());
            return false;
        }
    }

    private static String getStringResource(Context context, String name, String defaultValue) {
        try {
            int id = context.getResources().getIdentifier(name, "string", context.getPackageName());
            if (id != 0) {
                return context.getString(id);
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    private static int getDrawableResource(Context context, String name, int fallbackId) {
        try {
            int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            if (id != 0) {
                return id;
            }
        } catch (Exception ignored) {
        }
        return fallbackId;
    }
}
