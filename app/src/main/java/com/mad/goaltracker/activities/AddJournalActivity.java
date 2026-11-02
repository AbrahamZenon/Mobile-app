package com.mad.goaltracker.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mad.goaltracker.R;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Journal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddJournalActivity extends AppCompatActivity {

    private int goalId = -1;
    private DatabaseHelper dbHelper;

    private Spinner spJournalMood;
    private EditText etJournalContent, etJournalStrategy;
    private Button btnSaveJournal;

    private final String[] moods = {"Tuyệt vời ⭐", "Tốt 😊", "Bình thường 😐", "Cần cố gắng nhiều hơn 💪"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        dbHelper = new DatabaseHelper(this);
        goalId = getIntent().getIntExtra("GOAL_ID", -1);

        initViews();
        setupMoodSpinner();
        setupSaveButton();
    }

    private void initViews() {
        spJournalMood = findViewById(R.id.spJournalMood);
        etJournalContent = findViewById(R.id.etJournalContent);
        etJournalStrategy = findViewById(R.id.etJournalStrategy);
        btnSaveJournal = findViewById(R.id.btnSaveJournal);
    }

    private void setupMoodSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, moods);
        spJournalMood.setAdapter(adapter);
    }

    private void setupSaveButton() {
        btnSaveJournal.setOnClickListener(v -> {
            String content = etJournalContent.getText().toString().trim();
            String strategy = etJournalStrategy.getText().toString().trim();
            String mood = spJournalMood.getSelectedItem().toString();

            if (content.isEmpty()) {
                etJournalContent.setError("Vui lòng viết nội dung nhật ký!");
                etJournalContent.requestFocus();
                return;
            }

            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Journal journal = new Journal(goalId, currentDate, mood, content, strategy);
            long result = dbHelper.insertJournal(journal);

            if (result > 0) {
                Toast.makeText(this, "Đã lưu nhật ký hành trình!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi lưu bài viết nhật ký!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
