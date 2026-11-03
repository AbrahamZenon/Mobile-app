package com.mad.goaltracker.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.mad.goaltracker.R;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.StatisticsData;
import com.mad.goaltracker.utils.StatisticsDateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình Thống Kê Tiến Độ, Phân Bố Mục Tiêu (PieChart) và Năng Suất 7 Ngày (BarChart).
 * Sử dụng MPAndroidChart v3.2.0, nạp dữ liệu ngầm ngoài UI Thread để đảm bảo độ mượt mà.
 */
public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Views
    private ImageButton btnBack;
    private ProgressBar progressBarLoading;
    private Button btnRefreshStatistics;

    // Overview KPIs
    private TextView tvStatTotalGoals;
    private TextView tvStatCompletedGoals;
    private TextView tvStatOverdueGoals;
    private TextView tvStatTotalTasks;
    private TextView tvStatCompletedTasks;
    private TextView tvStatAverageProgress;

    // Pie Chart & Legend
    private PieChart pieChartCategories;
    private TextView tvEmptyPieChart;
    private TextView tvLegendHealth;
    private TextView tvLegendWork;
    private TextView tvLegendStudy;
    private TextView tvLegendFinance;
    private TextView tvLegendHobby;

    // Bar Chart
    private BarChart barChartWeekly;
    private TextView tvEmptyBarChart;

    // Bảng màu 5 danh mục chuẩn khớp với colors.xml
    private static final Map<String, Integer> CATEGORY_COLORS = new LinkedHashMap<>();
    static {
        CATEGORY_COLORS.put("Sức khỏe", Color.parseColor("#06D6A0"));
        CATEGORY_COLORS.put("Công việc", Color.parseColor("#118AB2"));
        CATEGORY_COLORS.put("Học tập", Color.parseColor("#7209B7"));
        CATEGORY_COLORS.put("Tài chính", Color.parseColor("#FFD166"));
        CATEGORY_COLORS.put("Sở thích", Color.parseColor("#F72585"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        configureChartAppearances();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatisticsData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        btnRefreshStatistics = findViewById(R.id.btnRefreshStatistics);

        // Overview KPIs
        tvStatTotalGoals = findViewById(R.id.tvStatTotalGoals);
        tvStatCompletedGoals = findViewById(R.id.tvStatCompletedGoals);
        tvStatOverdueGoals = findViewById(R.id.tvStatOverdueGoals);
        tvStatTotalTasks = findViewById(R.id.tvStatTotalTasks);
        tvStatCompletedTasks = findViewById(R.id.tvStatCompletedTasks);
        tvStatAverageProgress = findViewById(R.id.tvStatAverageProgress);

        // Pie Chart & Legend
        pieChartCategories = findViewById(R.id.pieChartCategories);
        tvEmptyPieChart = findViewById(R.id.tvEmptyPieChart);
        tvLegendHealth = findViewById(R.id.tvLegendHealth);
        tvLegendWork = findViewById(R.id.tvLegendWork);
        tvLegendStudy = findViewById(R.id.tvLegendStudy);
        tvLegendFinance = findViewById(R.id.tvLegendFinance);
        tvLegendHobby = findViewById(R.id.tvLegendHobby);

        // Bar Chart
        barChartWeekly = findViewById(R.id.barChartWeekly);
        tvEmptyBarChart = findViewById(R.id.tvEmptyBarChart);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnRefreshStatistics.setOnClickListener(v -> loadStatisticsData());
    }

    private void configureChartAppearances() {
        // Cấu hình PieChart
        pieChartCategories.getDescription().setEnabled(false);
        pieChartCategories.getLegend().setEnabled(false); // Dùng custom legend tiếng Việt
        pieChartCategories.setDrawHoleEnabled(true);
        pieChartCategories.setHoleColor(Color.WHITE);
        pieChartCategories.setHoleRadius(50f);
        pieChartCategories.setTransparentCircleRadius(55f);
        pieChartCategories.setEntryLabelColor(Color.WHITE);
        pieChartCategories.setEntryLabelTextSize(11f);
        pieChartCategories.setRotationEnabled(true);
        pieChartCategories.setHighlightPerTapEnabled(true);

        // Cấu hình BarChart
        barChartWeekly.getDescription().setEnabled(false);
        barChartWeekly.getLegend().setEnabled(false);
        barChartWeekly.setDrawGridBackground(false);
        barChartWeekly.setDrawBarShadow(false);
        barChartWeekly.setDrawValueAboveBar(true);
        barChartWeekly.setPinchZoom(false);
        barChartWeekly.setScaleEnabled(false);
        barChartWeekly.getAxisRight().setEnabled(false); // Ẩn trục Y bên phải

        YAxis yAxisLeft = barChartWeekly.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setTextColor(Color.parseColor("#8D99AE"));
        yAxisLeft.setGridColor(Color.parseColor("#E9ECEF"));

        XAxis xAxis = barChartWeekly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#2B2D42"));
        xAxis.setTextSize(11f);
    }

    /**
     * Nạp toàn bộ dữ liệu thống kê ngầm ngoài Background Thread.
     */
    private void loadStatisticsData() {
        progressBarLoading.setVisibility(View.VISIBLE);
        btnRefreshStatistics.setEnabled(false);

        executor.execute(() -> {
            String todayStr = StatisticsDateUtils.getTodayDateString();
            List<String> last7Days = StatisticsDateUtils.getLast7Days();

            int totalGoals = dbHelper.getTotalGoalsCount();
            int completedGoals = dbHelper.getCompletedGoalsCount();
            int overdueGoals = dbHelper.getOverdueGoalsCount(todayStr);
            int totalTasks = dbHelper.getTotalTasksCount();
            int completedTasks = dbHelper.getCompletedTasksCount();
            float averageProgress = dbHelper.getOverallAverageProgress();

            Map<String, Integer> categoryCounts = dbHelper.getGoalCountByCategory();
            Map<String, Integer> weeklyCounts = dbHelper.getWeeklyCompletedTaskCount(last7Days);

            StatisticsData data = new StatisticsData(
                    totalGoals, completedGoals, overdueGoals,
                    totalTasks, completedTasks, averageProgress,
                    categoryCounts, weeklyCounts, last7Days
            );

            mainHandler.post(() -> {
                if (isFinishing() || (android.os.Build.VERSION.SDK_INT >= 17 && isDestroyed())) {
                    return;
                }
                progressBarLoading.setVisibility(View.GONE);
                btnRefreshStatistics.setEnabled(true);
                bindStatisticsDataToUI(data);
            });
        });
    }

    /**
     * Cập nhật số liệu lên giao diện người dùng trên Main Thread.
     */
    private void bindStatisticsDataToUI(StatisticsData data) {
        // 1. Cập nhật 6 thẻ chỉ số Tổng quan
        tvStatTotalGoals.setText(String.valueOf(data.getTotalGoals()));
        tvStatCompletedGoals.setText(String.valueOf(data.getCompletedGoals()));
        tvStatOverdueGoals.setText(String.valueOf(data.getOverdueGoals()));
        tvStatTotalTasks.setText(String.valueOf(data.getTotalTasks()));
        tvStatCompletedTasks.setText(String.valueOf(data.getCompletedTasks()));
        tvStatAverageProgress.setText(data.getFormattedAverageProgress());

        // 2. Cập nhật PieChart và Custom Legend
        renderCategoryPieChart(data);

        // 3. Cập nhật Weekly BarChart
        renderWeeklyBarChart(data);
    }

    private void renderCategoryPieChart(StatisticsData data) {
        if (!data.hasGoals()) {
            pieChartCategories.setVisibility(View.GONE);
            tvEmptyPieChart.setVisibility(View.VISIBLE);
        } else {
            tvEmptyPieChart.setVisibility(View.GONE);
            pieChartCategories.setVisibility(View.VISIBLE);

            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            for (String category : StatisticsData.STANDARD_CATEGORIES) {
                int count = data.getCategoryCount(category);
                if (count > 0) {
                    entries.add(new PieEntry(count, category));
                    int color = CATEGORY_COLORS.getOrDefault(category, Color.GRAY);
                    colors.add(color);
                }
            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setSliceSpace(2f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);

            PieData pieData = new PieData(dataSet);
            pieData.setValueFormatter(new PercentFormatter());
            pieChartCategories.setData(pieData);
            pieChartCategories.setUsePercentValues(true);
            pieChartCategories.animateY(800);
            pieChartCategories.invalidate();
        }

        // Cập nhật chi tiết 5 dòng legend (luôn hiển thị đủ 5 danh mục dù count = 0)
        updateLegendRow(tvLegendHealth, "Sức khỏe", data);
        updateLegendRow(tvLegendWork, "Công việc", data);
        updateLegendRow(tvLegendStudy, "Học tập", data);
        updateLegendRow(tvLegendFinance, "Tài chính", data);
        updateLegendRow(tvLegendHobby, "Sở thích", data);
    }

    private void updateLegendRow(TextView tv, String category, StatisticsData data) {
        int count = data.getCategoryCount(category);
        float pct = data.getCategoryPercentage(category);
        String text = String.format(Locale.US, "%d mục tiêu (%.1f%%)", count, pct);
        tv.setText(text);
    }

    private void renderWeeklyBarChart(StatisticsData data) {
        List<String> dates = data.getWeeklyDates();
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < dates.size(); i++) {
            String date = dates.get(i);
            int count = data.getWeeklyCounts().getOrDefault(date, 0);
            entries.add(new BarEntry(i, count));
        }

        // Cấu hình nhãn trục X theo thứ tiếng Việt (T2, T3, ..., CN)
        XAxis xAxis = barChartWeekly.getXAxis();
        xAxis.setValueFormatter((value, axis) -> {
            int index = (int) value;
            if (index >= 0 && index < dates.size()) {
                return StatisticsDateUtils.getDayOfWeekLabel(dates.get(index));
            }
            return "";
        });

        BarDataSet dataSet = new BarDataSet(entries, "Nhiệm vụ hoàn thành");
        dataSet.setColor(Color.parseColor("#4361EE"));
        dataSet.setValueTextColor(Color.parseColor("#2B2D42"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChartWeekly.setData(barData);

        // Hiển thị trạng thái rỗng nếu cả 7 ngày đều không có nhiệm vụ hoàn thành
        if (!data.hasWeeklyCompletedTasks()) {
            tvEmptyBarChart.setVisibility(View.VISIBLE);
        } else {
            tvEmptyBarChart.setVisibility(View.GONE);
        }

        barChartWeekly.animateY(800);
        barChartWeekly.invalidate();
    }
}
