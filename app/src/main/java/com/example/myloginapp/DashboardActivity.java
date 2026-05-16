package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.FirebaseCallback;
import com.example.myloginapp.data.ProfileImageStore;
import com.example.myloginapp.data.SessionManager;
import com.example.myloginapp.ui.UiDialogHelper;
import com.bumptech.glide.Glide;
import android.util.Base64;

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
        View findFriendsBtn = findViewById(R.id.findFriendsBtn);
        View messagesBtn = findViewById(R.id.messagesBtn);
        View settingsBtn = findViewById(R.id.settingsBtn);
        View faqsBtn = findViewById(R.id.faqsBtn);
        View logoutBtn = findViewById(R.id.logoutBtn);

        welcomeText.setText(getString(R.string.welcome_label, currentUsername));
        providerText.setText(getString(R.string.provider_label, currentProvider));

        settingsBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
        });

        findFriendsBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, FindFriendsActivity.class));
        });

        messagesBtn.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ChatListActivity.class));
        });

        faqsBtn.setOnClickListener(v -> {
            UiDialogHelper.showFaqs(this);
        });

        logoutBtn.setOnClickListener(v -> {
            sessionManager.clearSession();
            goToLogin();
        });


        databaseHelper.listenForNotifications(currentUsername, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String sender) {
                Toast.makeText(DashboardActivity.this, "New message from " + sender + "!", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
    }

    private void loadProfileImage() {
        profileImageStore.getProfileImage(currentUsername, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String url) {
                if (isDestroyed() || isFinishing()) return;
                if (url == null || url.isEmpty()) {
                    profileImageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
                } else {
                    if (url.startsWith("http")) {
                        Glide.with(DashboardActivity.this).load(url).into(profileImageView);
                    } else {
                        byte[] decodedString = Base64.decode(url, Base64.DEFAULT);
                        Glide.with(DashboardActivity.this).load(decodedString).into(profileImageView);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                profileImageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
            }
        });
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

    }
}
