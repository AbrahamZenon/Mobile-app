package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Badge;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Quản lý Gamification Engine:
 * - Đánh giá điều kiện mở khóa cho 8 huy hiệu thành tích chuẩn
 * - Điều phối mở khóa an toàn (idempotent), không ghi đè thời điểm mở khóa cũ
 * - Cung cấp StreakSummary cho Dashboard và các màn hình khác
 */
public class GamificationManager {

    // 8 mã huy hiệu chuẩn
    public static final String BADGE_FIRST_GOAL = "FIRST_GOAL";
    public static final String BADGE_FIRST_TASK = "FIRST_TASK";
    public static final String BADGE_FIRST_JOURNAL = "FIRST_JOURNAL";
    public static final String BADGE_STREAK_3 = "STREAK_3";
    public static final String BADGE_STREAK_7 = "STREAK_7";
    public static final String BADGE_STREAK_30 = "STREAK_30";
    public static final String BADGE_GOAL_COMPLETED = "GOAL_COMPLETED";
    public static final String BADGE_PRODUCTIVITY_10 = "PRODUCTIVITY_10";

    private final GamificationDataSource dataSource;
    private final StreakCalculator streakCalculator;

    public GamificationManager(GamificationDataSource dataSource) {
        this(dataSource, new StreakCalculator());
    }

    public GamificationManager(GamificationDataSource dataSource, StreakCalculator streakCalculator) {
        if (dataSource == null) {
            throw new IllegalArgumentException("GamificationDataSource cannot be null");
        }
        if (streakCalculator == null) {
            throw new IllegalArgumentException("StreakCalculator cannot be null");
        }
        this.dataSource = dataSource;
        this.streakCalculator = streakCalculator;
    }

    /**
     * Lấy tổng hợp Streak hiện tại và dài nhất theo ngày hiện tại của thiết bị.
     */
    public StreakSummary getStreakSummary() {
        return getStreakSummary(streakCalculator.getTodayString());
    }

    /**
     * Lấy tổng hợp Streak dựa trên ngày neo chỉ định (dùng trong test hoặc khi timezone thay đổi).
     */
    public StreakSummary getStreakSummary(String currentDate) {
        List<String> activityDates = dataSource.getAllActivityDates();
        return streakCalculator.calculate(activityDates, currentDate);
    }

    /**
     * Đánh giá toàn bộ điều kiện huy hiệu và mở khóa các huy hiệu đủ điều kiện.
     * Sử dụng ngày giờ hiện tại của thiết bị.
     *
     * @return Danh sách các huy hiệu VỪA MỚI được mở khóa trong lần gọi này. Nếu không có trả về rỗng.
     */
    public List<Badge> evaluateAndUnlockBadges() {
        return evaluateAndUnlockBadges(streakCalculator.getTodayString(), getCurrentTimestamp());
    }

    /**
     * Đánh giá và mở khóa với ngày neo và thời điểm chỉ định (hỗ trợ kiểm thử độc lập).
     */
    public List<Badge> evaluateAndUnlockBadges(String anchorDate, String timestamp) {
        List<Badge> allBadges = dataSource.getAllBadges();
        if (allBadges == null || allBadges.isEmpty()) {
            return Collections.emptyList();
        }

        // Lấy số liệu thống kê từ Data Layer
        int totalGoals = dataSource.getTotalGoalsCount();
        int completedGoals = dataSource.getCompletedGoalsCount();
        int completedTasks = dataSource.getCompletedTasksCount();
        int totalJournals = dataSource.getTotalJournalsCount();

        // Tính chuỗi hoạt động; huy hiệu Streak bắt buộc dựa trên longestStreak
        StreakSummary streakSummary = getStreakSummary(anchorDate);
        int longestStreak = streakSummary != null ? streakSummary.getLongestStreak() : 0;

        String unlockTime = (timestamp != null && !timestamp.trim().isEmpty())
                ? timestamp
                : getCurrentTimestamp();

        List<Badge> newlyUnlockedBadges = new ArrayList<>();

        for (Badge badge : allBadges) {
            // Idempotent: Nếu đã mở khóa từ trước thì bỏ qua, không tính lại
            if (badge.isUnlocked()) {
                continue;
            }

            boolean isEligible = checkEligibility(badge.getCode(), totalGoals, completedGoals,
                    completedTasks, totalJournals, longestStreak);

            if (isEligible) {
                // Gọi API unlockBadge của DataSource; API này chỉ cập nhật khi is_unlocked == 0
                boolean success = dataSource.unlockBadge(badge.getCode(), unlockTime);
                if (success) {
                    badge.setUnlocked(true);
                    badge.setUnlockedAt(unlockTime);
                    newlyUnlockedBadges.add(badge);
                }
            }
        }

        return newlyUnlockedBadges;
    }

    /**
     * Kiểm tra điều kiện mở khóa cho từng mã huy hiệu cụ thể.
     */
    private boolean checkEligibility(String badgeCode, int totalGoals, int completedGoals,
                                     int completedTasks, int totalJournals, int longestStreak) {
        if (badgeCode == null) return false;

        switch (badgeCode) {
            case BADGE_FIRST_GOAL:
                return totalGoals >= 1;

            case BADGE_FIRST_TASK:
                return completedTasks >= 1;

            case BADGE_FIRST_JOURNAL:
                return totalJournals >= 1;

            case BADGE_STREAK_3:
                return longestStreak >= 3;

            case BADGE_STREAK_7:
                return longestStreak >= 7;

            case BADGE_STREAK_30:
                return longestStreak >= 30;

            case BADGE_GOAL_COMPLETED:
                return completedGoals >= 1;

            case BADGE_PRODUCTIVITY_10:
                return completedTasks >= 10;

            default:
                // Không hỗ trợ mã badge nằm ngoài 8 mã chuẩn
                return false;
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }
}
