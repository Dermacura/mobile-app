package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.*;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityLogin extends AppCompatActivity {

    // THIS ACTIVITY CLASS IS DONE AND CHECKED AT 1:46PM 8/6/2024
    // MAINTAINABILITY: CHECK
    // SCALABILITY: CHECK
    // READABILITY: CHECK

    // Declare Views
    TextView tvTitle, tvSubTitle, tvForgotPassword, tvRegister;
    EditText etEmailAddress, etPassword;
    ImageView ivEmailAddress, ivPassword;
    LinearLayout llEmailAddress, llPassword;
    MaterialButton btnContinue;

    // Declare Strings
    private static final String TAG = "ActivityLogin";
    private static final String URL = "https://backend.dermocura.net/android/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Methods
        initializeObjects();
        setOnClickListeners();
    }

    private void initializeObjects() {
        // Text Views
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvForgotPassword = findViewById(R.id.tvInformation);
        tvRegister = findViewById(R.id.tvRegister);
        // Edit Text
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        // Image Views
        ivEmailAddress = findViewById(R.id.ivEmailAddress);
        ivPassword = findViewById(R.id.ivPassword);
        // Linear Layout
        llEmailAddress = findViewById(R.id.llEmailAddress);
        llPassword = findViewById(R.id.llPassword);
        // Material Buttons
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material buttons
        btnContinue.setOnClickListener(v -> clickContinue());
        // Clickable Text Views
        tvForgotPassword.setOnClickListener(v -> clickForgotPassword());
        tvRegister.setOnClickListener(v -> clickNewAccount());
    }

    private void clickForgotPassword() {
        // Create an intent to start the PasswordReset activity
        Intent intentPasswordRecovery = new Intent(ActivityLogin.this, ActivityPasswordRecovery.class);

        // Start the PasswordReset activity
        startActivity(intentPasswordRecovery);
    }

    private void clickNewAccount() {
        // Create an intent to start the Register activity
        Intent intentRegister = new Intent(ActivityLogin.this, ActivityRegister.class);

        // Start the Register activity
        startActivity(intentRegister);
    }

    private void clickContinue() {
        // Retrieve user input from the input fields
        String email = etEmailAddress.getText().toString();
        String password = etPassword.getText().toString();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input email: " + email);
        Log.i(TAG + " clickContinue", "User Input password: " + password);

        // Validate user input
        if (validateInputs(email, password)) {
            makeHTTPRequest(email, password);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
        }
    }

    private boolean validateInputs(String email, String password) {
        // Validate email field - cannot be empty and must be a valid email format
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e(TAG + " validateInputs", "Email is either empty or not an email!");
            llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate password field - cannot be empty and must be more than 6 characters
        if (password.isEmpty() || password.length() < 6) {
            Log.e(TAG + " validateInputs", "Password is either empty or less than 6 character!");
            llPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // All validation checks passed
        return true;
    }

    private void makeHTTPRequest(String email, String password) {
        // Define keys for the JSON request body
        String keyEmail = "patientEmail";
        String keyPassword = "patientPassword";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Populate the JSON request body
        try {
            requestBody.put(keyEmail, email);
            requestBody.put(keyPassword, password);
        } catch (JSONException e) {
            Log.e(TAG + " makeHTTPRequest", String.valueOf(e));
            return;
        }

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

    private void onRequestSuccess(JSONObject response) {
        try {
            // Extract success status and message from the JSON response
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                // Login successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Extract the userData object from the response
                JSONObject userDataJson = response.getJSONObject("userData");

                // Create a UserData object from the JSON
                UserData userData = new UserData(
                        userDataJson.getString("patientEmail"),
                        userDataJson.getInt("patientID"),
                        userDataJson.getString("patientImageURL"),
                        userDataJson.getString("patientMobileNumber"),
                        userDataJson.getString("patientName")
                );

                // Save the UserData object to SharedPreferences
                MySharedPreferences prefs = MySharedPreferences.getInstance(this);
                prefs.saveUserData(userData);

                // Log all user data to test if it has values
                if (userData != null) {
                    Log.d(TAG + " UserData", "User Data Exists:");
                    Log.d(TAG + " UserData", "Patient Email: " + userData.getPatientEmail());
                    Log.d(TAG + " UserData", "Patient ID: " + userData.getPatientID());
                    Log.d(TAG + " UserData", "Patient Image URL: " + userData.getPatientImageURL());
                    Log.d(TAG + " UserData", "Patient Mobile Number: " + userData.getPatientMobileNumber());
                    Log.d(TAG + " UserData", "Patient Name: " + userData.getPatientName());
                } else {
                    Log.d(TAG + " UserData", "No User Data found in SharedPreferences.");
                }

                llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_selected);
                llPassword.setBackgroundResource(R.drawable.shape_edit_text_selected);

                Intent intentActivity = new Intent(ActivityLogin.this, NewDashboard.class);
                startActivity(intentActivity);
            } else {
                // Login failed
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
                llPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }

    private void onRequestError(VolleyError error) {
        // Log and highlight entry
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
    }

}