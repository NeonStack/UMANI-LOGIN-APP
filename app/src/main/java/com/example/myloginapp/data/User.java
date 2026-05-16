package com.example.myloginapp.data;

public class User {
    public String username;
    public String password;
    public String provider;
    public long createdAt;
    public int isDeleted;
    public String profileImageUrl;


    public User() {
    }

    public User(String username, String password, String provider, long createdAt) {
        this.username = username;
        this.password = password;
        this.provider = provider;
        this.createdAt = createdAt;
        this.isDeleted = 0;
        this.profileImageUrl = "";
    }
}
