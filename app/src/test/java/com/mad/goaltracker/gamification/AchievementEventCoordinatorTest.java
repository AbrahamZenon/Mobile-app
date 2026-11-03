package com.mad.goaltracker.gamification;

import android.content.SharedPreferences;

import com.mad.goaltracker.models.Badge;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test Suite kiểm tra toàn bộ luồng hoạt động của PendingAchievementStore
 * và AchievementEventCoordinator (Prompt 8).
 * Kiểm tra tính toàn vẹn sự kiện, gom nhiều huy hiệu, cơ chế chống hiển thị trùng lặp.
 */
public class AchievementEventCoordinatorTest {

    private FakeSharedPreferences fakePrefs;
    private PendingAchievementStore store;

    @Before
    public void setUp() {
        fakePrefs = new FakeSharedPreferences();
        store = new PendingAchievementStore(fakePrefs);
    }

    @Test
    public void testStoreInitiallyEmpty() {
        assertFalse(store.hasPendingBadges());
        assertTrue(store.getPendingBadgeCodes().isEmpty());
    }

    @Test
    public void testAddSinglePendingBadge() {
        store.addPendingBadge("FIRST_GOAL");

        assertTrue(store.hasPendingBadges());
        List<String> pending = store.getPendingBadgeCodes();
        assertEquals(1, pending.size());
        assertEquals("FIRST_GOAL", pending.get(0));
    }

    @Test
    public void testAddMultiplePendingBadges() {
        store.addPendingBadges(Arrays.asList("FIRST_GOAL", "FIRST_TASK", "STREAK_3"));

        assertTrue(store.hasPendingBadges());
        List<String> pending = store.getPendingBadgeCodes();
        assertEquals(3, pending.size());
        assertTrue(pending.contains("FIRST_GOAL"));
        assertTrue(pending.contains("FIRST_TASK"));
        assertTrue(pending.contains("STREAK_3"));
    }

    @Test
    public void testIdempotencyDuplicateCodesNotDuplicated() {
        store.addPendingBadge("FIRST_GOAL");
        store.addPendingBadge("FIRST_GOAL");
        store.addPendingBadges(Arrays.asList("FIRST_GOAL", "FIRST_TASK", "FIRST_TASK"));

        List<String> pending = store.getPendingBadgeCodes();
        assertEquals(2, pending.size());
        assertTrue(pending.contains("FIRST_GOAL"));
        assertTrue(pending.contains("FIRST_TASK"));
    }

    @Test
    public void testNullOrEmptyCodesIgnored() {
        store.addPendingBadge(null);
        store.addPendingBadge("");
        store.addPendingBadge("   ");
        store.addPendingBadges(null);
        store.addPendingBadges(Collections.emptyList());

        assertFalse(store.hasPendingBadges());
        assertEquals(0, store.getPendingBadgeCodes().size());
    }

    @Test
    public void testMarkAllAsCelebratedClearsPending() {
        store.addPendingBadges(Arrays.asList("FIRST_GOAL", "PRODUCTIVITY_10"));
        assertTrue(store.hasPendingBadges());

        store.markAllAsCelebrated();

        assertFalse(store.hasPendingBadges());
        assertTrue(store.getPendingBadgeCodes().isEmpty());
    }

    @Test
    public void testCelebratedBadgesNeverReAddedToPending() {
        store.addPendingBadge("FIRST_GOAL");
        store.markAllAsCelebrated();

        // Cố tình thêm lại FIRST_GOAL sau khi đã chúc mừng
        store.addPendingBadge("FIRST_GOAL");

        // Phải bị chặn, không bao giờ xuất hiện lại trong pending
        assertFalse(store.hasPendingBadges());
        assertTrue(store.getPendingBadgeCodes().isEmpty());
    }

    @Test
    public void testNewBadgesCanStillBeAddedAfterOtherCelebrated() {
        store.addPendingBadge("FIRST_GOAL");
        store.markAllAsCelebrated();

        // Thêm badge khác chưa từng được chúc mừng
        store.addPendingBadge("FIRST_TASK");

        assertTrue(store.hasPendingBadges());
        List<String> pending = store.getPendingBadgeCodes();
        assertEquals(1, pending.size());
        assertEquals("FIRST_TASK", pending.get(0));
    }

    @Test
    public void testMultipleBadgesBatchGrouping() {
        // Mô phỏng trường hợp người dùng hoàn thành task dẫn đến đồng thời mở 2 badge: STREAK_3 và STREAK_7
        List<String> batch = Arrays.asList("STREAK_3", "STREAK_7");
        store.addPendingBadges(batch);

        List<String> pending = store.getPendingBadgeCodes();
        assertEquals("Phải gom cả 2 badge vào cùng một danh sách", 2, pending.size());
        assertTrue(pending.contains("STREAK_3"));
        assertTrue(pending.contains("STREAK_7"));
    }

    @Test
    public void testClearAllResetsEverything() {
        store.addPendingBadge("FIRST_GOAL");
        store.markAllAsCelebrated();
        store.addPendingBadge("FIRST_TASK");

        store.clearAll();

        assertFalse(store.hasPendingBadges());
        // Sau khi clearAll, có thể thêm lại FIRST_GOAL vì lịch sử đã bị reset
        store.addPendingBadge("FIRST_GOAL");
        assertTrue(store.hasPendingBadges());
    }

    @Test
    public void testMarkSpecificBadgesAsCelebratedLeavesOtherPending() {
        store.addPendingBadges(Arrays.asList("FIRST_TASK", "GOAL_COMPLETED"));
        assertEquals(2, store.getPendingBadgeCodes().size());

        // Chỉ xác nhận một huy hiệu đã hiển thị trong dialog (FIRST_TASK)
        store.markBadgesAsCelebrated(Collections.singletonList("FIRST_TASK"));

        List<String> pending = store.getPendingBadgeCodes();
        assertEquals(1, pending.size());
        assertEquals("GOAL_COMPLETED", pending.get(0));

        // Huy hiệu đã xác nhận không thể bị thêm lại
        store.addPendingBadge("FIRST_TASK");
        assertEquals(1, store.getPendingBadgeCodes().size());
        assertEquals("GOAL_COMPLETED", store.getPendingBadgeCodes().get(0));
    }

    @Test
    public void testSimulateAppKillBeforeAcknowledgement() {
        store.addPendingBadge("FIRST_GOAL");

        // Giả lập UI lấy pending để hiển thị Dialog
        List<String> badgesForDialog = store.getPendingBadgeCodes();
        assertEquals(1, badgesForDialog.size());

        // Giả lập app bị kill / đóng đột ngột (KHÔNG gọi markBadgesAsCelebrated)
        // Khi mở lại ứng dụng, pending vẫn phải còn nguyên vẹn
        PendingAchievementStore newSessionStore = new PendingAchievementStore(fakePrefs);
        assertTrue(newSessionStore.hasPendingBadges());
        assertEquals("FIRST_GOAL", newSessionStore.getPendingBadgeCodes().get(0));

        // Người dùng xem và bấm xác nhận
        newSessionStore.markBadgesAsCelebrated(Collections.singletonList("FIRST_GOAL"));
        assertFalse(newSessionStore.hasPendingBadges());
    }

    @Test
    public void testNewBadgeArrivesWhileExistingBadgePending() {
        // Huy hiệu 1 xuất hiện
        store.addPendingBadge("FIRST_TASK");

        // Dialog đang mở với huy hiệu 1 (chưa bấm nút đóng)
        List<String> currentDialogBadges = Collections.singletonList("FIRST_TASK");

        // Trong lúc đó, một sự kiện nền mở thêm huy hiệu 2
        store.addPendingBadge("PRODUCTIVITY_10");

        // Người dùng đóng Dialog hiện tại -> chỉ xác nhận huy hiệu 1
        store.markBadgesAsCelebrated(currentDialogBadges);

        // Huy hiệu 2 không được bị mất, vẫn nằm trong hàng đợi pending
        List<String> remainingPending = store.getPendingBadgeCodes();
        assertEquals(1, remainingPending.size());
        assertEquals("PRODUCTIVITY_10", remainingPending.get(0));
    }

    @Test
    public void testThreadSafetyConcurrentAccess() throws InterruptedException {
        int threadCount = 8;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final String badgeCode = "BADGE_" + i;
            threads[i] = new Thread(() -> {
                store.addPendingBadge(badgeCode);
                store.getPendingBadgeCodes();
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(threadCount, store.getPendingBadgeCodes().size());
    }

    // =========================================================================
    // IN-MEMORY FAKE SHAREDPREFERENCES CHO KIỂM THỬ ĐƠN VỊ THUẦN TÚY (NO ROBOLECTRIC)
    // =========================================================================

    private static class FakeSharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Map<String, ?> getAll() { return values; }

        @Override
        public String getString(String key, String defValue) {
            Object v = values.get(key);
            return v instanceof String ? (String) v : defValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Object v = values.get(key);
            return v instanceof Set ? new HashSet<>((Set<String>) v) : defValues;
        }

        @Override public int getInt(String key, int defValue) { return defValue; }
        @Override public long getLong(String key, long defValue) { return defValue; }
        @Override public float getFloat(String key, float defValue) { return defValue; }
        @Override public boolean getBoolean(String key, boolean defValue) { return defValue; }
        @Override public boolean contains(String key) { return values.containsKey(key); }
        @Override public Editor edit() { return new FakeEditor(this); }
        @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
        @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
    }

    private static class FakeEditor implements SharedPreferences.Editor {
        private final FakeSharedPreferences target;
        private final Map<String, Object> temp = new HashMap<>();
        private boolean clearRequested = false;

        FakeEditor(FakeSharedPreferences target) {
            this.target = target;
        }

        @Override public SharedPreferences.Editor putString(String key, String value) { temp.put(key, value); return this; }
        @Override public SharedPreferences.Editor putStringSet(String key, Set<String> values) { temp.put(key, values != null ? new HashSet<>(values) : null); return this; }
        @Override public SharedPreferences.Editor putInt(String key, int value) { temp.put(key, value); return this; }
        @Override public SharedPreferences.Editor putLong(String key, long value) { temp.put(key, value); return this; }
        @Override public SharedPreferences.Editor putFloat(String key, float value) { temp.put(key, value); return this; }
        @Override public SharedPreferences.Editor putBoolean(String key, boolean value) { temp.put(key, value); return this; }
        @Override public SharedPreferences.Editor remove(String key) { temp.put(key, null); return this; }
        @Override public SharedPreferences.Editor clear() { clearRequested = true; return this; }

        @Override
        public boolean commit() {
            apply();
            return true;
        }

        @Override
        public void apply() {
            if (clearRequested) {
                target.values.clear();
            }
            for (Map.Entry<String, Object> entry : temp.entrySet()) {
                if (entry.getValue() == null) {
                    target.values.remove(entry.getKey());
                } else {
                    target.values.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
