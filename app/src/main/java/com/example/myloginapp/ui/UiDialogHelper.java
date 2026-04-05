package com.example.myloginapp.ui;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.example.myloginapp.R;

public class UiDialogHelper {

    public enum Type {
        ERROR,
        SUCCESS,
        INFO
    }

    public static void showStatus(
            AppCompatActivity activity,
            Type type,
            String title,
            String message,
            @Nullable Runnable onOk
    ) {
        Dialog dialog = new Dialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_status, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View header = view.findViewById(R.id.dialogHeader);
        TextView titleView = view.findViewById(R.id.dialogTitle);
        TextView messageView = view.findViewById(R.id.dialogMessage);
        MaterialButton okButton = view.findViewById(R.id.dialogOkBtn);

        int headerColor;
        if (type == Type.ERROR) {
            headerColor = activity.getColor(R.color.modal_error);
        } else if (type == Type.SUCCESS) {
            headerColor = activity.getColor(R.color.modal_success);
        } else {
            headerColor = activity.getColor(R.color.modal_info);
        }

        header.setBackgroundColor(headerColor);
        titleView.setText(title);
        messageView.setText(message);

        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (onOk != null) {
                onOk.run();
            }
        });

        dialog.show();
    }

    public static void showConfirm(
            AppCompatActivity activity,
            String title,
            String message,
            Runnable onConfirm
    ) {
        Dialog dialog = new Dialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView titleView = view.findViewById(R.id.confirmTitle);
        TextView messageView = view.findViewById(R.id.confirmMessage);
        MaterialButton cancelButton = view.findViewById(R.id.confirmCancelBtn);
        MaterialButton confirmButton = view.findViewById(R.id.confirmActionBtn);

        titleView.setText(title);
        messageView.setText(message);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            onConfirm.run();
        });

        dialog.show();
    }
}
