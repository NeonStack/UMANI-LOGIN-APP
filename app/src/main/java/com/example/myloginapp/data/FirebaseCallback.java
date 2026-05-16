package com.example.myloginapp.data;

public interface FirebaseCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
