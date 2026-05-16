package com.example.myloginapp.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import androidx.annotation.NonNull;

public class ProfileImageStore {

    private final DatabaseReference usersRef;
    private final Context context;

    public ProfileImageStore(Context context) {
        this.context = context;
        usersRef = FirebaseDatabase.getInstance("https://epbesocial-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
    }

    public void uploadProfileImage(Uri imageUri, String username, FirebaseCallback<String> callback) {
        try {
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            int MAX_SIZE = 500;
            float ratio = Math.min(
                    (float) MAX_SIZE / selectedImage.getWidth(),
                    (float) MAX_SIZE / selectedImage.getHeight());
            int width = Math.round((float) ratio * selectedImage.getWidth());
            int height = Math.round((float) ratio * selectedImage.getHeight());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedImage, width, height, false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] b = baos.toByteArray();
            String base64Image = Base64.encodeToString(b, Base64.DEFAULT);


            usersRef.child(username).child("profileImageUrl").setValue(base64Image)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(base64Image))
                    .addOnFailureListener(callback::onError);

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public void getProfileImage(String username, FirebaseCallback<String> callback) {
        usersRef.child(username).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onSuccess(snapshot.getValue(String.class));
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }
}
