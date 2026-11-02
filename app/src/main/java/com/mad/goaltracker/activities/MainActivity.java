package com.mad.goaltracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mad.goaltracker.R;
import com.mad.goaltracker.adapters.GoalAdapter;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Goal;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvGoals;
    private GoalAdapter goalAdapter;
    private TextView tvGoalCount, tvEmptyGoals;
    private ChipGroup chipGroupCategory;
    private FloatingActionButton fabAddGoal;
    private ImageButton btnSecuritySettings;

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
    }

    private void initViews() {
        rvGoals = findViewById(R.id.rvGoals);
        tvGoalCount = findViewById(R.id.tvGoalCount);
        tvEmptyGoals = findViewById(R.id.tvEmptyGoals);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        fabAddGoal = findViewById(R.id.fabAddGoal);
        btnSecuritySettings = findViewById(R.id.btnSecuritySettings);
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
}
