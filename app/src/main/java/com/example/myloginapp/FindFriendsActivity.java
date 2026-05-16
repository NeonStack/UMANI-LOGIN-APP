package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.FirebaseCallback;
import com.example.myloginapp.data.SessionManager;
import com.example.myloginapp.data.User;
import com.bumptech.glide.Glide;
import android.util.Base64;

import java.util.List;

public class FindFriendsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private String currentUsername;
    
    private List<User> potentialFriends;
    private int currentIndex = 0;

    private View cardContainer;
    private ImageView cardImage;
    private TextView cardName;
    private TextView noUsersText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUsername = sessionManager.getUsername();

        cardContainer = findViewById(R.id.cardContainer);
        cardImage = findViewById(R.id.cardImage);
        cardName = findViewById(R.id.cardName);
        noUsersText = findViewById(R.id.noUsersText);

        findViewById(R.id.btnPass).setOnClickListener(v -> nextCard(false));
        findViewById(R.id.btnLike).setOnClickListener(v -> {
            if (potentialFriends != null && currentIndex < potentialFriends.size()) {
                User likedUser = potentialFriends.get(currentIndex);
                databaseHelper.addFriend(currentUsername, likedUser.username, new FirebaseCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        Toast.makeText(FindFriendsActivity.this, "Liked " + likedUser.username + "!", Toast.LENGTH_SHORT).show();
                        nextCard(true);
                    }
                    @Override
                    public void onError(Exception e) {}
                });
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        databaseHelper.getAllOtherUsers(currentUsername, new FirebaseCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (isDestroyed() || isFinishing()) return;
                potentialFriends = users;
                currentIndex = 0;
                showCurrentCard();
            }

            @Override
            public void onError(Exception e) {
                // handle error
            }
        });
    }

    private void showCurrentCard() {
        cardContainer.setTranslationX(0f);
        cardContainer.setAlpha(1f);

        if (potentialFriends != null && currentIndex < potentialFriends.size()) {
            User user = potentialFriends.get(currentIndex);
            cardName.setText(user.username);
            
            if (user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
                if (user.profileImageUrl.startsWith("http")) {
                    Glide.with(this).load(user.profileImageUrl).into(cardImage);
                } else {
                    byte[] decodedString = Base64.decode(user.profileImageUrl, Base64.DEFAULT);
                    Glide.with(this).load(decodedString).into(cardImage);
                }
            } else {
                cardImage.setImageResource(R.drawable.ic_baseline_account_circle_24);
            }
            
            cardContainer.setVisibility(View.VISIBLE);
            noUsersText.setVisibility(View.GONE);
        } else {
            cardContainer.setVisibility(View.GONE);
            noUsersText.setVisibility(View.VISIBLE);
        }
    }

    private void nextCard(boolean isLike) {
        if (potentialFriends != null && currentIndex < potentialFriends.size()) {
            float targetX = isLike ? 1000f : -1000f;
            cardContainer.animate()
                    .translationX(targetX)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        currentIndex++;
                        showCurrentCard();
                    })
                    .start();
        } else {
            currentIndex++;
            showCurrentCard();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
