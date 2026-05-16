package com.example.myloginapp.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private final DatabaseReference usersRef;
    private final DatabaseReference friendsRef;
    private final DatabaseReference messagesRef;

    public DatabaseHelper(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://epbesocial-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("users");
        friendsRef = database.getReference("friends");
        messagesRef = database.getReference("messages");
    }

    public void createUser(String username, String password, String provider, FirebaseCallback<Boolean> callback) {
        userExists(username, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (exists) {
                    callback.onSuccess(false);
                } else {
                    User user = new User(username, password, provider, System.currentTimeMillis());
                    usersRef.child(username).setValue(user)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                            .addOnFailureListener(callback::onError);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void userExists(String username, FirebaseCallback<Boolean> callback) {
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onSuccess(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void verifyUserCredentials(String username, String password, FirebaseCallback<Boolean> callback) {
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.password.equals(password) && user.isDeleted == 0) {
                        callback.onSuccess(true);
                        return;
                    }
                }
                callback.onSuccess(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void getProviderForUser(String username, FirebaseCallback<String> callback) {
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    callback.onSuccess(user != null ? user.provider : "local");
                } else {
                    callback.onSuccess("local");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void updatePassword(String username, String currentPassword, String newPassword, FirebaseCallback<Boolean> callback) {
        verifyUserCredentials(username, currentPassword, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isValid) {
                if (isValid) {
                    usersRef.child(username).child("password").setValue(newPassword)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                            .addOnFailureListener(callback::onError);
                } else {
                    callback.onSuccess(false);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void deleteUser(String username, FirebaseCallback<Boolean> callback) {
        usersRef.child(username).child("isDeleted").setValue(1)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onError);
    }

    public void getOrCreateSocialUser(String provider, FirebaseCallback<String> callback) {
        String username = provider + "_user";
        userExists(username, new FirebaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (!exists) {
                    createUser(username, "social-login", provider, new FirebaseCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            callback.onSuccess(username);
                        }

                        @Override
                        public void onError(Exception e) {
                            callback.onError(e);
                        }
                    });
                } else {
                    callback.onSuccess(username);
                }
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }



    public void getAllOtherUsers(String currentUsername, FirebaseCallback<List<User>> callback) {
        getFriends(currentUsername, new FirebaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> friends) {
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> otherUsers = new ArrayList<>();
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            User u = userSnap.getValue(User.class);
                            if (u != null && !u.username.equals(currentUsername) && u.isDeleted == 0 && !friends.contains(u.username)) {
                                otherUsers.add(u);
                            }
                        }
                        callback.onSuccess(otherUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.toException());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void addFriend(String currentUsername, String friendUsername, FirebaseCallback<Boolean> callback) {
        friendsRef.child(currentUsername).child(friendUsername).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onError);
    }

    public void getFriends(String currentUsername, FirebaseCallback<List<String>> callback) {
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> friends = new ArrayList<>();

                if (snapshot.hasChild(currentUsername)) {
                    for (DataSnapshot friendSnap : snapshot.child(currentUsername).getChildren()) {
                        friends.add(friendSnap.getKey());
                    }
                }

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    if (userSnap.hasChild(currentUsername) && !friends.contains(userSnap.getKey())) {
                        friends.add(userSnap.getKey());
                    }
                }
                callback.onSuccess(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public String getChatId(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }

    public void addMessage(String sender, String receiver, String content, FirebaseCallback<Boolean> callback) {
        String chatId = getChatId(sender, receiver);
        Message message = new Message(sender, receiver, content, System.currentTimeMillis());
        messagesRef.child(chatId).push().setValue(message)
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(receiver).child("lastMessageNotification").setValue(sender + ":" + System.currentTimeMillis());
                    callback.onSuccess(true);
                })
                .addOnFailureListener(callback::onError);
    }

    public void listenForNotifications(String currentUsername, FirebaseCallback<String> callback) {
        usersRef.child(currentUsername).child("lastMessageNotification").addValueEventListener(new ValueEventListener() {
            boolean isFirstLoad = true;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }
                if (snapshot.exists()) {
                    String val = snapshot.getValue(String.class);
                    if (val != null && val.contains(":")) {
                        callback.onSuccess(val.split(":")[0]);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public DatabaseReference getMessagesReference(String user1, String user2) {
        return messagesRef.child(getChatId(user1, user2));
    }
    public void updateUserProfileImage(String username, String url, FirebaseCallback<Boolean> callback) {
        usersRef.child(username).child("profileImageUrl").setValue(url)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onError);
    }
}
