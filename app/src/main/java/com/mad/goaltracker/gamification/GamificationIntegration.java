package com.mad.goaltracker.gamification;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.GamificationDashboardData;
import com.mad.goaltracker.models.Quote;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller/Facade độc lập cung cấp dữ liệu Gamification cho Dashboard của Thành viên 1.
 * Tuân thủ nguyên tắc:
 * - Không sửa code của Thành viên 1.
 * - Truy vấn dữ liệu hoàn toàn bất đồng bộ (Background Thread).
 * - Trả kết quả về Main Thread an toàn qua Handler.
 * - Tránh rò rỉ bộ nhớ (sử dụng applicationContext).
 */
public class GamificationIntegration {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface GamificationCallback {
        void onSuccess(GamificationDashboardData data);
        void onError(Throwable error);
    }

    /**
     * Tải dữ liệu tổng hợp Gamification bất đồng bộ cho Dashboard.
     *
     * @param context Context của ứng dụng hoặc Activity
     * @param callback Nhận kết quả trên Main UI Thread
     */
    public static void loadDashboardData(Context context, final GamificationCallback callback) {
        if (context == null) {
            if (callback != null) {
                callback.onError(new IllegalArgumentException("Context cannot be null"));
            }
            return;
        }

        final Context appContext = context.getApplicationContext();

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseHelper dbHelper = new DatabaseHelper(appContext);
                    final GamificationDashboardData data = getDashboardDataSync(dbHelper);

                    MAIN_HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onSuccess(data);
                            }
                        }
                    });
                } catch (final Throwable t) {
                    MAIN_HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onError(t);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Lấy dữ liệu Gamification đồng bộ (dùng trong background thread hoặc kiểm thử).
     */
    public static GamificationDashboardData getDashboardDataSync(DatabaseHelper dbHelper) {
        if (dbHelper == null) {
            throw new IllegalArgumentException("DatabaseHelper cannot be null");
        }

        // 1. Tính toán Streak
        StreakCalculator streakCalculator = new StreakCalculator();
        List<String> activityDates = dbHelper.getAllActivityDates();
        StreakSummary streakSummary = streakCalculator.calculate(activityDates, streakCalculator.getTodayString());

        int currentStreak = (streakSummary != null) ? streakSummary.getCurrentStreak() : 0;
        int longestStreak = (streakSummary != null) ? streakSummary.getLongestStreak() : 0;

        // 2. Lấy Daily Motivational Quote (tất định theo ngày thiết bị)
        MotivationalQuoteProvider quoteProvider = new MotivationalQuoteProvider();
        Quote dailyQuote = quoteProvider.getDailyQuote();

        // 3. Lấy số lượng huy hiệu từ Database
        int unlockedCount = dbHelper.getUnlockedBadgesCount();
        int totalCount = dbHelper.getAllBadges().size();
        if (totalCount == 0) {
            totalCount = 8; // Mặc định 8 huy hiệu chuẩn
        }

        return new GamificationDashboardData(currentStreak, longestStreak, dailyQuote, unlockedCount, totalCount);
    }
}
