// LoadingDialog.java
package com.thesis.dermocura.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.thesis.dermocura.R;

public class LoadingDialog {
    private Dialog dialog;

    public LoadingDialog(Context context) {
        // Create a Dialog with a custom layout
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Hide the dialog title
        View view = LayoutInflater.from(context).inflate(R.layout.loading_overlay, null);
        dialog.setContentView(view);
        dialog.setCancelable(false); // Prevent the dialog from being dismissed by touching outside
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Set transparent background
    }

    // Method to show the loading dialog
    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    // Method to dismiss the loading dialog
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
