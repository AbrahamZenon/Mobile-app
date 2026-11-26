package com.mad.goaltracker.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.mad.goaltracker.utils.StatisticsDateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình Thành Tích & Gamification phong cách Duolingo của GoalTracker.
 * Hiển thị:
 * - Hero Streak Card + Dải lịch hoạt động 7 ngày (T2 - CN)
 * - Mascot Speech Bubble với câu danh ngôn tạo động lực hằng ngày
 * - Tủ huy hiệu 2 cột hiển thị đầy đủ 8 huy hiệu thành tích
 * - Dialog ăn mừng kèm hiệu ứng Confetti khi vừa mở khóa huy hiệu mới
 */
public class GamificationActivity extends AppCompatActivity implements BadgeAdapter.OnBadgeClickListener {

    private static final String KEY_HAS_EVALUATED = "key_has_evaluated";

    private DatabaseHelper dbHelper;
    private GamificationManager gamificationManager;
    private MotivationalQuoteProvider quoteProvider;
    private BadgeAdapter badgeAdapter;

    private ExecutorService executor;
    private Handler mainHandler;

    private ImageView ivStreakFlame;
    private TextView tvCurrentStreakNumber;
    private TextView tvLongestStreak;
    private TextView tvStreakEncouragement;
    private LinearLayout layoutStreakWeekStrip;
    private TextView tvQuoteContent;
    private TextView tvQuoteAuthor;
    private TextView tvBadgeProgressSummary;
    private RecyclerView rvBadges;
    private ProgressBar progressBarLoading;
    private ImageButton btnBack;
    private Button btnRefreshGamification;

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

        loadGamificationData(!hasEvaluatedBadges);
    }

    private void initViews() {
        ivStreakFlame = findViewById(R.id.ivStreakFlame);
        tvCurrentStreakNumber = findViewById(R.id.tvCurrentStreakNumber);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        tvStreakEncouragement = findViewById(R.id.tvStreakEncouragement);
        layoutStreakWeekStrip = findViewById(R.id.layoutStreakWeekStrip);
        tvQuoteContent = findViewById(R.id.tvQuoteContent);
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor);
        tvBadgeProgressSummary = findViewById(R.id.tvBadgeProgressSummary);
        rvBadges = findViewById(R.id.rvBadges);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        btnBack = findViewById(R.id.btnBack);
        btnRefreshGamification = findViewById(R.id.btnRefreshGamification);

        btnBack.setOnClickListener(v -> onBackPressed());

        btnRefreshGamification.setOnClickListener(v -> loadGamificationData(true));
    }

    private void setupRecyclerView() {
        badgeAdapter = new BadgeAdapter(new ArrayList<Badge>(), this);
        rvBadges.setLayoutManager(new GridLayoutManager(this, 2));
        rvBadges.setAdapter(badgeAdapter);
    }

    private void loadGamificationData(final boolean shouldEvaluateBadges) {
        if (progressBarLoading != null) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        executor.execute(() -> {
            try {
                List<Badge> badgesToCelebrate = com.mad.goaltracker.gamification.AchievementEventCoordinator
                        .getPendingBadgesToCelebrate(GamificationActivity.this, dbHelper);
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
                final StreakSummary streakSummary = gamificationManager.getStreakSummary();
                final List<String> activityDates = dbHelper.getAllActivityDates();
                final Quote dailyQuote = quoteProvider.getDailyQuote();
                final List<Badge> allBadges = dbHelper.getAllBadges();
                final int unlockedCount = dbHelper.getUnlockedBadgesCount();

                mainHandler.post(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    bindGamificationData(streakSummary, activityDates, dailyQuote, allBadges, unlockedCount);

                    if (newlyUnlocked != null && !newlyUnlocked.isEmpty()) {
                        showAchievementDialog(newlyUnlocked);
                    }

                    hasEvaluatedBadges = true;

                    if (progressBarLoading != null) {
                        progressBarLoading.setVisibility(View.GONE);
                    }
                });

            } catch (final Throwable t) {
                mainHandler.post(() -> {
                    if (progressBarLoading != null) {
                        progressBarLoading.setVisibility(View.GONE);
                    }
                    Toast.makeText(GamificationActivity.this,
                            "Không thể tải dữ liệu thành tích. Vui lòng thử lại!",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void bindGamificationData(StreakSummary streak, List<String> activityDates, Quote quote,
                                      List<Badge> badges, int unlockedCount) {
        int currentStreak = (streak != null) ? streak.getCurrentStreak() : 0;
        int longestStreak = (streak != null) ? streak.getLongestStreak() : 0;

        tvCurrentStreakNumber.setText(String.format(Locale.US, "%d NGÀY", currentStreak));
        tvLongestStreak.setText(getString(R.string.streak_record_label, longestStreak));

        if (currentStreak > 0 && ivStreakFlame != null) {
            ivStreakFlame.setScaleX(0.75f);
            ivStreakFlame.setScaleY(0.75f);
            ivStreakFlame.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(450)
                    .setInterpolator(new OvershootInterpolator(2.0f))
                    .start();
        }

        if (currentStreak == 0) {
            tvStreakEncouragement.setVisibility(View.VISIBLE);
        } else {
            tvStreakEncouragement.setVisibility(View.GONE);
        }

        renderStreakWeekStrip(activityDates);

        if (quote != null) {
            tvQuoteContent.setText(String.format("“%s”", quote.getContent()));
            tvQuoteAuthor.setText(String.format("— %s", quote.getAuthor()));
        }

        int totalBadges = (badges != null && !badges.isEmpty()) ? badges.size() : 8;
        tvBadgeProgressSummary.setText(getString(R.string.badge_progress_summary, unlockedCount, totalBadges));

        if (badgeAdapter != null && badges != null) {
            badgeAdapter.updateBadges(badges);
        }
    }

    /**
     * Render dải lịch 7 ngày gần nhất kiểu Duolingo (T2 - CN) dựa trên ngày hoạt động thật trong SQLite.
     */
    private void renderStreakWeekStrip(List<String> activityDates) {
        if (layoutStreakWeekStrip == null) return;
        layoutStreakWeekStrip.removeAllViews();

        Set<String> activeSet = new HashSet<>();
        if (activityDates != null) {
            activeSet.addAll(activityDates);
        }

        List<String> last7Days = StatisticsDateUtils.getLast7Days();
        float density = getResources().getDisplayMetrics().density;
        int dotSizePx = (int) (30 * density);

        for (int i = 0; i < last7Days.size(); i++) {
            String dateStr = last7Days.get(i);
            boolean isActive = activeSet.contains(dateStr);
            boolean isToday = (i == last7Days.size() - 1);
            String dayLabel = StatisticsDateUtils.getDayOfWeekLabel(dateStr);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            col.setLayoutParams(colParams);

            TextView tvDay = new TextView(this);
            tvDay.setText(dayLabel);
            tvDay.setTextSize(11f);
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setTextColor(isToday ? Color.parseColor("#E07E00") : Color.parseColor("#777777"));
            tvDay.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView dot = new TextView(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            dotParams.topMargin = (int) (4 * density);
            dot.setLayoutParams(dotParams);
            dot.setGravity(Gravity.CENTER);
            dot.setTextSize(13f);
            dot.setTypeface(null, android.graphics.Typeface.BOLD);

            if (isActive) {
                dot.setBackgroundResource(R.drawable.bg_duo_day_active);
                dot.setText("✓");
                dot.setTextColor(Color.WHITE);
            } else {
                dot.setBackgroundResource(R.drawable.bg_duo_day_inactive);
                dot.setText(isToday ? "•" : "");
                dot.setTextColor(Color.parseColor("#AFAFAF"));
            }

            col.addView(tvDay);
            col.addView(dot);
            layoutStreakWeekStrip.addView(col);
        }
    }

    private void showAchievementDialog(List<Badge> newlyUnlocked) {
        if (newlyUnlocked == null || newlyUnlocked.isEmpty()) return;
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isStateSaved()) return;
        if (getSupportFragmentManager().findFragmentByTag(AchievementDialogFragment.TAG) != null) return;

        AchievementDialogFragment dialog = AchievementDialogFragment.newCelebrationInstance(new ArrayList<>(newlyUnlocked));
        dialog.show(getSupportFragmentManager(), AchievementDialogFragment.TAG);
    }

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
