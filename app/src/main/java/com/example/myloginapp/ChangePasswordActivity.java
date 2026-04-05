package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.SessionManager;
import com.example.myloginapp.ui.UiDialogHelper;

public class ChangePasswordActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_PROVIDER = "extra_provider";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private String username;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        username = getIntent().getStringExtra(EXTRA_USERNAME);
        provider = getIntent().getStringExtra(EXTRA_PROVIDER);

        if (username == null) {
            username = sessionManager.getUsername();
        }
        if (provider == null) {
            provider = sessionManager.getProvider();
        }

        if (username == null) {
            goToLogin();
            return;
        }

        if (!"local".equalsIgnoreCase(provider)) {
            UiDialogHelper.showStatus(
                    this,
                    UiDialogHelper.Type.INFO,
                    getString(R.string.status_info),
                    getString(R.string.password_not_available_social),
                    this::finish
            );
            return;
        }

        EditText currentPasswordInput = findViewById(R.id.currentPasswordInput);
        EditText newPasswordInput = findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        MaterialButton submitBtn = findViewById(R.id.submitChangePasswordBtn);

        submitBtn.setOnClickListener(v -> {
            String currentPassword = currentPasswordInput.getText().toString();
            String newPassword = newPasswordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_missing_fields),
                        null
                );
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_password_mismatch),
                        null
                );
                return;
            }

            boolean updated = databaseHelper.updatePassword(username, currentPassword, newPassword);
            if (!updated) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_wrong_current_password),
                        null
                );
                return;
            }

            UiDialogHelper.showStatus(
                    this,
                    UiDialogHelper.Type.SUCCESS,
                    getString(R.string.status_success),
                    getString(R.string.password_changed),
                    this::finish
            );
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
