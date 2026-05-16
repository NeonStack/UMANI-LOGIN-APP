package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.ProfileImageStore;
import com.example.myloginapp.data.SessionManager;

public class ChatActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProfileImageStore profileImageStore;
    private String currentUsername;
    private String friendUsername;

    private LinearLayout messagesContainer;
    private ScrollView chatScrollView;
    private EditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        profileImageStore = new ProfileImageStore(this);
        currentUsername = sessionManager.getUsername();
        friendUsername = getIntent().getStringExtra("FRIEND_USERNAME");

        TextView chatTitle = findViewById(R.id.chatTitle);
        chatTitle.setText("Chat with " + friendUsername);

        messagesContainer = findViewById(R.id.messagesContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        messageInput = findViewById(R.id.messageInput);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                databaseHelper.addMessage(currentUsername, friendUsername, text);
                messageInput.setText("");
                loadMessages();
            }
        });

        loadMessages();
    }

    private void loadMessages() {
        messagesContainer.removeAllViews();
        Cursor cursor = databaseHelper.getMessages(currentUsername, friendUsername);
        
        int senderIndex = cursor.getColumnIndex("sender");
        int contentIndex = cursor.getColumnIndex("content");

        while (cursor.moveToNext()) {
            String sender = cursor.getString(senderIndex);
            String content = cursor.getString(contentIndex);

            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 8, 0, 8);
            rowLayout.setLayoutParams(rowParams);

            ImageView profileIcon = new ImageView(this);
            String imageUri = profileImageStore.getProfileImage(sender);
            if (imageUri != null) {
                profileIcon.setImageURI(android.net.Uri.parse(imageUri));
            } else {
                profileIcon.setImageResource(R.drawable.ic_baseline_account_circle_24);
            }
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(100, 100);
            iconParams.setMargins(16, 0, 16, 0);
            profileIcon.setLayoutParams(iconParams);
            profileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // Optionally add a background circle like in dashboard
            profileIcon.setBackgroundResource(R.drawable.profile_image_circle);
            profileIcon.setClipToOutline(true);

            TextView msgView = new TextView(this);
            msgView.setText(content);
            msgView.setTextSize(16f);
            msgView.setTextColor(android.graphics.Color.parseColor("#1F2937"));
            msgView.setPadding(32, 24, 32, 24);
            
            LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            msgView.setLayoutParams(msgParams);

            if (sender.equals(currentUsername)) {
                // Sent message (Right side)
                msgView.setBackgroundResource(R.drawable.menu_card_bg);
                rowLayout.setGravity(Gravity.END);
                rowLayout.addView(msgView);
                rowLayout.addView(profileIcon);
            } else {
                // Received message (Left side)
                msgView.setBackgroundColor(0xFFE0E0E0); // Light grey
                rowLayout.setGravity(Gravity.START);
                rowLayout.addView(profileIcon);
                rowLayout.addView(msgView);
            }
            
            messagesContainer.addView(rowLayout);
        }
        cursor.close();
        
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }
}
