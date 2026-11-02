package com.mad.goaltracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mad.goaltracker.R;
import com.mad.goaltracker.adapters.JournalAdapter;
import com.mad.goaltracker.database.DatabaseHelper;
import com.mad.goaltracker.models.Journal;

import java.util.ArrayList;
import java.util.List;

public class JournalActivity extends AppCompatActivity {

    private int goalId = -1;
    private String goalTitle = "";
    private DatabaseHelper dbHelper;

    private RecyclerView rvJournals;
    private JournalAdapter journalAdapter;
    private TextView tvEmptyJournals;
    private FloatingActionButton fabAddJournal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        dbHelper = new DatabaseHelper(this);
        goalId = getIntent().getIntExtra("GOAL_ID", -1);
        goalTitle = getIntent().getStringExtra("GOAL_TITLE");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nhật ký: " + goalTitle);
        }

        initViews();
        setupRecyclerView();
        setupFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJournals();
    }

    private void initViews() {
        rvJournals = findViewById(R.id.rvJournals);
        tvEmptyJournals = findViewById(R.id.tvEmptyJournals);
        fabAddJournal = findViewById(R.id.fabAddJournal);
    }

    private void setupRecyclerView() {
        rvJournals.setLayoutManager(new LinearLayoutManager(this));
        journalAdapter = new JournalAdapter(this, new ArrayList<>());
        rvJournals.setAdapter(journalAdapter);
    }

    private void setupFab() {
        fabAddJournal.setOnClickListener(v -> {
            Intent intent = new Intent(JournalActivity.this, AddJournalActivity.class);
            intent.putExtra("GOAL_ID", goalId);
            startActivity(intent);
        });
    }

    private void loadJournals() {
        List<Journal> journals = dbHelper.getJournalsByGoal(goalId);
        journalAdapter.setJournalList(journals);

        if (journals.isEmpty()) {
            tvEmptyJournals.setVisibility(View.VISIBLE);
            rvJournals.setVisibility(View.GONE);
        } else {
            tvEmptyJournals.setVisibility(View.GONE);
            rvJournals.setVisibility(View.VISIBLE);
        }
    }
}
