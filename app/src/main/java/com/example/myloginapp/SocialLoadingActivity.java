package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.SessionManager;

public class SocialLoadingActivity extends AppCompatActivity {

    public static final String EXTRA_PROVIDER = "extra_provider";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_loading);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        String provider = getIntent().getStringExtra(EXTRA_PROVIDER);
        if (provider == null || provider.trim().isEmpty()) {
            provider = "facebook";
        }

        TextView loadingText = findViewById(R.id.socialLoadingText);
        String providerLabel = provider.equalsIgnoreCase("linkedin")
            ? getString(R.string.provider_name_linkedin)
            : getString(R.string.provider_name_facebook);
        loadingText.setText(getString(R.string.social_loading_text, providerLabel));

        final String finalProvider = provider;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String username = databaseHelper.getOrCreateSocialUser(finalProvider);
            sessionManager.saveSession(username, finalProvider);

            Intent intent = new Intent(SocialLoadingActivity.this, DashboardActivity.class);
            intent.putExtra(DashboardActivity.EXTRA_USERNAME, username);
            intent.putExtra(DashboardActivity.EXTRA_PROVIDER, finalProvider);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
