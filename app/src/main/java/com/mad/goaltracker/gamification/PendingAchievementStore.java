package com.mad.goaltracker.gamification;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Kho lưu trữ nhẹ (Persistence Store) sử dụng SharedPreferences để ghi nhận
 * các huy hiệu vừa được mở khóa nhưng chưa được thông báo/chúc mừng tới người dùng.
 * Đảm bảo:
 * - Không mất sự kiện khi Activity bị destroy hoặc ứng dụng bị kill.
 * - Chống thông báo trùng lặp (Idempotent).
 * - Thread-safe.
 */
public class PendingAchievementStore {

    private static final String PREF_NAME = "goaltracker_achievement_prefs";
    private static final String KEY_PENDING_CODES = "key_pending_badge_codes";
    private static final String KEY_CELEBRATED_CODES = "key_celebrated_badge_codes";

    private final SharedPreferences prefs;

    public PendingAchievementStore(Context context) {
        this(context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE));
    }

    public PendingAchievementStore(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    /**
     * Thêm một danh sách mã huy hiệu mới mở khóa vào hàng đợi pending.
     * Tự động loại trừ những mã đã được chúc mừng trước đó.
     */
    public synchronized void addPendingBadges(List<String> badgeCodes) {
        if (badgeCodes == null || badgeCodes.isEmpty()) return;

        Set<String> celebrated = prefs.getStringSet(KEY_CELEBRATED_CODES, Collections.emptySet());
        Set<String> currentPending = new LinkedHashSet<>(prefs.getStringSet(KEY_PENDING_CODES, Collections.emptySet()));

        for (String code : badgeCodes) {
            if (code != null && !code.trim().isEmpty()) {
                String cleanCode = code.trim();
                if (celebrated == null || !celebrated.contains(cleanCode)) {
                    currentPending.add(cleanCode);
                }
            }
        }

        prefs.edit().putStringSet(KEY_PENDING_CODES, currentPending).apply();
    }

    /**
     * Thêm một mã huy hiệu đơn lẻ vào hàng đợi pending.
     */
    public synchronized void addPendingBadge(String badgeCode) {
        if (badgeCode != null) {
            addPendingBadges(Collections.singletonList(badgeCode));
        }
    }

    /**
     * Lấy toàn bộ danh sách mã huy hiệu đang chờ thông báo.
     */
    public synchronized List<String> getPendingBadgeCodes() {
        Set<String> pendingSet = prefs.getStringSet(KEY_PENDING_CODES, Collections.emptySet());
        if (pendingSet == null || pendingSet.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(pendingSet);
    }

    /**
     * Kiểm tra có huy hiệu nào đang chờ thông báo hay không.
     */
    public synchronized boolean hasPendingBadges() {
        Set<String> pendingSet = prefs.getStringSet(KEY_PENDING_CODES, Collections.emptySet());
        return pendingSet != null && !pendingSet.isEmpty();
    }

    /**
     * Đánh dấu toàn bộ huy hiệu đang chờ đã được hiển thị chúc mừng.
     * Chuyển các mã này sang danh sách đã chúc mừng (celebrated) để vĩnh viễn không thông báo lại.
     */
    public synchronized void markAllAsCelebrated() {
        Set<String> currentPending = prefs.getStringSet(KEY_PENDING_CODES, Collections.emptySet());
        if (currentPending == null || currentPending.isEmpty()) return;

        Set<String> celebrated = new HashSet<>(prefs.getStringSet(KEY_CELEBRATED_CODES, Collections.emptySet()));
        celebrated.addAll(currentPending);

        prefs.edit()
                .putStringSet(KEY_CELEBRATED_CODES, celebrated)
                .putStringSet(KEY_PENDING_CODES, new HashSet<>())
                .apply();
    }

    /**
     * Xóa sạch toàn bộ store (dùng trong kiểm thử).
     */
    public synchronized void clearAll() {
        prefs.edit().clear().apply();
    }
}
