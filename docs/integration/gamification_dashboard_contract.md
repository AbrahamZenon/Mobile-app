# HỢP ĐỒNG TÍCH HỢP GAMIFICATION VÀO DASHBOARD (CHO THÀNH VIÊN 1)

> **Tài liệu bàn giao kiến trúc và API**  
> **Người thực hiện:** Thành viên 2 — Nguyễn Hoàng Anh Đức (MSV: 22A1001D0089)  
> **Dự án:** GoalTracker (Android Native Java, minSdk 24, compileSdk 34, AndroidX, SQLite DB v3)  
> **Phiên bản tài liệu:** 1.0 (Hoàn thành theo Prompt 6)

---

## 1. TỔNG QUAN

Tài liệu này cung cấp hướng dẫn chi tiết và hợp đồng giao tiếp (Integration Contract) để Thành viên 1 có thể tích hợp dữ liệu **Chuỗi hoạt động (Streak)**, **Câu danh ngôn động lực (Daily Quote)** và **Huy hiệu thành tích (Badges)** vào màn hình chính `MainActivity` (Dashboard) mà **không làm xáo trộn kiến trúc hay phát sinh xung đột Git**.

Toàn bộ logic tính toán phức tạp đã được Thành viên 2 đóng gói hoàn chỉnh trong các module độc lập. Thành viên 1 chỉ cần gọi các phương thức Facade được cung cấp bên dưới.

---

## 2. CÁC CLASS VÀ DATA MODEL ĐƯỢC CUNG CẤP

| Class / Interface | Package | Vai trò & Mục đích |
| :--- | :--- | :--- |
| `GamificationIntegration` | `com.mad.goaltracker.gamification` | **Facade chính** để Dashboard gọi bất đồng bộ lấy dữ liệu Streak, Quote và Badge count. |
| `GamificationCallback` | `com.mad.goaltracker.gamification.GamificationIntegration` | Interface callback trả kết quả về **Main UI Thread**. |
| `GamificationDashboardData` | `com.mad.goaltracker.models` | Data Model bất biến (`immutable`) chứa 5 thông số tổng hợp cho Dashboard. |
| `GamificationActivity` | `com.mad.goaltracker.activities` | Màn hình chi tiết Thành Tích (Streak, Quote, Lưới 8 Huy hiệu). |
| `AchievementDialogFragment` | `com.mad.goaltracker.activities` | Dialog tự động hiển thị chúc mừng khi có huy hiệu mới được mở khóa. |
| `MotivationalQuoteProvider` | `com.mad.goaltracker.gamification` | Cung cấp 20 câu danh ngôn tiếng Việt offline, tất định theo ngày. |
| `StreakCalculator` | `com.mad.goaltracker.gamification` | Thuật toán tính current streak và longest streak chuẩn quốc tế. |

---

## 3. DATA MODEL: `GamificationDashboardData`

Lớp đóng gói dữ liệu bất biến, bảo vệ an toàn trước các giá trị âm hoặc null:

```java
public class GamificationDashboardData implements Serializable {
    public int getCurrentStreak();           // Chuỗi ngày hoạt động liên tiếp hiện tại (ví dụ: 7)
    public int getLongestStreak();           // Kỷ lục chuỗi ngày dài nhất từng đạt được (ví dụ: 12)
    public Quote getDailyQuote();            // Câu danh ngôn của ngày hôm nay (tất định)
    public int getUnlockedBadgesCount();     // Số lượng huy hiệu đã mở khóa (ví dụ: 3)
    public int getTotalBadgesCount();        // Tổng số huy hiệu chuẩn trong hệ thống (cố định là 8)
    public boolean hasActiveStreak();        // Trả về true nếu currentStreak > 0
    public float getBadgeProgressPercentage(); // Tỷ lệ hoàn thành huy hiệu (0.0% -> 100.0%)
}
```

---

## 4. CÁCH SỬ DỤNG TRÊN DASHBOARD (`MainActivity`)

### 4.1. Lấy dữ liệu Streak, Quote và Huy hiệu bất đồng bộ
Thành viên 1 gọi `GamificationIntegration.loadDashboardData()` trong `onResume()` hoặc `setupViews()` của `MainActivity`:

```java
import com.mad.goaltracker.gamification.GamificationIntegration;
import com.mad.goaltracker.models.GamificationDashboardData;

// Trong MainActivity.java:
@Override
protected void onResume() {
    super.onResume();
    loadGamificationOverview();
}

private void loadGamificationOverview() {
    GamificationIntegration.loadDashboardData(this, new GamificationIntegration.GamificationCallback() {
        @Override
        public void onSuccess(GamificationDashboardData data) {
            // Callback luôn được trả về trên Main UI Thread!
            // 1. Cập nhật Chuỗi hoạt động
            tvStreakCount.setText(data.getCurrentStreak() + " NGÀY");
            tvLongestStreak.setText("Kỷ lục: " + data.getLongestStreak() + " ngày");

            // 2. Cập nhật Câu danh ngôn trong ngày
            tvDailyQuoteContent.setText("“" + data.getDailyQuote().getContent() + "”");
            tvDailyQuoteAuthor.setText("— " + data.getDailyQuote().getAuthor());

            // 3. Cập nhật tiến độ huy hiệu
            tvBadgeSummary.setText(data.getUnlockedBadgesCount() + "/" + data.getTotalBadgesCount() + " huy hiệu");
        }

        @Override
        public void onError(Throwable error) {
            // Xử lý lỗi an toàn (ví dụ: log lỗi hoặc hiển thị fallback)
            Log.e("MainActivity", "Lỗi tải Gamification: " + error.getMessage());
        }
    });
}
```

### 4.2. Mở màn hình Thành Tích (`GamificationActivity`)
Khi người dùng click vào Card Streak, Card Quote hoặc nút "Xem Thành Tích" trên Dashboard:

```java
import android.content.Intent;
import com.mad.goaltracker.activities.GamificationActivity;

btnViewAchievements.setOnClickListener(v -> {
    Intent intent = new Intent(MainActivity.this, GamificationActivity.class);
    startActivity(intent);
});
```

### 4.3. Cập nhật sau khi người dùng Thêm/Sửa/Xóa Goal, Task, hoặc Viết Journal
Khi người dùng hoàn thành một hành động nghiệp vụ (tạo goal, check hoàn thành task, lưu bài nhật ký):
- Hãy gọi lại hàm `loadGamificationOverview()` trong `onResume()` của `MainActivity`.
- `GamificationActivity` khi được người dùng mở ra sẽ tự động kích hoạt `evaluateAndUnlockBadges()` và bật `AchievementDialogFragment` chúc mừng nếu có huy hiệu mới đủ điều kiện.

---

## 5. THIẾT KẾ GIAO DIỆN GỢI Ý CHO DASHBOARD

Thành viên 1 có thể bổ sung một Card nhỏ gọn trên đầu `activity_main.xml` (hoặc vị trí phù hợp):

```xml
<!-- Gợi ý widget Streak & Quote thu nhỏ trên Dashboard -->
<androidx.cardview.widget.CardView
    android:id="@+id/cardDashboardGamification"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="14dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <ImageView
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:src="@drawable/ic_streak_fire" />

            <TextView
                android:id="@+id/tvStreakCount"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="6dp"
                android:text="0 NGÀY"
                android:textColor="@color/streak_fire_color"
                android:textStyle="bold" />

            <View
                android:layout_width="0dp"
                android:layout_height="1dp"
                android:layout_weight="1" />

            <TextView
                android:id="@+id/tvBadgeSummary"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="0/8 huy hiệu"
                android:textColor="@color/primary"
                android:textSize="12sp"
                android:textStyle="bold" />
        </LinearLayout>

        <TextView
            android:id="@+id/tvDailyQuoteContent"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:ellipsize="end"
            android:maxLines="2"
            android:textColor="@color/text_primary"
            android:textSize="13sp"
            android:textStyle="italic" />

    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

## 6. DANH SÁCH 8 HUY HIỆU CHUẨN ĐÃ ĐƯỢC HỆ THỐNG QUẢN LÝ

| Mã Huy Hiệu (Code) | Tên Tiếng Việt | Điều Kiện Mở Khóa Tự Động |
| :--- | :--- | :--- |
| `FIRST_GOAL` | Người Khởi Đầu | Tạo ít nhất 1 mục tiêu (`totalGoals >= 1`) |
| `FIRST_TASK` | Hành Động Nhanh | Hoàn thành ít nhất 1 nhiệm vụ (`completedTasks >= 1`) |
| `FIRST_JOURNAL` | Nhà Văn Kiên Định | Viết ít nhất 1 bài nhật ký (`totalJournals >= 1`) |
| `STREAK_3` | Kỷ Luật 3 Ngày | Chuỗi hoạt động liên tiếp đạt 3 ngày (`longestStreak >= 3`) |
| `STREAK_7` | Kỷ Luật Thép | Chuỗi hoạt động liên tiếp đạt 7 ngày (`longestStreak >= 7`) |
| `STREAK_30` | Chiến Binh 30 Ngày | Chuỗi hoạt động liên tiếp đạt 30 ngày (`longestStreak >= 30`) |
| `GOAL_COMPLETED` | Người Chinh Phục | Hoàn thành trọn vẹn 1 mục tiêu (`completedGoals >= 1`) |
| `PRODUCTIVITY_10` | Bậc Thầy Năng Suất | Hoàn thành tích lũy 10 nhiệm vụ (`completedTasks >= 10`) |

---

## 7. CAM KẾT AN TOÀN HỆ THỐNG
1. **Không chặn Main Thread:** Mọi truy vấn SQLite đều chạy trong Thread Pool riêng biệt.
2. **Không rò rỉ bộ nhớ:** Callback sử dụng `context.getApplicationContext()`.
3. **Chống trùng lặp:** Huy hiệu đã mở khóa sẽ không bị mở khóa lại hay hiển thị dialog trùng.
4. **Không cần Internet:** Hoạt động 100% offline, lưu trữ bền vững trong SQLite Database v3.
