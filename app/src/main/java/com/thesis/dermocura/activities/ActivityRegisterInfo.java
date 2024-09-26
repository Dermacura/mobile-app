package com.thesis.dermocura.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.datas.RegistrationData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class ActivityRegisterInfo extends AppCompatActivity {

    // Declare input fields
    private EditText etFullName, etPhoneNumber, etGender, etAge;
    private MaterialButton btnContinue, btnSelectBirthday;
    private LoadingDialogFragment loadingDialogFragment;

    String TAG = "ActivityRegisterInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize input fields and loading dialog
        initializeViews();
        loadingDialogFragment = new LoadingDialogFragment();

        // Set click listener for the continue button
        btnContinue.setOnClickListener(v -> saveInputData());

        // Set click listener for the birthday button
        btnSelectBirthday.setOnClickListener(v -> openDatePicker());
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etGender = findViewById(R.id.etGender);
        etAge = findViewById(R.id.etAge);
        btnContinue = findViewById(R.id.btnContinue);
        btnSelectBirthday = findViewById(R.id.btnSelectBirthday);
    }

    // Function to open the date picker dialog
    private void openDatePicker() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date to YYYY-MM-DD
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    btnSelectBirthday.setText(formattedDate);

                    // Save the selected date to RegistrationData
                    RegistrationData.getInstance().setPatientBirthDate(formattedDate); // Save as string formatted in YYYY-MM-DD

                    // Calculate age and set it to the age EditText
                    calculateAge(selectedYear, selectedMonth, selectedDay);
                },
                year, month, day
        );

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        etAge.setText(String.valueOf(age)); // Set the calculated age in the age EditText
    }

    private void saveInputData() {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String ageText = etAge.getText().toString().trim();

        // Validate that the full name is not empty
        if (fullName.isEmpty()) {
            etFullName.setError("Full Name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        // Convert age to integer, handling potential errors
        Integer age = null;
        try {
            age = ageText.isEmpty() ? null : Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid age input: " + e.getMessage());
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save input data to the RegistrationData singleton
        RegistrationData registrationData = RegistrationData.getInstance();
        registrationData.setPatientName(fullName);
        registrationData.setPatientMobileNumber(phoneNumber.isEmpty() ? null : phoneNumber);
        registrationData.setPatientGender(gender.isEmpty() ? null : gender);
        registrationData.setPatientAge(age);

        // Log saved data for debugging
        Log.d(TAG, "Data Saved: " +
                "\nName: " + registrationData.getPatientName() +
                "\nPhone: " + registrationData.getPatientMobileNumber() +
                "\nGender: " + registrationData.getPatientGender() +
                "\nAge: " + registrationData.getPatientAge() +
                "\nBirthday: " + registrationData.getPatientBirthDate());

        // Show confirmation message
        Toast.makeText(this, "Profile information saved successfully.", Toast.LENGTH_SHORT).show();

        makeHTTPRequest();
    }

    // Function to make an HTTP request using Volley
    private void makeHTTPRequest() {
        // Show loading dialog
        if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
        }

        // Get an instance of RegistrationData
        RegistrationData registrationData = RegistrationData.getInstance();

        // Define keys for the JSON request body as per your PHP script
        String keyName = "patientName";
        String keyPassword = "patientPassword";
        String keyEmail = "patientEmail";
        String keyMobileNumber = "patientMobileNumber";
        String keyGender = "patientGender";
        String keyAge = "patientAge";
        String keyBirthDate = "patientBirthDate";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Populate the JSON request body with data from RegistrationData
        try {
            requestBody.put(keyName, registrationData.getPatientName());
            requestBody.put(keyPassword, registrationData.getPatientPassword());
            requestBody.put(keyEmail, registrationData.getPatientEmail());
            requestBody.put(keyMobileNumber, registrationData.getPatientMobileNumber());
            requestBody.put(keyGender, registrationData.getPatientGender());
            requestBody.put(keyAge, registrationData.getPatientAge());
            requestBody.put(keyBirthDate, registrationData.getPatientBirthDate()); // Send as string formatted in YYYY-MM-DD
        } catch (JSONException e) {
            Log.e(TAG + " makeHTTPRequest", String.valueOf(e));
            dismissLoadingDialog();
            return;
        }

        // Define your backend URL where the PHP script is hosted
        String URL = "https://backend.dermocura.net/android/authentication/registerinfo.php";

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        // Log the JSON request body for debugging
        String stringJSON = requestBody.toString();
        Log.i(TAG + " makeHTTPRequest", stringJSON);

        // Add the request to the Volley request queue
        queue.add(request);
    }

    // Callback function for a successful request
    private void onRequestSuccess(JSONObject response) {
        dismissLoadingDialog();
        try {
            // Extract success status and message from the JSON response
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                // Registration successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                showAlertDialog("Registration Successful", message, true);
            } else {
                // Registration failed
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                showAlertDialog("Registration Failed", message, false);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
            showAlertDialog("Error", "Error parsing the response.", false);
        }
    }

    // Callback function for a failed request
    private void onRequestError(VolleyError error) {
        dismissLoadingDialog();
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
        showAlertDialog("Request Error", "Error: " + error.getMessage(), false);
    }

    private void dismissLoadingDialog() {
        if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
            loadingDialogFragment.dismiss();
        }
    }

    private void showAlertDialog(String title, String message, boolean success) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (success) {
                        // If registration is successful, finish all activities and open login activity
                        Intent intent = new Intent(ActivityRegisterInfo.this, ActivityLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}
