package com.mad.goaltracker.gamification;

import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Badge;

import java.util.List;

/**
 * Adapter triển khai GamificationDataSource kết nối trực tiếp với DatabaseHelper của Android SQLite.
 */
public class DatabaseGamificationDataSource implements GamificationDataSource {

    private final DatabaseHelper dbHelper;

    public DatabaseGamificationDataSource(DatabaseHelper dbHelper) {
        if (dbHelper == null) {
            throw new IllegalArgumentException("DatabaseHelper cannot be null");
        }
        this.dbHelper = dbHelper;
    }

    @Override
    public int getTotalGoalsCount() {
        return dbHelper.getTotalGoalsCount();
    }

    @Override
    public int getCompletedGoalsCount() {
        return dbHelper.getCompletedGoalsCount();
    }

    @Override
    public int getTotalTasksCount() {
        return dbHelper.getTotalTasksCount();
    }

    @Override
    public int getCompletedTasksCount() {
        return dbHelper.getCompletedTasksCount();
    }

    @Override
    public int getTotalJournalsCount() {
        return dbHelper.getTotalJournalsCount();
    }

    @Override
    public List<String> getAllActivityDates() {
        return dbHelper.getAllActivityDates();
    }

    @Override
    public List<Badge> getAllBadges() {
        return dbHelper.getAllBadges();
    }

    @Override
    public Badge getBadgeByCode(String code) {
        return dbHelper.getBadgeByCode(code);
    }

    @Override
    public boolean unlockBadge(String code, String unlockedAt) {
        return dbHelper.unlockBadge(code, unlockedAt);
    }

    @Override
    public int getUnlockedBadgesCount() {
        return dbHelper.getUnlockedBadgesCount();
    }
}
