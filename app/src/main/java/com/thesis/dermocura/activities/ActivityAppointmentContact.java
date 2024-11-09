package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityAppointmentContact extends AppCompatActivity {

    private static final String TAG = "ActivityAppointmentContact";
    private static final String URL_SUBMIT_APPOINTMENT = "https://backend.dermocura.net/android/appointment/setappointment.php";

    private EditText etAdditionalInput;
    private MaterialButton btnSubmitContact;

    private int docAvailID;
    private String startTime, endTime;

    private LoadingDialogFragment loadingDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_contact);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadingDialogFragment = new LoadingDialogFragment();

        // Retrieve passed data
        Intent intent = getIntent();
        docAvailID = intent.getIntExtra("docAvailID", -1);
        startTime = intent.getStringExtra("startTime");
        endTime = intent.getStringExtra("endTime");

        // Initialize views
        etAdditionalInput = findViewById(R.id.etAdditionalInput);
        btnSubmitContact = findViewById(R.id.btnSubmitContact);

        // Handle form submission
        btnSubmitContact.setOnClickListener(v -> {
            String additionalInput = etAdditionalInput.getText().toString().trim();
            MySharedPreferences prefs = MySharedPreferences.getInstance(this);
            UserData userData = prefs.getUserData();

            // Check if userData is not null before accessing the phone number
            if (userData != null) {
                String phoneNumber = userData.getPatientMobileNumber();

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    // Use the phone number as needed
                    showLoadingDialog();
                    submitAppointment(phoneNumber, additionalInput);
                } else {
                    Log.d("Activity", "Phone number is not available.");
                }
            } else {
                Log.e("Activity", "User data not found.");
            }
        });
    }

    // Method to submit the appointment
    private void submitAppointment(String phoneNumber, String additionalInput) {
        // Get patient ID from shared preferences
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        int patientID = prefs.getUserData().getPatientID();

        // Prepare request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", patientID);
            requestBody.put("docAvailabilityID", docAvailID);
            requestBody.put("start_time", startTime);
            requestBody.put("end_time", endTime);
            requestBody.put("patientContact", phoneNumber); // Add phone number
            requestBody.put("patientAdditionalInput", additionalInput); // Add additional input
            requestBody.put("status", "Pending");
            requestBody.put("notif_active", "active");
            requestBody.put("notif_count", 1);

            Log.d(TAG, "Submitting appointment: " + requestBody.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
        }

        // Submit using Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_SUBMIT_APPOINTMENT,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Toast.makeText(ActivityAppointmentContact.this, "Appointment registered successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ActivityAppointmentContact.this, ActivityAppointmentList.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(ActivityAppointmentContact.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            dismissLoadingDialog();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        dismissLoadingDialog();
                    }
                },
                error -> {
                    Log.e(TAG, "Error submitting appointment: " + error.getMessage());
                    Toast.makeText(ActivityAppointmentContact.this, "Failed to submit appointment", Toast.LENGTH_SHORT).show();
                    dismissLoadingDialog();
                }
        );
        queue.add(request);
    }

    // Show the loading dialog
    private void showLoadingDialog() {
        if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
        }
    }

    // Dismiss the loading dialog
    private void dismissLoadingDialog() {
        if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
            loadingDialogFragment.dismiss();
        }
    }
}
