package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.ProfileImageStore;
import com.example.myloginapp.data.SessionManager;
import com.example.myloginapp.ui.UiDialogHelper;

public class DashboardActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_PROVIDER = "extra_provider";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProfileImageStore profileImageStore;
    private String currentUsername;
    private String currentProvider;
    private ImageView profileImageView;

    private static final int REQUEST_PROFILE_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        profileImageStore = new ProfileImageStore(this);

        currentUsername = getIntent().getStringExtra(EXTRA_USERNAME);
        currentProvider = getIntent().getStringExtra(EXTRA_PROVIDER);

        if (currentUsername == null) {
            currentUsername = sessionManager.getUsername();
        }
        if (currentProvider == null) {
            currentProvider = sessionManager.getProvider();
        }

        if (currentUsername == null) {
            goToLogin();
            return;
        }

        TextView welcomeText = findViewById(R.id.welcomeText);
        TextView providerText = findViewById(R.id.providerText);
        profileImageView = findViewById(R.id.profileImageView);
        View uploadProfileBtn = findViewById(R.id.uploadProfileBtn);
        View changePasswordBtn = findViewById(R.id.changePasswordBtn);
        View deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        View logoutBtn = findViewById(R.id.logoutBtn);

        welcomeText.setText(getString(R.string.welcome_label, currentUsername));
        providerText.setText(getString(R.string.provider_label, currentProvider));
        loadProfileImage();

        boolean isLocalUser = "local".equalsIgnoreCase(currentProvider);
        changePasswordBtn.setVisibility(isLocalUser ? View.VISIBLE : View.GONE);

        uploadProfileBtn.setOnClickListener(v -> openImagePicker());

        changePasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ChangePasswordActivity.class);
            intent.putExtra(ChangePasswordActivity.EXTRA_USERNAME, currentUsername);
            intent.putExtra(ChangePasswordActivity.EXTRA_PROVIDER, currentProvider);
            startActivity(intent);
        });

        deleteAccountBtn.setOnClickListener(v -> {
            UiDialogHelper.showConfirm(
                    this,
                    getString(R.string.delete_confirm_title),
                    getString(R.string.delete_confirm_message),
                    () -> {
                boolean deleted = databaseHelper.deleteUser(currentUsername);
                if (deleted) {
                    profileImageStore.clearProfileImage(currentUsername);
                    sessionManager.clearSession();
                    UiDialogHelper.showStatus(
                            this,
                            UiDialogHelper.Type.SUCCESS,
                            getString(R.string.status_success),
                            getString(R.string.account_deleted),
                            this::goToLogin
                    );
                }
                    }
            );
        });

        logoutBtn.setOnClickListener(v -> {
            sessionManager.clearSession();
            goToLogin();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PROFILE_IMAGE);
    }

    private void loadProfileImage() {
        String savedUri = profileImageStore.getProfileImage(currentUsername);
        if (savedUri == null) {
            profileImageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
            return;
        }
        profileImageView.setImageURI(Uri.parse(savedUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PROFILE_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) {
            return;
        }

        Uri imageUri = data.getData();
        final int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(imageUri, flags);
        } catch (SecurityException ignored) {
            // Persisted permission is best-effort; image can still work for current session.
        }

        profileImageStore.saveProfileImage(currentUsername, imageUri.toString());
        profileImageView.setImageURI(imageUri);
    }

    private void goToLogin() {
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
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
