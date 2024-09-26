// ActivityRegisterVerify.java
package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.thesis.dermocura.datas.RegistrationData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityRegisterVerify extends AppCompatActivity {

    // Declare Views
    TextView tvTitle, tvSubTitle, tvLogin;
    EditText etVerificationCode;
    ImageView ivVerificationCode;
    LinearLayout llVerificationCode;
    MaterialButton btnVerify;

    // Declare Strings
    private static final String TAG = "ActivityVerifyCode";
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_verify);
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
        tvLogin = findViewById(R.id.tvLogin);

        // Edit Texts
        etVerificationCode = findViewById(R.id.etVerificationCode);

        // Image Views
        ivVerificationCode = findViewById(R.id.ivVerificationCode);

        // Linear Layouts
        llVerificationCode = findViewById(R.id.llVerificationCode);

        // Material Buttons
        btnVerify = findViewById(R.id.btnVerify);

        // Initialize Loading Dialog
        loadingDialogFragment = new LoadingDialogFragment();
    }

    private void setOnClickListeners() {
        // Clickable Material button
        btnVerify.setOnClickListener(v -> clickVerify());
    }

    private void clickVerify() {
        // Retrieve user input for the verification code
        String verificationCode = etVerificationCode.getText().toString().trim();

        // Log user input for debugging purposes
        Log.i(TAG + " clickVerify", "User Input verification code: " + verificationCode);

        // Validate user input and make HTTP request if valid
        if (validateInput(verificationCode)) {
            // Get email from the registration data (or pass it through Intent)
            RegistrationData registrationData = RegistrationData.getInstance();
            String email = registrationData.getPatientEmail();

            // Show loading dialog
            if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
                loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
            }

            // Verify the code
            verifyCode(email, verificationCode);
        } else {
            Log.e(TAG + " clickVerify", "Validation Failed");
        }
    }

    private boolean validateInput(String verificationCode) {
        // Validate verification code - cannot be empty
        if (verificationCode.isEmpty()) {
            Log.e(TAG + " validateInput", "Verification code cannot be empty!");
            etVerificationCode.setError("Verification code cannot be empty");
            etVerificationCode.requestFocus();
            llVerificationCode.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }
        // All validation checks passed
        return true;
    }

    private void verifyCode(String email, String verificationCode) {
        // Define keys for the JSON request body
        String keyEmail = "email";
        String keyCode = "code";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Populate the JSON request body
        try {
            requestBody.put(keyEmail, email);
            requestBody.put(keyCode, verificationCode);
        } catch (JSONException e) {
            Log.e(TAG + " verifyCode", String.valueOf(e));
            dismissLoadingDialog();
            return;
        }

        // Define your backend URL for verifying the code
        String url = "https://backend.dermocura.net/phpmailer/verify_code.php";

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        // Log the JSON request body for debugging
        Log.i(TAG + " verifyCode", requestBody.toString());

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void onRequestSuccess(JSONObject response) {
        dismissLoadingDialog();
        try {
            // Extract success status and message from the JSON response
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                // Verification successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Toast.makeText(this, "Code Verified Successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ActivityRegisterVerify.this, ActivityRegisterInfo.class);
                startActivity(intent);
            } else {
                // Verification failed
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                displayErrorMessage(message); // Display the error message in EditText
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }

    private void onRequestError(com.android.volley.VolleyError error) {
        dismissLoadingDialog();
        // Log and highlight entry
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
            loadingDialogFragment.dismiss();
        }
    }

    // Method to display error messages from PHP in the EditText field
    private void displayErrorMessage(String message) {
        etVerificationCode.setError(message);
        etVerificationCode.requestFocus();
        llVerificationCode.setBackgroundResource(R.drawable.shape_edit_text_error);
    }
}
