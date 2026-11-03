package com.mad.goaltracker.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.mad.goaltracker.R;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Reminder;
import com.mad.goaltracker.notification.NotificationScheduler;
import com.mad.goaltracker.notification.NotificationSettingsModel;
import com.mad.goaltracker.notification.NotificationSettingsModel.ExactAlarmStatus;
import com.mad.goaltracker.notification.NotificationSettingsModel.NotificationPermissionStatus;
import com.mad.goaltracker.notification.ReminderNotificationHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình Cài Đặt Nhắc Nhở & Quản Lý Quyền Thông Báo (Prompt 7).
 * Cho phép người dùng cấu hình độc lập hai nhắc nhở hằng ngày (Sáng và Tối),
 * chọn giờ bằng TimePickerDialog (định dạng HH:mm) và kiểm soát các quyền hệ thống:
 * - Runtime permission POST_NOTIFICATIONS (Android 13+ / API 33+)
 * - Special access SCHEDULE_EXACT_ALARM và Inexact Fallback (Android 12+ / API 31+)
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String TAG = "NotifSettingsActivity";

    // Views
    private ImageButton btnBack;
    private ProgressBar progressBarLoading;
    private LinearLayout layoutUnavailableState;
    private LinearLayout layoutMainContent;

    // Morning Card Views
    private TextView tvMorningTime;
    private SwitchCompat switchMorning;
    private MaterialButton btnChangeMorningTime;

    // Evening Card Views
    private TextView tvEveningTime;
    private SwitchCompat switchEvening;
    private MaterialButton btnChangeEveningTime;

    // Notification Permission Views
    private TextView tvNotificationPermissionStatus;
    private TextView tvNotificationPermissionDesc;
    private MaterialButton btnRequestNotificationPermission;
    private MaterialButton btnOpenNotificationSettings;

    // Exact Alarm Views
    private TextView tvExactAlarmStatus;
    private TextView tvExactAlarmDesc;
    private TextView tvExactAlarmWarning;
    private MaterialButton btnRequestExactAlarm;

    // Data & Dependencies
    private DatabaseHelper dbHelper;
    private NotificationScheduler scheduler;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Reminder morningReminder;
    private Reminder eveningReminder;

    // Cờ chống vòng lặp phản hồi sự kiện khi cập nhật trạng thái Switch bằng code
    private boolean isBindingUi = false;
    private boolean lastExactAlarmGranted = false;

    // Activity Result Launcher để xin quyền POST_NOTIFICATIONS (API 33+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                updatePermissionCards();
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền thông báo thành công.", Toast.LENGTH_SHORT).show();
                    // Đồng bộ lại các nhắc nhở đang bật
                    scheduler.scheduleAllEnabledReminders();
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_notification_settings);

        dbHelper = new DatabaseHelper(this);
        scheduler = new NotificationScheduler(this);
        executor = Executors.newSingleThreadExecutor();

        initViews();
        setupListeners();
        loadRemindersData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        layoutUnavailableState = findViewById(R.id.layoutUnavailableState);
        layoutMainContent = findViewById(R.id.layoutMainContent);

        // Morning
        tvMorningTime = findViewById(R.id.tvMorningTime);
        switchMorning = findViewById(R.id.switchMorning);
        btnChangeMorningTime = findViewById(R.id.btnChangeMorningTime);

        // Evening
        tvEveningTime = findViewById(R.id.tvEveningTime);
        switchEvening = findViewById(R.id.switchEvening);
        btnChangeEveningTime = findViewById(R.id.btnChangeEveningTime);

        // Notification Permission
        tvNotificationPermissionStatus = findViewById(R.id.tvNotificationPermissionStatus);
        tvNotificationPermissionDesc = findViewById(R.id.tvNotificationPermissionDesc);
        btnRequestNotificationPermission = findViewById(R.id.btnRequestNotificationPermission);
        btnOpenNotificationSettings = findViewById(R.id.btnOpenNotificationSettings);

        // Exact Alarm
        tvExactAlarmStatus = findViewById(R.id.tvExactAlarmStatus);
        tvExactAlarmDesc = findViewById(R.id.tvExactAlarmDesc);
        tvExactAlarmWarning = findViewById(R.id.tvExactAlarmWarning);
        btnRequestExactAlarm = findViewById(R.id.btnRequestExactAlarm);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Switch Morning
        switchMorning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBindingUi || morningReminder == null) return;
            handleToggleReminder(morningReminder, isChecked, switchMorning);
        });

        // Switch Evening
        switchEvening.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isBindingUi || eveningReminder == null) return;
            handleToggleReminder(eveningReminder, isChecked, switchEvening);
        });

        // Change Morning Time
        btnChangeMorningTime.setOnClickListener(v -> {
            if (morningReminder != null) {
                showTimePicker(morningReminder, tvMorningTime);
            }
        });

        // Change Evening Time
        btnChangeEveningTime.setOnClickListener(v -> {
            if (eveningReminder != null) {
                showTimePicker(eveningReminder, tvEveningTime);
            }
        });

        // Xin quyền thông báo runtime (API 33+)
        btnRequestNotificationPermission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        });

        // Mở màn hình cài đặt thông báo hệ thống
        btnOpenNotificationSettings.setOnClickListener(v -> openSystemNotificationSettings());

        // Mở màn hình xin quyền exact alarm (API 31+)
        btnRequestExactAlarm.setOnClickListener(v -> openExactAlarmSettings());
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean currentExactGranted = scheduler.hasExactAlarmPermission();
        if (!lastExactAlarmGranted && currentExactGranted) {
            Log.i(TAG, "Quyền Exact Alarm vừa được cấp trong Settings. Đồng bộ lại các reminders.");
            scheduler.scheduleAllEnabledReminders();
        }
        lastExactAlarmGranted = currentExactGranted;
        updatePermissionCards();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * Tải dữ liệu reminders từ SQLite trong background thread.
     */
    private void loadRemindersData() {
        progressBarLoading.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            List<Reminder> reminders = dbHelper.getAllReminders();

            Reminder morning = null;
            Reminder evening = null;

            if (reminders != null) {
                for (Reminder r : reminders) {
                    if ("MORNING".equalsIgnoreCase(r.getType()) || r.getId() == 1) {
                        if (morning == null) morning = r;
                    } else if ("EVENING".equalsIgnoreCase(r.getType()) || r.getId() == 2) {
                        if (evening == null) evening = r;
                    }
                }
            }

            final Reminder finalMorning = morning;
            final Reminder finalEvening = evening;

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                progressBarLoading.setVisibility(View.GONE);

                if (finalMorning == null || finalEvening == null) {
                    Log.e(TAG, "Không tìm thấy cấu hình Reminder trong SQLite (Morning=" + finalMorning + ", Evening=" + finalEvening + ")");
                    layoutUnavailableState.setVisibility(View.VISIBLE);
                    layoutMainContent.setVisibility(View.GONE);
                    return;
                }

                layoutUnavailableState.setVisibility(View.GONE);
                layoutMainContent.setVisibility(View.VISIBLE);

                morningReminder = finalMorning;
                eveningReminder = finalEvening;

                isBindingUi = true;
                tvMorningTime.setText(morningReminder.getTimeString());
                switchMorning.setChecked(morningReminder.isEnabled());

                tvEveningTime.setText(eveningReminder.getTimeString());
                switchEvening.setChecked(eveningReminder.isEnabled());
                isBindingUi = false;

                lastExactAlarmGranted = scheduler.hasExactAlarmPermission();
                updatePermissionCards();
            });
        });
    }

    /**
     * Xử lý bật/tắt một reminder độc lập.
     */
    private void handleToggleReminder(Reminder reminder, boolean isEnabled, SwitchCompat switchView) {
        executor.execute(() -> {
            boolean success = dbHelper.setReminderEnabled(reminder.getId(), isEnabled);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (success) {
                    reminder.setEnabled(isEnabled);
                    if (isEnabled) {
                        scheduler.scheduleReminder(reminder.getId());
                        String name = "MORNING".equalsIgnoreCase(reminder.getType())
                                ? getString(R.string.reminder_morning_title)
                                : getString(R.string.reminder_evening_title);
                        Toast.makeText(this, getString(R.string.msg_reminder_enabled, name), Toast.LENGTH_SHORT).show();
                    } else {
                        scheduler.cancelReminder(reminder.getId());
                        String name = "MORNING".equalsIgnoreCase(reminder.getType())
                                ? getString(R.string.reminder_morning_title)
                                : getString(R.string.reminder_evening_title);
                        Toast.makeText(this, getString(R.string.msg_reminder_disabled, name), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Revert switch nếu database lưu thất bại
                    isBindingUi = true;
                    switchView.setChecked(!isEnabled);
                    isBindingUi = false;
                    Toast.makeText(this, R.string.msg_error_db, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Mở TimePickerDialog để người dùng chọn giờ cho reminder.
     */
    private void showTimePicker(Reminder reminder, TextView tvTime) {
        String currentTime = reminder.getTimeString();
        int initialHour = 8;
        int initialMinute = 0;

        if (NotificationSettingsModel.isValidTime(currentTime)) {
            String[] parts = currentTime.split(":");
            try {
                initialHour = Integer.parseInt(parts[0]);
                initialMinute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String formattedTime = NotificationSettingsModel.formatTime(hourOfDay, minute);
                    handleUpdateTime(reminder, formattedTime, tvTime);
                },
                initialHour,
                initialMinute,
                true // 24 giờ
        );
        dialog.setTitle(getString(R.string.time_picker_title));
        dialog.show();
    }

    /**
     * Lưu thời gian mới vào SQLite và cập nhật AlarmManager.
     */
    private void handleUpdateTime(Reminder reminder, String newTime, TextView tvTime) {
        executor.execute(() -> {
            boolean success = dbHelper.updateReminderTime(reminder.getId(), newTime);

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (success) {
                    reminder.setTimeString(newTime);
                    tvTime.setText(newTime);
                    scheduler.refreshReminder(reminder.getId());
                    Toast.makeText(this, getString(R.string.msg_time_updated, newTime), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.msg_error_db, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Cập nhật các card trạng thái quyền thông báo và exact alarm.
     */
    private void updatePermissionCards() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        boolean areNotificationsEnabled = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && nm != null) {
            areNotificationsEnabled = nm.areNotificationsEnabled();
        }

        boolean isRuntimeGranted = true;
        if (Build.VERSION.SDK_INT >= 33) {
            isRuntimeGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }

        boolean isChannelBlocked = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel ch = nm.getNotificationChannel(ReminderNotificationHelper.CHANNEL_ID);
            if (ch != null && ch.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                isChannelBlocked = true;
            }
        }

        NotificationPermissionStatus notifStatus = NotificationSettingsModel.evaluateNotificationPermission(
                Build.VERSION.SDK_INT, areNotificationsEnabled, isRuntimeGranted, isChannelBlocked
        );

        switch (notifStatus) {
            case GRANTED:
                tvNotificationPermissionStatus.setText(R.string.permission_status_granted);
                tvNotificationPermissionStatus.setBackgroundResource(R.drawable.bg_status_pill_unlocked);
                tvNotificationPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_granted_dark));
                tvNotificationPermissionDesc.setText(R.string.permission_desc);
                btnRequestNotificationPermission.setVisibility(View.GONE);
                btnOpenNotificationSettings.setVisibility(View.GONE);
                break;

            case NOT_REQUIRED:
                tvNotificationPermissionStatus.setText(R.string.permission_status_granted);
                tvNotificationPermissionStatus.setBackgroundResource(R.drawable.bg_status_pill_unlocked);
                tvNotificationPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_granted_dark));
                tvNotificationPermissionDesc.setText(R.string.permission_desc);
                btnRequestNotificationPermission.setVisibility(View.GONE);
                btnOpenNotificationSettings.setVisibility(View.GONE);
                break;

            case DENIED:
                tvNotificationPermissionStatus.setText(R.string.permission_status_denied);
                tvNotificationPermissionStatus.setBackgroundResource(R.drawable.bg_status_pill_denied);
                tvNotificationPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_denied_dark));
                tvNotificationPermissionDesc.setText(R.string.permission_denied_message);
                btnRequestNotificationPermission.setVisibility(View.VISIBLE);
                btnOpenNotificationSettings.setVisibility(View.VISIBLE);
                break;

            case BLOCKED_IN_SETTINGS:
                tvNotificationPermissionStatus.setText(R.string.permission_status_blocked);
                tvNotificationPermissionStatus.setBackgroundResource(R.drawable.bg_status_pill_denied);
                tvNotificationPermissionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_denied_dark));
                tvNotificationPermissionDesc.setText(R.string.permission_denied_message);
                btnRequestNotificationPermission.setVisibility(View.GONE);
                btnOpenNotificationSettings.setVisibility(View.VISIBLE);
                break;
        }

        // Exact Alarm Card
        boolean canExact = scheduler.hasExactAlarmPermission();
        ExactAlarmStatus exactStatus = NotificationSettingsModel.evaluateExactAlarmPermission(
                Build.VERSION.SDK_INT, canExact
        );

        switch (exactStatus) {
            case GRANTED:
                tvExactAlarmStatus.setText(R.string.exact_alarm_status_granted);
                tvExactAlarmStatus.setBackgroundResource(R.drawable.bg_status_pill_unlocked);
                tvExactAlarmStatus.setTextColor(ContextCompat.getColor(this, R.color.status_granted_dark));
                tvExactAlarmWarning.setVisibility(View.GONE);
                btnRequestExactAlarm.setVisibility(View.GONE);
                break;

            case NOT_REQUIRED:
                tvExactAlarmStatus.setText(R.string.exact_alarm_status_not_required);
                tvExactAlarmStatus.setBackgroundResource(R.drawable.bg_status_pill_unlocked);
                tvExactAlarmStatus.setTextColor(ContextCompat.getColor(this, R.color.status_granted_dark));
                tvExactAlarmWarning.setVisibility(View.GONE);
                btnRequestExactAlarm.setVisibility(View.GONE);
                break;

            case INEXACT_FALLBACK:
                tvExactAlarmStatus.setText(R.string.exact_alarm_status_inexact);
                tvExactAlarmStatus.setBackgroundResource(R.drawable.bg_status_pill_warning);
                tvExactAlarmStatus.setTextColor(ContextCompat.getColor(this, R.color.status_warning_dark));
                tvExactAlarmWarning.setVisibility(View.VISIBLE);
                btnRequestExactAlarm.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Điều hướng người dùng tới cài đặt thông báo của ứng dụng.
     */
    private void openSystemNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        }

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Không thể mở ACTION_APP_NOTIFICATION_SETTINGS, chuyển sang ACTION_APPLICATION_DETAILS_SETTINGS");
            Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            fallback.setData(Uri.parse("package:" + getPackageName()));
            try {
                startActivity(fallback);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, "Không thể mở cài đặt hệ thống.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Điều hướng người dùng tới cài đặt SCHEDULE_EXACT_ALARM (Android 12+ / API 31+).
     */
    private void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Không thể mở ACTION_REQUEST_SCHEDULE_EXACT_ALARM, chuyển sang ACTION_APPLICATION_DETAILS_SETTINGS");
                Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                fallback.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(fallback);
                } catch (ActivityNotFoundException e2) {
                    Toast.makeText(this, "Không thể mở cài đặt hệ thống.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
