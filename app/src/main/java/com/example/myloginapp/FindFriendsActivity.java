package com.example.myloginapp;

import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myloginapp.data.DatabaseHelper;
import com.example.myloginapp.data.ProfileImageStore;
import com.example.myloginapp.data.SessionManager;

import java.util.List;

public class FindFriendsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProfileImageStore profileImageStore;
    private String currentUsername;
    
    private List<String> potentialFriends;
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
        profileImageStore = new ProfileImageStore(this);
        currentUsername = sessionManager.getUsername();

        cardContainer = findViewById(R.id.cardContainer);
        cardImage = findViewById(R.id.cardImage);
        cardName = findViewById(R.id.cardName);
        noUsersText = findViewById(R.id.noUsersText);

        findViewById(R.id.btnPass).setOnClickListener(v -> nextCard());
        findViewById(R.id.btnLike).setOnClickListener(v -> {
            if (currentIndex < potentialFriends.size()) {
                String likedUser = potentialFriends.get(currentIndex);
                databaseHelper.addFriend(currentUsername, likedUser);
                Toast.makeText(this, "Liked " + likedUser + "!", Toast.LENGTH_SHORT).show();
                nextCard();
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        potentialFriends = databaseHelper.getAllOtherUsers(currentUsername);
        currentIndex = 0;
        showCurrentCard();
    }

    private void showCurrentCard() {
        if (currentIndex < potentialFriends.size()) {
            String user = potentialFriends.get(currentIndex);
            cardName.setText(user);
            
            String imageUri = profileImageStore.getProfileImage(user);
            if (imageUri != null) {
                cardImage.setImageURI(Uri.parse(imageUri));
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

    private void nextCard() {
        currentIndex++;
        showCurrentCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }
}
