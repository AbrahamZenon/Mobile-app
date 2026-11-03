package com.mad.goaltracker.utils;

import com.mad.goaltracker.R;

/**
 * Tiện ích ánh xạ tên icon từ Database sang tài nguyên drawable an toàn.
 * Chống crash khi icon_name null, rỗng hoặc không tồn tại.
 */
public class BadgeIconHelper {

    public static int getBadgeDrawableResId(String iconName) {
        if (iconName == null || iconName.trim().isEmpty()) {
            return R.drawable.ic_badge_default;
        }

        switch (iconName.trim()) {
            case "ic_badge_start":
                return R.drawable.ic_badge_start;
            case "ic_badge_task":
                return R.drawable.ic_badge_task;
            case "ic_badge_journal":
                return R.drawable.ic_badge_journal;
            case "ic_badge_streak3":
                return R.drawable.ic_badge_streak3;
            case "ic_badge_streak7":
                return R.drawable.ic_badge_streak7;
            case "ic_badge_streak30":
                return R.drawable.ic_badge_streak30;
            case "ic_badge_conquer":
                return R.drawable.ic_badge_conquer;
            case "ic_badge_master":
                return R.drawable.ic_badge_master;
            default:
                return R.drawable.ic_badge_default;
        }
    }
}
