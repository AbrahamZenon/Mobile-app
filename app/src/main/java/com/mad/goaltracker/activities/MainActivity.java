package com.mad.goaltracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mad.goaltracker.R;
import com.mad.goaltracker.adapters.GoalAdapter;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.gamification.AchievementEventCoordinator;
import com.mad.goaltracker.gamification.GamificationIntegration;
import com.mad.goaltracker.models.GamificationDashboardData;
import com.mad.goaltracker.models.Goal;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Dashboard chính của ứng dụng GoalTracker.
 * Tích hợp toàn diện các phân hệ của Thành viên 2 (Prompt 8):
 * - Điều hướng: StatisticsActivity, GamificationActivity, NotificationSettingsActivity.
 * - Khối tóm tắt Gamification: Current Streak, Daily Quote, Badge Progress.
 * - Tự động hiển thị Achievement Dialog khi có thành tích mới sau các thao tác nghiệp vụ.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvGoals;
    private GoalAdapter goalAdapter;
    private TextView tvGoalCount, tvEmptyGoals;
    private ChipGroup chipGroupCategory;
    private FloatingActionButton fabAddGoal;
    private ImageButton btnSecuritySettings;

    // Navigation & Gamification Views (Prompt 8)
    private ImageButton btnStatistics;
    private ImageButton btnGamification;
    private ImageButton btnNotificationSettings;
    private CardView cardDashboardGamification;
    private TextView tvDashboardStreak;
    private TextView tvDashboardBadgeCount;
    private TextView tvDashboardQuote;
    private TextView tvDashboardQuoteAuthor;
    private MaterialButton btnNavStatistics;
    private MaterialButton btnNavGamification;
    private MaterialButton btnNavNotifications;

    private String currentCategoryFilter = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupCategoryFilter();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
        loadDashboardGamificationSummary();

        // Kiểm tra và hiển thị Achievement Celebration Dialog nếu có huy hiệu mới mở khóa
        AchievementEventCoordinator.checkAndShowCelebrationDialog(MainActivity.this, dbHelper);
    }

    private void initViews() {
        rvGoals = findViewById(R.id.rvGoals);
        tvGoalCount = findViewById(R.id.tvGoalCount);
        tvEmptyGoals = findViewById(R.id.tvEmptyGoals);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        fabAddGoal = findViewById(R.id.fabAddGoal);
        btnSecuritySettings = findViewById(R.id.btnSecuritySettings);

        // Prompt 8 Navigation & Gamification views
        btnStatistics = findViewById(R.id.btnStatistics);
        btnGamification = findViewById(R.id.btnGamification);
        btnNotificationSettings = findViewById(R.id.btnNotificationSettings);
        cardDashboardGamification = findViewById(R.id.cardDashboardGamification);
        tvDashboardStreak = findViewById(R.id.tvDashboardStreak);
        tvDashboardBadgeCount = findViewById(R.id.tvDashboardBadgeCount);
        tvDashboardQuote = findViewById(R.id.tvDashboardQuote);
        tvDashboardQuoteAuthor = findViewById(R.id.tvDashboardQuoteAuthor);
        btnNavStatistics = findViewById(R.id.btnNavStatistics);
        btnNavGamification = findViewById(R.id.btnNavGamification);
        btnNavNotifications = findViewById(R.id.btnNavNotifications);
    }

    private void setupRecyclerView() {
        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        goalAdapter = new GoalAdapter(this, new ArrayList<>());
        rvGoals.setAdapter(goalAdapter);
    }

    private void setupCategoryFilter() {
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = findViewById(checkedId);
            if (chip != null) {
                currentCategoryFilter = chip.getText().toString();
            } else {
                currentCategoryFilter = "Tất cả";
            }
            loadGoals();
        });
    }

    private void setupListeners() {
        fabAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditGoalActivity.class);
            startActivity(intent);
        });

        btnSecuritySettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecurityPinActivity.class);
            intent.putExtra(SecurityPinActivity.EXTRA_IS_SETTINGS_MODE, true);
            startActivity(intent);
        });

        // Prompt 8 Navigation Listeners
        View.OnClickListener statsListener = v -> startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
        if (btnStatistics != null) btnStatistics.setOnClickListener(statsListener);
        if (btnNavStatistics != null) btnNavStatistics.setOnClickListener(statsListener);

        View.OnClickListener gamificationListener = v -> startActivity(new Intent(MainActivity.this, GamificationActivity.class));
        if (btnGamification != null) btnGamification.setOnClickListener(gamificationListener);
        if (btnNavGamification != null) btnNavGamification.setOnClickListener(gamificationListener);
        if (cardDashboardGamification != null) cardDashboardGamification.setOnClickListener(gamificationListener);

        View.OnClickListener notifListener = v -> startActivity(new Intent(MainActivity.this, NotificationSettingsActivity.class));
        if (btnNotificationSettings != null) btnNotificationSettings.setOnClickListener(notifListener);
        if (btnNavNotifications != null) btnNavNotifications.setOnClickListener(notifListener);
    }

    private void loadGoals() {
        List<Goal> goals = dbHelper.getAllGoals(currentCategoryFilter);
        goalAdapter.setGoalList(goals);

        tvGoalCount.setText("Đang quản lý " + goals.size() + " mục tiêu");

        if (goals.isEmpty()) {
            tvEmptyGoals.setVisibility(View.VISIBLE);
            rvGoals.setVisibility(View.GONE);
        } else {
            tvEmptyGoals.setVisibility(View.GONE);
            rvGoals.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Tải dữ liệu tổng quan Gamification bất đồng bộ thông qua Facade GamificationIntegration.
     */
    private void loadDashboardGamificationSummary() {
        GamificationIntegration.loadDashboardData(this, new GamificationIntegration.GamificationCallback() {
            @Override
            public void onSuccess(final GamificationDashboardData data) {
                if (isFinishing() || isDestroyed()) return;
                if (data != null) {
                    if (tvDashboardStreak != null) {
                        tvDashboardStreak.setText(getString(R.string.dashboard_streak_format, data.getCurrentStreak()));
                    }
                    if (tvDashboardBadgeCount != null) {
                        tvDashboardBadgeCount.setText(getString(R.string.dashboard_badge_format, data.getUnlockedBadgesCount(), data.getTotalBadgesCount()));
                    }
                    if (data.getDailyQuote() != null) {
                        if (tvDashboardQuote != null) {
                            tvDashboardQuote.setText(data.getDailyQuote().getContent());
                        }
                        if (tvDashboardQuoteAuthor != null) {
                            tvDashboardQuoteAuthor.setText("— " + data.getDailyQuote().getAuthor());
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable error) {
                // Giữ nguyên giao diện mặc định, không chặn ứng dụng khi gặp lỗi
            }
        });
    }
}
