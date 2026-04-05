package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.SessionManager;
import com.example.myloginapp.ui.UiDialogHelper;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            openDashboard(sessionManager.getUsername(), sessionManager.getProvider());
            finish();
            return;
        }

        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        MaterialButton loginbtn = findViewById(R.id.loginbtn);
        TextView registerNow = findViewById(R.id.registerNow);
        ImageView fbButton = findViewById(R.id.fbButton);
        ImageView linkedinButton = findViewById(R.id.linkedinButton);

        loginbtn.setOnClickListener(v -> attemptLogin());

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isDoneAction = actionId == EditorInfo.IME_ACTION_DONE;
            boolean isEnterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (isDoneAction || isEnterKey) {
                attemptLogin();
                return true;
            }
            return false;
        });

        registerNow.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        fbButton.setOnClickListener(v -> openSocialLoading("facebook"));
        linkedinButton.setOnClickListener(v -> openSocialLoading("linkedin"));
    }

    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            UiDialogHelper.showStatus(
                    this,
                    UiDialogHelper.Type.ERROR,
                    getString(R.string.status_error),
                    getString(R.string.error_missing_fields),
                    null
            );
            return;
        }

        boolean isValidUser = databaseHelper.verifyUserCredentials(username, password);
        if (!isValidUser) {
            UiDialogHelper.showStatus(
                    this,
                    UiDialogHelper.Type.ERROR,
                    getString(R.string.status_error),
                    getString(R.string.error_login_failed),
                    null
            );
            return;
        }

        String provider = databaseHelper.getProviderForUser(username);
        sessionManager.saveSession(username, provider);
        openDashboard(username, provider);
        finish();
    }

    private void openSocialLoading(String provider) {
        Intent intent = new Intent(MainActivity.this, SocialLoadingActivity.class);
        intent.putExtra(SocialLoadingActivity.EXTRA_PROVIDER, provider);
        startActivity(intent);
    }

    private void openDashboard(String username, String provider) {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra(DashboardActivity.EXTRA_USERNAME, username);
        intent.putExtra(DashboardActivity.EXTRA_PROVIDER, provider);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}