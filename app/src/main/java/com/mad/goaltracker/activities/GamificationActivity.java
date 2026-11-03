package com.mad.goaltracker.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.goaltracker.R;
import com.mad.goaltracker.adapters.BadgeAdapter;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.gamification.DatabaseGamificationDataSource;
import com.mad.goaltracker.gamification.GamificationManager;
import com.mad.goaltracker.gamification.MotivationalQuoteProvider;
import com.mad.goaltracker.gamification.StreakSummary;
import com.mad.goaltracker.models.Badge;
import com.mad.goaltracker.models.Quote;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình Thành Tích & Gamification của GoalTracker.
 * Hiển thị:
 * - Chuỗi hoạt động hiện tại (Current Streak) & Kỷ lục chuỗi (Longest Streak)
 * - Câu danh ngôn tạo động lực hằng ngày (Daily Motivational Quote)
 * - Tiến độ mở khóa huy hiệu (X / 8 huy hiệu)
 * - Lưới 2 cột hiển thị đầy đủ 8 huy hiệu thành tích
 * - Tự động hiển thị Achievement Dialog khi người dùng vừa mở khóa huy hiệu mới
 */
public class GamificationActivity extends AppCompatActivity implements BadgeAdapter.OnBadgeClickListener {

    private static final String KEY_HAS_EVALUATED = "key_has_evaluated";

    private DatabaseHelper dbHelper;
    private GamificationManager gamificationManager;
    private MotivationalQuoteProvider quoteProvider;
    private BadgeAdapter badgeAdapter;

    private ExecutorService executor;
    private Handler mainHandler;

    private TextView tvCurrentStreakNumber;
    private TextView tvLongestStreak;
    private TextView tvStreakEncouragement;
    private TextView tvQuoteContent;
    private TextView tvQuoteAuthor;
    private TextView tvBadgeProgressSummary;
    private RecyclerView rvBadges;
    private ProgressBar progressBarLoading;
    private ImageButton btnBack;
    private Button btnRefreshGamification;

    // Cờ đánh dấu đã đánh giá mở khóa huy hiệu trong phiên (tránh đánh giá trùng khi xoay màn hình)
    private boolean hasEvaluatedBadges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamification);

        dbHelper = new DatabaseHelper(this);
        DatabaseGamificationDataSource dataSource = new DatabaseGamificationDataSource(dbHelper);
        gamificationManager = new GamificationManager(dataSource);
        quoteProvider = new MotivationalQuoteProvider();

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupRecyclerView();

        if (savedInstanceState != null) {
            hasEvaluatedBadges = savedInstanceState.getBoolean(KEY_HAS_EVALUATED, false);
        }

        // Tải dữ liệu; chỉ đánh giá mở khóa huy hiệu nếu chưa được đánh giá trong phiên này
        loadGamificationData(!hasEvaluatedBadges);
    }

    private void initViews() {
        tvCurrentStreakNumber = findViewById(R.id.tvCurrentStreakNumber);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        tvStreakEncouragement = findViewById(R.id.tvStreakEncouragement);
        tvQuoteContent = findViewById(R.id.tvQuoteContent);
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor);
        tvBadgeProgressSummary = findViewById(R.id.tvBadgeProgressSummary);
        rvBadges = findViewById(R.id.rvBadges);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        btnBack = findViewById(R.id.btnBack);
        btnRefreshGamification = findViewById(R.id.btnRefreshGamification);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnRefreshGamification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi bấm làm mới thủ công, cho phép đánh giá lại huy hiệu
                loadGamificationData(true);
            }
        });
    }

    private void setupRecyclerView() {
        badgeAdapter = new BadgeAdapter(new ArrayList<Badge>(), this);
        rvBadges.setLayoutManager(new GridLayoutManager(this, 2));
        rvBadges.setAdapter(badgeAdapter);
    }

    /**
     * Tải toàn bộ dữ liệu Gamification ngầm trên Background Thread.
     *
     * @param shouldEvaluateBadges Nếu true, chạy logic đánh giá và mở khóa badge mới
     */
    private void loadGamificationData(final boolean shouldEvaluateBadges) {
        if (progressBarLoading != null) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 1. Kiểm tra huy hiệu đang chờ chúc mừng từ PendingAchievementStore hoặc đánh giá mới
                    List<Badge> badgesToCelebrate = com.mad.goaltracker.gamification.AchievementEventCoordinator.getPendingBadgesToCelebrate(GamificationActivity.this, dbHelper);
                    if (badgesToCelebrate == null) {
                        badgesToCelebrate = new ArrayList<>();
                    }

                    if (badgesToCelebrate.isEmpty() && shouldEvaluateBadges) {
                        List<Badge> newlyEvaluated = gamificationManager.evaluateAndUnlockBadges();
                        if (newlyEvaluated != null && !newlyEvaluated.isEmpty()) {
                            badgesToCelebrate = newlyEvaluated;
                        }
                    }

                    final List<Badge> newlyUnlocked = badgesToCelebrate;

                    // 2. Tính toán chuỗi hoạt động
                    final StreakSummary streakSummary = gamificationManager.getStreakSummary();

                    // 3. Lấy câu danh ngôn trong ngày
                    final Quote dailyQuote = quoteProvider.getDailyQuote();

                    // 4. Lấy danh sách toàn bộ 8 huy hiệu từ Database (đã cập nhật trạng thái mới nhất)
                    final List<Badge> allBadges = dbHelper.getAllBadges();
                    final int unlockedCount = dbHelper.getUnlockedBadgesCount();

                    // 5. Cập nhật giao diện trên Main Thread
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing() || isDestroyed()) return;

                            bindGamificationData(streakSummary, dailyQuote, allBadges, unlockedCount);

                            // Nếu có huy hiệu vừa mở khóa thành công, hiển thị Dialog chúc mừng
                            if (newlyUnlocked != null && !newlyUnlocked.isEmpty()) {
                                showAchievementDialog(newlyUnlocked);
                            }

                            hasEvaluatedBadges = true;

                            if (progressBarLoading != null) {
                                progressBarLoading.setVisibility(View.GONE);
                            }
                        }
                    });

                } catch (final Throwable t) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBarLoading != null) {
                                progressBarLoading.setVisibility(View.GONE);
                            }
                            Toast.makeText(GamificationActivity.this,
                                    "Không thể tải dữ liệu thành tích. Vui lòng thử lại!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Gắn dữ liệu lên giao diện người dùng.
     */
    private void bindGamificationData(StreakSummary streak, Quote quote, List<Badge> badges, int unlockedCount) {
        // Gắn Streak Card
        int currentStreak = (streak != null) ? streak.getCurrentStreak() : 0;
        int longestStreak = (streak != null) ? streak.getLongestStreak() : 0;

        tvCurrentStreakNumber.setText(String.format(Locale.US, "%d NGÀY", currentStreak));
        tvLongestStreak.setText(getString(R.string.streak_record_label, longestStreak));

        if (currentStreak == 0) {
            tvStreakEncouragement.setVisibility(View.VISIBLE);
        } else {
            tvStreakEncouragement.setVisibility(View.GONE);
        }

        // Gắn Daily Quote Card
        if (quote != null) {
            tvQuoteContent.setText(String.format("“%s”", quote.getContent()));
            tvQuoteAuthor.setText(String.format("— %s", quote.getAuthor()));
        }

        // Gắn Badges Section
        int totalBadges = (badges != null && !badges.isEmpty()) ? badges.size() : 8;
        tvBadgeProgressSummary.setText(getString(R.string.badge_progress_summary, unlockedCount, totalBadges));

        if (badgeAdapter != null && badges != null) {
            badgeAdapter.updateBadges(badges);
        }
    }

    /**
     * Hiển thị Achievement Dialog khi có huy hiệu mới mở khóa.
     */
    private void showAchievementDialog(List<Badge> newlyUnlocked) {
        if (newlyUnlocked == null || newlyUnlocked.isEmpty()) return;
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isStateSaved()) return;
        if (getSupportFragmentManager().findFragmentByTag(AchievementDialogFragment.TAG) != null) return;

        AchievementDialogFragment dialog = AchievementDialogFragment.newCelebrationInstance(new ArrayList<>(newlyUnlocked));
        dialog.show(getSupportFragmentManager(), AchievementDialogFragment.TAG);
    }

    /**
     * Sự kiện khi người dùng click vào một huy hiệu trong danh sách.
     */
    @Override
    public void onBadgeClick(Badge badge) {
        if (badge == null) return;
        AchievementDialogFragment dialog = AchievementDialogFragment.newDetailInstance(badge);
        dialog.show(getSupportFragmentManager(), AchievementDialogFragment.TAG);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HAS_EVALUATED, hasEvaluatedBadges);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
