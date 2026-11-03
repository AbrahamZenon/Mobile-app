package com.mad.goaltracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.goaltracker.R;
import com.mad.goaltracker.adapters.TaskAdapter;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Goal;
import com.mad.goaltracker.models.Task;

import java.util.ArrayList;
import java.util.List;

public class GoalDetailActivity extends AppCompatActivity implements TaskAdapter.OnTaskStatusChangeListener {

    private Goal goal;
    private DatabaseHelper dbHelper;

    private TextView tvDetailTitle, tvDetailDesc, tvDetailCategory, tvDetailDifficulty, tvDetailDeadline, tvDetailProgressText, tvEmptyTasks;
    private ProgressBar progressBarDetail;
    private Button btnOpenJournal, btnAddTask;
    private ImageButton btnDeleteGoal;
    private EditText etTaskTitle;
    private Spinner spPlanType, spTaskPriority;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;

    private final String[] planTypes = {"Hàng ngày", "Hàng tuần"};
    private final String[] priorities = {"Cao", "Trung bình", "Thấp"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        dbHelper = new DatabaseHelper(this);
        goal = (Goal) getIntent().getSerializableExtra("GOAL_OBJECT");

        if (goal == null) {
            Toast.makeText(this, "Không tìm thấy dữ liệu mục tiêu!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayGoalInfo();
        setupSpinners();
        setupRecyclerView();
        setupListeners();
        loadTasks();
    }

    private void initViews() {
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDifficulty = findViewById(R.id.tvDetailDifficulty);
        tvDetailDeadline = findViewById(R.id.tvDetailDeadline);
        tvDetailProgressText = findViewById(R.id.tvDetailProgressText);
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);
        progressBarDetail = findViewById(R.id.progressBarDetail);
        btnOpenJournal = findViewById(R.id.btnOpenJournal);
        btnDeleteGoal = findViewById(R.id.btnDeleteGoal);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        btnAddTask = findViewById(R.id.btnAddTask);
        spPlanType = findViewById(R.id.spPlanType);
        spTaskPriority = findViewById(R.id.spTaskPriority);
        rvTasks = findViewById(R.id.rvTasks);
    }

    private void displayGoalInfo() {
        tvDetailTitle.setText(goal.getTitle());
        tvDetailDesc.setText(goal.getDescription() != null && !goal.getDescription().isEmpty() ? goal.getDescription() : "Không có mô tả chi tiết.");
        tvDetailCategory.setText(goal.getCategory());
        tvDetailDifficulty.setText("Độ khó: " + goal.getDifficulty());
        tvDetailDeadline.setText("Hạn chót: " + goal.getDeadline());
        updateProgressViews(goal.getProgress());
    }

    private void updateProgressViews(int progress) {
        progressBarDetail.setProgress(progress);
        tvDetailProgressText.setText(progress + "%");
    }

    private void setupSpinners() {
        ArrayAdapter<String> planAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, planTypes);
        spPlanType.setAdapter(planAdapter);

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        spTaskPriority.setAdapter(priorityAdapter);
    }

    private void setupRecyclerView() {
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(this, new ArrayList<>(), this);
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        btnAddTask.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            if (title.isEmpty()) {
                etTaskTitle.setError("Nhập tên nhiệm vụ cần làm!");
                return;
            }

            String planType = spPlanType.getSelectedItem().toString();
            String priority = spTaskPriority.getSelectedItem().toString();

            Task task = new Task(goal.getId(), title, priority, planType, goal.getDeadline());
            dbHelper.insertTask(task);
            etTaskTitle.setText("");
            Toast.makeText(this, "Đã thêm hành động vào kế hoạch!", Toast.LENGTH_SHORT).show();

            loadTasks();
            refreshGoalProgress();
        });

        btnOpenJournal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalDetailActivity.this, JournalActivity.class);
            intent.putExtra("GOAL_ID", goal.getId());
            intent.putExtra("GOAL_TITLE", goal.getTitle());
            startActivity(intent);
        });

        btnDeleteGoal.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa mục tiêu này cùng toàn bộ kế hoạch và nhật ký liên quan không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        dbHelper.deleteGoal(goal.getId());
                        Toast.makeText(this, "Đã xóa mục tiêu!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadTasks() {
        List<Task> tasks = dbHelper.getTasksByGoal(goal.getId());
        taskAdapter.setTaskList(tasks);

        if (tasks.isEmpty()) {
            tvEmptyTasks.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            tvEmptyTasks.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }
    }

    private void refreshGoalProgress() {
        List<Goal> goals = dbHelper.getAllGoals("Tất cả");
        for (Goal g : goals) {
            if (g.getId() == goal.getId()) {
                goal.setProgress(g.getProgress());
                updateProgressViews(g.getProgress());
                break;
            }
        }
    }

    @Override
    public void onStatusChanged(Task task, boolean isCompleted) {
        boolean transitioned = dbHelper.updateTaskCompletion(task.getId(), isCompleted, goal.getId());
        refreshGoalProgress();
        loadTasks();
        if (transitioned) {
            com.mad.goaltracker.gamification.AchievementEventCoordinator.triggerEvaluation(GoalDetailActivity.this);
        }
    }

    @Override
    public void onDeleteTask(Task task) {
        dbHelper.deleteTask(task.getId(), goal.getId());
        refreshGoalProgress();
        loadTasks();
    }
}
