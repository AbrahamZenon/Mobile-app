package com.mad.goaltracker.gamification;

import com.mad.goaltracker.models.Badge;

import java.util.List;

/**
 * Interface trừu tượng hóa tầng dữ liệu cho Gamification Engine.
 * Cho phép tách biệt logic đánh giá huy hiệu khỏi Android SQLite để viết Unit Test độc lập.
 */
public interface GamificationDataSource {

    int getTotalGoalsCount();

    int getCompletedGoalsCount();

    int getTotalTasksCount();

    int getCompletedTasksCount();

    int getTotalJournalsCount();

    List<String> getAllActivityDates();

    List<Badge> getAllBadges();

    Badge getBadgeByCode(String code);

    boolean unlockBadge(String code, String unlockedAt);

    int getUnlockedBadgesCount();
}
