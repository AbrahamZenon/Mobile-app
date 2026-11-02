package com.mad.goaltracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mad.goaltracker.R;
import com.mad.goaltracker.database.DatabaseHelper;

public class SecurityPinActivity extends AppCompatActivity {

    public static final String EXTRA_IS_SETTINGS_MODE = "IS_SETTINGS_MODE";

    private DatabaseHelper dbHelper;
    private EditText etPinCode;
    private Button btnSubmitPin, btnDisablePin;
    private TextView tvSecurityPrompt, tvSecurityTitle;
    private boolean isSettingsMode = false;
    private boolean isSecurityEnabled = false;
    private String savedPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_pin);

        dbHelper = new DatabaseHelper(this);
        isSecurityEnabled = dbHelper.isSecurityEnabled();
        savedPin = dbHelper.getPinCode();

        isSettingsMode = getIntent().getBooleanExtra(EXTRA_IS_SETTINGS_MODE, false);

        // Nếu mở app bình thường và chưa bật bảo mật -> đi thẳng vào MainActivity
        if (!isSettingsMode && (!isSecurityEnabled || savedPin == null || savedPin.isEmpty())) {
            navigateToMain();
            return;
        }

        initViews();
        setupMode();
    }

    private void initViews() {
        tvSecurityTitle = findViewById(R.id.tvSecurityTitle);
        tvSecurityPrompt = findViewById(R.id.tvSecurityPrompt);
        etPinCode = findViewById(R.id.etPinCode);
        btnSubmitPin = findViewById(R.id.btnSubmitPin);
        btnDisablePin = findViewById(R.id.btnDisablePin);
    }

    private void setupMode() {
        if (isSettingsMode) {
            tvSecurityTitle.setText("Cài Đặt Mã PIN Cá Nhân");
            if (isSecurityEnabled && !savedPin.isEmpty()) {
                tvSecurityPrompt.setText("Nhập 4 số để đổi mã PIN mới hoặc bấm nút bên dưới để tắt bảo mật.");
                btnDisablePin.setVisibility(View.VISIBLE);
                btnDisablePin.setOnClickListener(v -> {
                    dbHelper.setSecurity("", false);
                    Toast.makeText(this, "Đã tắt tính năng bảo mật mã PIN!", Toast.LENGTH_SHORT).finish();
                    finish();
                });
            } else {
                tvSecurityPrompt.setText("Thiết lập mã PIN 4 chữ số để khóa và bảo vệ mục tiêu cá nhân.");
                btnDisablePin.setVisibility(View.GONE);
            }
            btnSubmitPin.setText("LƯU MÃ PIN");
            btnSubmitPin.setOnClickListener(v -> saveNewPin());
        } else {
            // Chế độ mở khóa khi vào app
            tvSecurityTitle.setText("Khóa Ứng Dụng 🔒");
            tvSecurityPrompt.setText("Vui lòng nhập mã PIN 4 chữ số để tiếp tục truy cập dữ liệu mục tiêu.");
            btnDisablePin.setVisibility(View.GONE);
            btnSubmitPin.setText("MỞ KHÓA");
            btnSubmitPin.setOnClickListener(v -> verifyPin());
        }
    }

    private void saveNewPin() {
        String pin = etPinCode.getText().toString().trim();
        if (pin.length() != 4) {
            Toast.makeText(this, "Mã PIN phải có đúng 4 chữ số!", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.setSecurity(pin, true);
        Toast.makeText(this, "Đã kích hoạt bảo mật mã PIN thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void verifyPin() {
        String inputPin = etPinCode.getText().toString().trim();
        if (inputPin.equals(savedPin)) {
            navigateToMain();
        } else {
            Toast.makeText(this, "Mã PIN không đúng, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            etPinCode.setText("");
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(SecurityPinActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
