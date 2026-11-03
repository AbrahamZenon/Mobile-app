package com.mad.goaltracker.gamification;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.mad.goaltracker.activities.AchievementDialogFragment;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Badge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Trình điều phối sự kiện thành tích tập trung (Centralized Achievement Coordinator).
 * Cầu nối giữa các thao tác nghiệp vụ (Tạo Goal, Hoàn thành Task, Viết Journal)
 * với GamificationManager và giao diện hiển thị Achievement Dialog.
 *
 * Nguyên tắc:
 * 1. Chỉ đánh giá mở khóa SAU KHI SQLite commit thành công.
 * 2. Lưu các huy hiệu mới mở khóa vào PendingAchievementStore.
 * 3. Cho phép GamificationActivity (hoặc MainActivity) lấy danh sách pending và hiển thị dialog.
 * 4. Gom nhiều huy hiệu mở khóa đồng thời vào 1 dialog duy nhất.
 * 5. Chống hiển thị trùng lặp (Idempotent).
 */
public class AchievementEventCoordinator {

    private static final String TAG = "AchievementCoordinator";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface EvaluationCallback {
        void onCompleted(List<Badge> newlyUnlockedBadges);
    }

    /**
     * Kích hoạt đánh giá và mở khóa huy hiệu bất đồng bộ trong luồng nền.
     * Thường được gọi sau khi thêm Goal, hoàn thành Task hoặc viết Journal.
     */
    public static void triggerEvaluation(Context context) {
        triggerEvaluation(context, null);
    }

    /**
     * Kích hoạt đánh giá bất đồng bộ kèm callback nhận kết quả trên Main UI Thread.
     */
    public static void triggerEvaluation(Context context, final EvaluationCallback callback) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();

        EXECUTOR.execute(() -> {
            try {
                final List<Badge> newlyUnlocked = triggerEvaluationSync(appContext);
                if (callback != null) {
                    MAIN_HANDLER.post(() -> callback.onCompleted(newlyUnlocked));
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi thực thi đánh giá huy hiệu: " + e.getMessage(), e);
                if (callback != null) {
                    MAIN_HANDLER.post(() -> callback.onCompleted(new ArrayList<>()));
                }
            }
        });
    }

    /**
     * Thực thi đánh giá đồng bộ (dùng trong luồng nền hoặc unit test).
     */
    public static List<Badge> triggerEvaluationSync(Context context) {
        if (context == null) return new ArrayList<>();

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        DatabaseGamificationDataSource dataSource = new DatabaseGamificationDataSource(dbHelper);
        GamificationManager manager = new GamificationManager(dataSource);

        List<Badge> newlyUnlocked = manager.evaluateAndUnlockBadges();

        if (newlyUnlocked != null && !newlyUnlocked.isEmpty()) {
            PendingAchievementStore store = new PendingAchievementStore(context);
            List<String> codes = new ArrayList<>();
            for (Badge b : newlyUnlocked) {
                codes.add(b.getCode());
            }
            store.addPendingBadges(codes);
            Log.i(TAG, "Đã ghi nhận " + newlyUnlocked.size() + " huy hiệu mới mở khóa vào PendingAchievementStore: " + codes);
        }

        return newlyUnlocked != null ? newlyUnlocked : new ArrayList<>();
    }

    /**
     * Lấy danh sách đối tượng Badge đầy đủ của các huy hiệu đang chờ chúc mừng.
     */
    public static List<Badge> getPendingBadgesToCelebrate(Context context, DatabaseHelper dbHelper) {
        if (context == null || dbHelper == null) return new ArrayList<>();

        PendingAchievementStore store = new PendingAchievementStore(context);
        List<String> pendingCodes = store.getPendingBadgeCodes();
        List<Badge> badges = new ArrayList<>();

        for (String code : pendingCodes) {
            Badge b = dbHelper.getBadgeByCode(code);
            if (b != null && b.isUnlocked()) {
                badges.add(b);
            }
        }

        return badges;
    }

    /**
     * Đánh dấu toàn bộ các huy hiệu pending đã được hiển thị chúc mừng.
     */
    public static void markAchievementsCelebrated(Context context) {
        if (context == null) return;
        PendingAchievementStore store = new PendingAchievementStore(context);
        store.markAllAsCelebrated();
    }

    /**
     * Kiểm tra và hiển thị Achievement Celebration Dialog nếu có huy hiệu mới đang chờ.
     * Tự động xóa trạng thái pending sau khi hiển thị.
     *
     * @return true nếu có hiển thị dialog, false nếu không có huy hiệu nào pending
     */
    public static boolean checkAndShowCelebrationDialog(FragmentActivity activity, DatabaseHelper dbHelper) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed() || dbHelper == null) {
            return false;
        }

        List<Badge> pendingBadges = getPendingBadgesToCelebrate(activity, dbHelper);
        if (pendingBadges.isEmpty()) {
            return false;
        }

        // Kiểm tra xem dialog đã đang hiển thị hay chưa để tránh mở trùng
        if (activity.getSupportFragmentManager().findFragmentByTag(AchievementDialogFragment.TAG) != null) {
            return false;
        }

        AchievementDialogFragment dialog = AchievementDialogFragment.newCelebrationInstance(new ArrayList<>(pendingBadges));
        dialog.show(activity.getSupportFragmentManager(), AchievementDialogFragment.TAG);

        // Đánh dấu đã chúc mừng để không bao giờ hiện lại
        markAchievementsCelebrated(activity);
        return true;
    }
}
