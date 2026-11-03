# HỢP ĐỒNG TÍCH HỢP MÀN HÌNH CÀI ĐẶT THÔNG BÁO (NOTIFICATION SETTINGS CONTRACT)

**Dự án:** GoalTracker (Android Native Java)  
**Phân hệ:** Quản lý Cài đặt Nhắc nhở & Quyền Hệ thống (Prompt 7)  
**Người phụ trách:** Nguyễn Hoàng Anh Đức (MSV: 22A1001D0089 - Thành viên 2)  
**Đối tượng bàn giao:** Thành viên 1 (Tích hợp Navigation từ Dashboard / Menu trong Prompt 8)  
**Trạng thái Database:** SQLite Database Version 3  

---

## 1. MỤC ĐÍCH & PHẠM VI TÀI LIỆU

Tài liệu này xác lập hợp đồng tích hợp chính thức giữa **Thành viên 2** (phát triển phân hệ Notification Settings UI và Runtime Permissions) và **Thành viên 1** (sở hữu `MainActivity`, Dashboard và Menu điều hướng).

Thành viên 2 cam kết:
- **KHÔNG** chỉnh sửa trực tiếp `MainActivity.java` hay `activity_main.xml` trong Prompt 7 để tránh xung đột mã nguồn (Git conflict).
- Cung cấp màn hình `NotificationSettingsActivity`, Intent điều hướng chuẩn, và các quy tắc đồng bộ độc lập cho hai nhắc nhở (Buổi sáng & Buổi tối).

---

## 2. ACTIVITY & ĐĂNG KÝ MANIFEST

- **Tên lớp Activity:** `com.mad.goaltracker.activities.NotificationSettingsActivity`
- **Khai báo trong AndroidManifest.xml:**
```xml
<activity
    android:name=".activities.NotificationSettingsActivity"
    android:exported="false"
    android:label="@string/title_notification_settings"
    android:parentActivityName=".activities.MainActivity" />
```
- **Quyền bảo mật:** `android:exported="false"` (chỉ khởi chạy nội bộ trong ứng dụng, bảo vệ dữ liệu người dùng).

---

## 3. CÁCH THÀNH VIÊN 1 KHỞI CHẠY NOTIFICATION SETTINGS

Thành viên 1 có thể mở màn hình Cài đặt nhắc nhở từ `MainActivity` (Menu Toolbar, nút Cài đặt, hoặc Card Dashboard) thông qua Intent tiêu chuẩn:

```java
// Ví dụ điều hướng từ MainActivity hoặc bất kỳ Activity nào
Intent intent = new Intent(this, NotificationSettingsActivity.class);
startActivity(intent);
```

Nếu muốn nhận phản hồi khi người dùng quay lại Dashboard (để refresh UI trạng thái nhắc nhở nếu cần):
```java
ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        // Cập nhật lại UI Dashboard nếu Thành viên 1 muốn hiển thị giờ nhắc tiếp theo
        refreshReminderDashboardStatus();
    }
);

// Khởi chạy
settingsLauncher.launch(new Intent(this, NotificationSettingsActivity.class));
```

---

## 4. QUẢN LÝ QUYỀN HỆ THỐNG (RUNTIME PERMISSIONS & SPECIAL ACCESS)

Ứng dụng tuân thủ nghiêm ngặt các nguyên tắc của Google Android:

### 4.1. Quyền `POST_NOTIFICATIONS` (Android 13+ / API 33+)
- **Đặc điểm:** Là Runtime Permission thông thường.
- **Quy tắc kích hoạt:** Ứng dụng **không bao giờ** xin quyền tự động khi vừa mở Settings. Chỉ hiển thị dialog cấp quyền khi người dùng chủ động nhấn nút *"Cho phép thông báo"*.
- **Xử lý từ chối:** Nếu người dùng từ chối cấp quyền, UI hiển thị thông báo hướng dẫn và nút *"Mở cài đặt thông báo"* dẫn tới `Settings.ACTION_APP_NOTIFICATION_SETTINGS`.
- **Hệ thống API < 33:** Không đòi hỏi quyền runtime, ứng dụng tự động kiểm tra `NotificationManager.areNotificationsEnabled()` và trạng thái Notification Channel (`goal_tracker_reminders`).

### 4.2. Quyền `SCHEDULE_EXACT_ALARM` (Android 12+ / API 31+)
- **Đặc điểm:** Là **Special App Access**, KHÔNG PHẢI runtime permission. Người dùng phải cấp quyền trong phần Cài đặt hệ thống của Android.
- **Tuyệt đối không dùng `USE_EXACT_ALARM`:** Dự án tuân thủ chính sách Google Play, không khai báo quyền đặc quyền này cho ứng dụng quản lý mục tiêu cá nhân.
- **Inexact Alarm Fallback:** Khi người dùng chưa cấp quyền exact alarm, `NotificationScheduler` tự động chuyển sang cơ chế Inexact Alarm (`setAndAllowWhileIdle`), đảm bảo hệ thống vẫn gửi thông báo nhắc nhở mà không gây crash hay vô hiệu hóa tính năng của người dùng.
- **Đồng bộ tự động khi được cấp:** Khi người dùng mở `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` và quay trở lại (`onResume()`), ứng dụng tự động phát hiện quyền mới và đồng bộ lại toàn bộ alarms sang chế độ chính xác.

---

## 5. CƠ CHẾ ĐỘC LẬP GIỮA HAI REMINDERS

Dữ liệu lưu trữ trong SQLite Database v3 tại bảng `reminders`:
- **Reminder 1 (MORNING):** Type `"MORNING"`, Giờ mặc định `"08:00"`, Bật mặc định `1`.
- **Reminder 2 (EVENING):** Type `"EVENING"`, Giờ mặc định `"21:00"`, Bật mặc định `1`.

### 5.1. Quy tắc độc lập tuyệt đối (Isolation Invariant):
1. **Bật/Tắt Morning Switch:** Chỉ cập nhật bản ghi ID 1, chỉ gọi `scheduler.scheduleReminder(1)` hoặc `scheduler.cancelReminder(1)`. Reminder Evening (ID 2) được giữ nguyên 100%.
2. **Bật/Tắt Evening Switch:** Chỉ cập nhật bản ghi ID 2, chỉ gọi `scheduler.scheduleReminder(2)` hoặc `scheduler.cancelReminder(2)`. Reminder Morning (ID 1) được giữ nguyên 100%.
3. **Đổi giờ:** Người dùng chọn giờ qua `TimePickerDialog` (24h, format chuẩn `HH:mm`). Khi lưu thành công vào SQLite, gọi `scheduler.refreshReminder(id)`. Nếu reminder đang ở trạng thái TẮT (`is_enabled == 0`), việc đổi giờ **vẫn giữ nguyên trạng thái TẮT**, không tự ý bật alarm.

---

## 6. PUBLIC FAÇADE / TIỆN ÍCH DÀNH CHO THÀNH VIÊN 1

Thành viên 1 có thể sử dụng các lớp sau đã được Thành viên 2 đóng gói hoàn chỉnh:

1. **`NotificationScheduler` (`com.mad.goaltracker.notification.NotificationScheduler`):**
   - `scheduleAllEnabledReminders()`: Khởi tạo/khôi phục lại tất cả reminders đang bật (đã được tích hợp trong `GoalTrackerApp.onCreate()`).
   - `hasExactAlarmPermission()`: Kiểm tra máy có quyền exact alarm hay không.
   - `isNotificationPermissionGranted()`: Kiểm tra ứng dụng có quyền gửi thông báo hay không.

2. **`NotificationSettingsModel` (`com.mad.goaltracker.notification.NotificationSettingsModel`):**
   - `isValidTime(String timeString)`: Kiểm tra chuỗi thời gian hợp lệ (`HH:mm`).
   - `formatTime(int hourOfDay, int minute)`: Định dạng giờ và phút chuẩn US.
   - `evaluateNotificationPermission(...)`: Đánh giá trạng thái quyền theo từng API level.
   - `evaluateExactAlarmPermission(...)`: Đánh giá trạng thái báo giờ chính xác.

---

## 7. CHECKLIST NGHIỆM THU TÍCH HỢP

| STT | Tiêu chí kiểm tra | Kết quả cam kết |
| :---: | :--- | :--- |
| 1 | Khởi chạy `NotificationSettingsActivity` từ Dashboard | Hoạt động trơn tru, không crash, không xung đột layout. |
| 2 | Giờ nhắc đọc từ SQLite v3 | Hiển thị chính xác giờ trong database (mặc định 08:00 & 21:00). |
| 3 | Chọn giờ mới qua TimePicker | Lưu đúng định dạng `HH:mm`, đồng bộ `AlarmManager`. |
| 4 | Bật/tắt switch | Độc lập hoàn toàn giữa Sáng và Tối. |
| 5 | Không có quyền POST_NOTIFICATIONS | Giao diện hiển thị nút cấp quyền và nút mở Settings. |
| 6 | Không có quyền EXACT_ALARM | Tự động dùng Inexact fallback, hiển thị cảnh báo nhẹ. |
| 7 | Xoay màn hình | Không mất dữ liệu, không tạo alarms trùng lặp. |
