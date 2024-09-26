package com.thesis.dermocura.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.thesis.dermocura.R;

public class LoadingPopup {
    private AlertDialog dialog;

    public LoadingPopup(Context context) {
        // Inflate the custom layout for the loading dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.loading_popup, null);

        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setCancelable(false); // Prevent the dialog from being dismissed by the user

        // Create the AlertDialog
        dialog = builder.create();
    }

    // Show the loading dialog
    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    // Dismiss the loading dialog
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
