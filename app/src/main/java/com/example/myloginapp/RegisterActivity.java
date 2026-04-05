package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.ui.UiDialogHelper;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        EditText usernameInput = findViewById(R.id.registerUsername);
        EditText passwordInput = findViewById(R.id.registerPassword);
        EditText confirmInput = findViewById(R.id.registerConfirmPassword);
        MaterialButton registerBtn = findViewById(R.id.registerBtn);
        TextView gotoLogin = findViewById(R.id.gotoLogin);

        registerBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmInput.getText().toString();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_missing_fields),
                        null
                );
                return;
            }

            if (!password.equals(confirmPassword)) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_password_mismatch),
                        null
                );
                return;
            }

            if (databaseHelper.userExists(username)) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_user_exists),
                        null
                );
                return;
            }

            boolean created = databaseHelper.createUser(username, password, "local");
            if (!created) {
                UiDialogHelper.showStatus(
                        this,
                        UiDialogHelper.Type.ERROR,
                        getString(R.string.status_error),
                        getString(R.string.error_user_exists),
                        null
                );
                return;
            }

            UiDialogHelper.showStatus(
                    this,
                    UiDialogHelper.Type.SUCCESS,
                    getString(R.string.status_success),
                    getString(R.string.register_success),
                    () -> {
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }
            );
        });

        gotoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
