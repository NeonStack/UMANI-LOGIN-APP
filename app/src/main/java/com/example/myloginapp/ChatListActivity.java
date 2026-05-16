package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.FirebaseCallback;
import com.example.myloginapp.data.SessionManager;

import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUsername = sessionManager.getUsername();

        LinearLayout container = findViewById(R.id.friendsListContainer);
        databaseHelper.getFriends(currentUsername, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> friends) {
                if (friends.isEmpty()) {
                    TextView emptyText = new TextView(ChatListActivity.this);
                    emptyText.setText("You haven't liked anyone yet. Go to Find Friends!");
                    emptyText.setTextSize(16f);
                    emptyText.setTextColor(android.graphics.Color.parseColor("#1F2937"));
                    container.addView(emptyText);
                    return;
                }

                for (String friend : friends) {
                    TextView friendView = new TextView(ChatListActivity.this);
                    friendView.setText(friend);
                    friendView.setTextSize(20f);
                    friendView.setTextColor(android.graphics.Color.parseColor("#1F2937"));
                    friendView.setPadding(32, 48, 32, 48);
                    friendView.setBackgroundResource(R.drawable.menu_card_bg);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 24);
                    friendView.setLayoutParams(params);
                    friendView.setOnClickListener(v -> {
                        Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                        intent.putExtra("FRIEND_USERNAME", friend);
                        startActivity(intent);
                    });

                    container.addView(friendView);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
