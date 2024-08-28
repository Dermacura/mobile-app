package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class ActivitySetPassword extends AppCompatActivity {

    // THIS ACTIVITY CLASS IS DONE AND CHECKED AT 1:56PM 8/6/2024
    // MAINTAINABILITY: CHECK
    // SCALABILITY: CHECK
    // READABILITY: CHECK

    // Declare Views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle;
    ImageView ivPassword, ivConfirmPassword;
    EditText etPassword, etConfirmPassword;
    LinearLayout llHeader, llPassword, llConfirmPassword;
    MaterialButton btnContinue;

    // Declare Strings
    private static final String TAG = "ActivitySetPassword";
    private static final String URL = "https://backend.dermocura.net/android/register.php";
    String name, email, phone, code1, code2, code3;

    // Words for recovery code
    private static final String[] RECOVERY_WORDS = {
            "kinetic", "meager", "continue", "flowery", "imbibe", "marksmen",
            "hussar", "weekly", "disdain", "cafe", "humidor", "becalm",
            "apple", "banana", "cherry", "date", "elderberry", "fig", "grape",
            "honeydew", "iceberg", "kiwi", "lemon", "mango",
            "nectarine", "orange", "papaya", "quince", "raspberry", "strawberry"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Methods
        initializeObjects();
        setOnClickListeners();
        receiveDataIntent();
    }

    private void initializeObjects() {
        // Image Buttons
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        // Text Views
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        // Image Views
        ivPassword = findViewById(R.id.ivPassword);
        ivConfirmPassword = findViewById(R.id.ivConfirmPassword);
        // Edit Texts
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        // Linear Layouts
        llHeader = findViewById(R.id.llHeader);
        llPassword = findViewById(R.id.llPassword);
        llConfirmPassword = findViewById(R.id.llConfirmPassword);
        // Material Buttons
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material buttons
        btnContinue.setOnClickListener(v -> clickContinue());
    }

    private void clickContinue() {
        // Retrieve user input from the input fields
        String password = etPassword.getText().toString();
        String passwordConfirm = etConfirmPassword.getText().toString();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input Password: " + password);
        Log.i(TAG + " clickContinue", "User Input Confirm Password: " + passwordConfirm);

        // Validate user input
        if (validateInputs(password, passwordConfirm)) {
            generateRecoveryCodes();
            makeHTTPRequest(password);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
        }
    }

    private boolean validateInputs(String password, String passwordConfirm) {
        // Validate password field - cannot be empty
        if (password.isEmpty()) {
            Log.e(TAG + " validateInputs", "Password is Empty!");
            llPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate confirm password field - cannot be empty
        if (passwordConfirm.isEmpty()) {
            Log.e(TAG + " validateInputs", "Confirm Password is Empty!");
            llConfirmPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate both password field - must be the same
        if (!password.equals(passwordConfirm)) {
            Log.e(TAG + " validateInputs", "Password and Confirm Password not the same!");
            llPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            llConfirmPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // All validation checks passed
        return true;
    }

    private void generateRecoveryCodes() {
        // Generate Recovery Codes
        code1 = generateRecoveryCode();
        code2 = generateRecoveryCode();
        code3 = generateRecoveryCode();

        // Log Codes
        // TODO: TO REMOVE FOR SECURITY PURPOSE
        Log.i(TAG + " generateRecoveryCodes", "Recovery Code #1: " + code1);
        Log.i(TAG + " generateRecoveryCodes", "Recovery Code #2: " + code2);
        Log.i(TAG + " generateRecoveryCodes", "Recovery Code #3: " + code3);
    }

    private static String generateRecoveryCode() {
        // Create a random number generator
        Random random = new Random();

        // Create an array to store the four random words
        String[] words = new String[4];

        // Generate four random words from the RECOVERY_WORDS array
        for (int i = 0; i < 4; i++) {
            words[i] = RECOVERY_WORDS[random.nextInt(RECOVERY_WORDS.length)];
        }

        // Join the words with hyphens to create the recovery code
        return String.join("-", words);
    }

    private void receiveDataIntent() {
        // Retrieve the Intent that started this activity
        Intent intent = getIntent();

        // Extract data passed from the previous activity
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        phone = intent.getStringExtra("phone");
    }

    private void sendDataIntent() {
        // Create an intent to start the RecoveryCodes activity
        Intent intentRecoveryCode = new Intent(ActivitySetPassword.this, ActivityRecoveryCode.class);

        // Pass user data (code1, code2, code3) to the SetPassword activity
        intentRecoveryCode.putExtra("code1", code1);
        intentRecoveryCode.putExtra("code2", code2);
        intentRecoveryCode.putExtra("code3", code3);

        // Start the SetPassword activity
        startActivity(intentRecoveryCode);
    }

    private void makeHTTPRequest(String password) {
        // Define keys for the JSON request body
        String keyName = "patientName";
        String keyEmail = "patientEmail";
        String keyNumber = "patientMobileNumber";
        String keyPassword = "patientPassword";
        String keyCode1 = "patientRecoveryCode1";
        String keyCode2 = "patientRecoveryCode2";
        String keyCode3 = "patientRecoveryCode3";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Populate the JSON request body
        try {
            requestBody.put(keyName, name);
            requestBody.put(keyEmail, email);
            requestBody.put(keyNumber, phone);
            requestBody.put(keyPassword, password);
            requestBody.put(keyCode1, code1);
            requestBody.put(keyCode2, code2);
            requestBody.put(keyCode3, code3);
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
                sendDataIntent();
            } else {
                // Login failed
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                llConfirmPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
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