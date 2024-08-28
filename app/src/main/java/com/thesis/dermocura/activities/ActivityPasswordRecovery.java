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

public class ActivityPasswordRecovery extends AppCompatActivity {

    // Declare views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle;
    EditText etEmailAddress, etRecoveryCode;
    ImageView ivEmailAddress, ivRecoveryCode;
    LinearLayout llHeader, llEmailAddress, llRecoveryCode;
    MaterialButton btnContinue;

    // Declare Strings
    private static final String TAG = "ActivityPasswordRecovery";
    private static final String URL = "https://backend.dermocura.net/android/recovery.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_recovery);
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
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);

        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);

        etEmailAddress = findViewById(R.id.etEmailAddress);
        etRecoveryCode = findViewById(R.id.etRecoveryCode);

        ivEmailAddress = findViewById(R.id.ivEmailAddress);
        ivRecoveryCode = findViewById(R.id.ivRecoveryCode);

        llHeader = findViewById(R.id.llHeader);
        llEmailAddress = findViewById(R.id.llEmailAddress);
        llRecoveryCode = findViewById(R.id.llRecoveryCode);

        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material Buttons
        btnContinue.setOnClickListener(v -> clickContinue());
    }

    private void clickContinue() {
        // Retrieve user input from the input fields
        String email = etEmailAddress.getText().toString();
        String code = etRecoveryCode.getText().toString();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input email: " + email);
        Log.i(TAG + " clickContinue", "User Input recovery code: " + code);

        // Validate user input
        if (validateInputs(email, code)) {
            makeHTTPRequest(email, code);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
        }
    }

    private boolean validateInputs(String email, String code) {
        // Validate email field - cannot be empty and must be a valid email format
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e(TAG + " validateInputs", "Email is either empty or not an email!");
            llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate code field - cannot be empty
        if (code.isEmpty()) {
            Log.e(TAG + " validateInputs", "Recovery Code is empty!");
            llRecoveryCode.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // All validation checks passed
        return true;
    }

    private void makeHTTPRequest(String email, String code) {
        // Define keys for the JSON request body
        String keyEmail = "patientEmail";
        String keyCode = "recoveryCode";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Populate the JSON request body
        try {
            requestBody.put(keyEmail, email);
            requestBody.put(keyCode, code);
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
                // successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);
                sendDataIntent();
            } else {
                // failed
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
                llRecoveryCode.setBackgroundResource(R.drawable.shape_edit_text_error);
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

    private void sendDataIntent() {
        // Create an intent to start the SetPassword activity
        Intent intentRegister = new Intent(ActivityPasswordRecovery.this, ActivityChangePassword.class);

        // Pass user data (email) to the SetPassword activity
        String email = etEmailAddress.getText().toString();
        intentRegister.putExtra("email", email);

        // Start the SetPassword activity
        startActivity(intentRegister);
    }
}