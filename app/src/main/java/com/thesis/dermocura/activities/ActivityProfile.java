package com.thesis.dermocura.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityProfile extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhoneNumber;
    private ImageView circularImageView;
    private Button btnLogout;
    private static final String TAG = "ActivityProfile";
    private MySharedPreferences prefs;
    private UserData userData;
    private static final String UPDATE_URL = "https://backend.dermocura.net/android/update_user.php"; // Your update URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        circularImageView = findViewById(R.id.circularImageView);
        btnLogout = findViewById(R.id.btnContinue); // Assuming you want to use this button for logout

        // Retrieve UserData from MySharedPreferences
        prefs = MySharedPreferences.getInstance(this);
        userData = prefs.getUserData();

        if (userData != null) {
            // Set data to the Views
            tvName.setText(userData.getPatientName());
            tvEmail.setText(userData.getPatientEmail());
            tvPhoneNumber.setText(userData.getPatientMobileNumber());

            // Load the profile image using Glide
            Glide.with(this)
                    .load(userData.getPatientImageURL())
                    .placeholder(R.drawable.default_placeholder) // Placeholder image
                    .error(R.drawable.default_placeholder) // Error image
                    .into(circularImageView);
        } else {
            Log.e(TAG, "UserData is null. Unable to retrieve user information.");
        }

        // Set click listeners to allow editing
        tvName.setOnClickListener(v -> showEditDialog(tvName, "Edit Name", "patientName", userData.getPatientName()));
        tvEmail.setOnClickListener(v -> showEditDialog(tvEmail, "Edit Email", "patientEmail", userData.getPatientEmail()));
        tvPhoneNumber.setOnClickListener(v -> showEditDialog(tvPhoneNumber, "Edit Phone Number", "patientMobileNumber", userData.getPatientMobileNumber()));

        // Logout functionality
        btnLogout.setOnClickListener(v -> {
            prefs.clearUserData();
            Intent intent = new Intent(ActivityProfile.this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Method to show the dialog and update both the UI and the SharedPreferences
    private void showEditDialog(TextView textView, String title, String field, String oldValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_text, null);
        builder.setView(dialogView);

        // Set dialog title
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText(title);

        // Get the EditText input and set its current value
        EditText editTextInput = dialogView.findViewById(R.id.editTextInput);
        editTextInput.setText(textView.getText().toString());

        // Get the dialog buttons
        Button btnOk = dialogView.findViewById(R.id.btnOk);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        // Set up the OK button
        btnOk.setOnClickListener(v -> {
            String newText = editTextInput.getText().toString().trim();
            if (!newText.isEmpty()) {
                // Update the TextView with new value
                textView.setText(newText);

                // Update the value in SharedPreferences
                updateUserDataInSharedPreferences(field, newText);

                // Update the value in the database (send old value and new value)
                updateUserDataInDatabase(field, oldValue, newText);

                dialog.dismiss();
            } else {
                editTextInput.setError("Input cannot be empty!");
            }
        });

        // Set up the Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Method to update the SharedPreferences
    private void updateUserDataInSharedPreferences(String field, String newValue) {
        if (userData == null) {
            Log.e(TAG, "UserData is null. Cannot update SharedPreferences.");
            return;
        }

        switch (field) {
            case "patientName":
                userData.setPatientName(newValue);
                break;
            case "patientEmail":
                userData.setPatientEmail(newValue);
                break;
            case "patientMobileNumber":
                userData.setPatientMobileNumber(newValue);
                break;
            default:
                break;
        }

        // Save the updated UserData object
        prefs.saveUserData(userData);
    }

    // Method to update user data in the database
    private void updateUserDataInDatabase(String field, String oldValue, String newValue) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the request body with patientID, field, oldValue, and newValue
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", userData.getPatientID()); // Patient ID
            requestBody.put("field", field);                      // Field to update (e.g., "patientName")
            requestBody.put("oldValue", oldValue);                // Old value to verify
            requestBody.put("newValue", newValue);                // New value to update

            // Log for debugging purposes
            Log.d(TAG, "Field: " + field + " Old Value: " + oldValue + " New Value: " + newValue);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON request body: " + e.getMessage());
        }

        // Create a request to update the server
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                UPDATE_URL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Log.d(TAG, "Database successfully updated for field: " + field);
                        } else {
                            String message = response.getString("message");
                            Log.e(TAG, "Failed to update the database: " + message);
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Network error: " + error.getMessage())
        );

        // Add the request to the Volley queue
        queue.add(request);
    }
}