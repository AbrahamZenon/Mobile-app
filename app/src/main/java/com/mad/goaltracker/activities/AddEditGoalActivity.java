package com.mad.goaltracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mad.goaltracker.R;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Goal;

import java.util.Calendar;
import java.util.Locale;

public class AddEditGoalActivity extends AppCompatActivity {

    private EditText etGoalTitle, etGoalDescription;
    private Spinner spGoalCategory, spGoalDifficulty;
    private RadioGroup rgGoalType;
    private RadioButton rbShortTerm, rbLongTerm;
    private Button btnPickDeadline, btnSaveGoal;

    private DatabaseHelper dbHelper;
    private String selectedDeadline = "";

    private final String[] categories = {"Sức khỏe", "Công việc", "Học tập", "Tài chính", "Sở thích"};
    private final String[] difficulties = {"Dễ", "Trung bình", "Khó"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_goal);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupSpinners();
        setupDatePicker();
        setupSaveButton();
    }

    private void initViews() {
        etGoalTitle = findViewById(R.id.etGoalTitle);
        etGoalDescription = findViewById(R.id.etGoalDescription);
        spGoalCategory = findViewById(R.id.spGoalCategory);
        spGoalDifficulty = findViewById(R.id.spGoalDifficulty);
        rgGoalType = findViewById(R.id.rgGoalType);
        rbShortTerm = findViewById(R.id.rbShortTerm);
        rbLongTerm = findViewById(R.id.rbLongTerm);
        btnPickDeadline = findViewById(R.id.btnPickDeadline);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spGoalCategory.setAdapter(catAdapter);

        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, difficulties);
        spGoalDifficulty.setAdapter(diffAdapter);
        spGoalDifficulty.setSelection(1); // Mặc định là "Trung bình"
    }

    private void setupDatePicker() {
        btnPickDeadline.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        selectedDeadline = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                        btnPickDeadline.setText("Hạn chót: " + selectedDeadline);
                    }, year, month, day);

            // Ràng buộc nghiệp vụ: Không được chọn ngày trong quá khứ
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void setupSaveButton() {
        btnSaveGoal.setOnClickListener(v -> {
            String title = etGoalTitle.getText().toString().trim();
            String desc = etGoalDescription.getText().toString().trim();
            String category = spGoalCategory.getSelectedItem().toString();
            String difficulty = spGoalDifficulty.getSelectedItem().toString();
            String type = rbShortTerm.isChecked() ? "Ngắn hạn" : "Dài hạn";

            if (title.isEmpty()) {
                etGoalTitle.setError("Vui lòng nhập tên mục tiêu!");
                etGoalTitle.requestFocus();
                return;
            }

            if (selectedDeadline.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ngày hạn chót (Deadline)!", Toast.LENGTH_SHORT).show();
                return;
            }

            Goal newGoal = new Goal(title, desc, category, type, difficulty, selectedDeadline);
            long result = dbHelper.insertGoal(newGoal);

            if (result > 0) {
                com.mad.goaltracker.gamification.AchievementEventCoordinator.triggerEvaluation(AddEditGoalActivity.this);
                Toast.makeText(this, "Đã thêm mục tiêu thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Có lỗi xảy ra khi lưu mục tiêu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
