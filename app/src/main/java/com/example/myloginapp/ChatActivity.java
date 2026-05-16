package com.example.myloginapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.FirebaseCallback;
import com.example.myloginapp.data.Message;
import com.example.myloginapp.data.ProfileImageStore;
import com.example.myloginapp.data.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import android.util.Base64;

public class ChatActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProfileImageStore profileImageStore;
    private String currentUsername;
    private String friendUsername;

    private String currentProfileUrl;
    private String friendProfileUrl;

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
                databaseHelper.addMessage(currentUsername, friendUsername, text, new FirebaseCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        messageInput.setText("");
                    }
                    @Override
                    public void onError(Exception e) {}
                });
            }
        });


        profileImageStore.getProfileImage(currentUsername, new FirebaseCallback<String>() {
            @Override
            public void onSuccess(String url1) {
                currentProfileUrl = url1;
                profileImageStore.getProfileImage(friendUsername, new FirebaseCallback<String>() {
                    @Override
                    public void onSuccess(String url2) {
                        friendProfileUrl = url2;
                        attachChatListener();
                    }
                    @Override
                    public void onError(Exception e) { attachChatListener(); }
                });
            }
            @Override
            public void onError(Exception e) { attachChatListener(); }
        });
    }

    private void attachChatListener() {
        DatabaseReference chatRef = databaseHelper.getMessagesReference(currentUsername, friendUsername);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isDestroyed() || isFinishing()) return;
                messagesContainer.removeAllViews();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    Message msg = msgSnap.getValue(Message.class);
                    if (msg != null) {
                        appendMessage(msg);
                    }
                }
                chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void appendMessage(Message msg) {
        String sender = msg.sender;
        String content = msg.content;

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 8, 0, 8);
        rowLayout.setLayoutParams(rowParams);

        ImageView profileIcon = new ImageView(this);
        String imageUri = sender.equals(currentUsername) ? currentProfileUrl : friendProfileUrl;
        if (imageUri != null && !imageUri.isEmpty()) {
            if (imageUri.startsWith("http")) {
                Glide.with(this).load(imageUri).into(profileIcon);
            } else {
                byte[] decodedString = Base64.decode(imageUri, Base64.DEFAULT);
                Glide.with(this).load(decodedString).into(profileIcon);
            }
        } else {
            profileIcon.setImageResource(R.drawable.ic_baseline_account_circle_24);
        }
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(100, 100);
        iconParams.setMargins(16, 0, 16, 0);
        profileIcon.setLayoutParams(iconParams);
        profileIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
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

            msgView.setBackgroundResource(R.drawable.menu_card_bg);
            rowLayout.setGravity(Gravity.END);
            rowLayout.addView(msgView);
            rowLayout.addView(profileIcon);
        } else {

            msgView.setBackgroundColor(0xFFE0E0E0);
            rowLayout.setGravity(Gravity.START);
            rowLayout.addView(profileIcon);
            rowLayout.addView(msgView);
        }
        messagesContainer.addView(rowLayout);
    }
}
