// ActivityRegister.java
package com.thesis.dermocura.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.CheckBox;
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

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.thesis.dermocura.R;
import com.thesis.dermocura.datas.RegistrationData;
import com.thesis.dermocura.retrof.ApiService;
import com.thesis.dermocura.retrof.RetrofitClient;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityRegister extends AppCompatActivity {

    // Declare Views
    TextView tvTitle, tvSubTitle, tvLogin;
    EditText etEmailAddress, etPassword, etConfirmPassword;
    ImageView ivEmailAddress, ivPassword, ivConfirmPassword;
    LinearLayout llEmailAddress, llPassword, llConfirmPassword;
    MaterialButton btnContinue;

    CheckBox cbTerms;
    TextView tvTerms;

    // Declare Strings and loading dialog
    private static final String TAG = "ActivityRegister";
    private String email, password;
    private LoadingDialogFragment loadingDialogFragment;
    private boolean isRequestInProgress = false; // Flag to prevent multiple requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Image Views
        ivEmailAddress = findViewById(R.id.ivEmailAddress);
        ivPassword = findViewById(R.id.ivPassword);
        ivConfirmPassword = findViewById(R.id.ivConfirmPassword);

        // Linear Layouts
        llEmailAddress = findViewById(R.id.llEmailAddress);
        llPassword = findViewById(R.id.llPassword);
        llConfirmPassword = findViewById(R.id.llConfirmPassword);

        // Material Buttons
        btnContinue = findViewById(R.id.btnContinue);

        // Initialize Loading Dialog
        loadingDialogFragment = new LoadingDialogFragment();

        cbTerms = findViewById(R.id.cbTerms);
        tvTerms = findViewById(R.id.tvTerms);
    }

    private void setOnClickListeners() {
        // Clickable Material button
        btnContinue.setOnClickListener(v -> clickContinue());
        // Clickable Text View
        tvLogin.setOnClickListener(v -> clickLogin());
        tvTerms.setOnClickListener(v -> openTermsAndPrivacyPolicy());
    }

    private void openTermsAndPrivacyPolicy() {
        // Replace this URL with the actual terms and privacy policy link
        String url = "https://backend.dermocura.net/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void clickContinue() {
        // Prevent multiple clicks by disabling the button and setting the flag
        if (isRequestInProgress) return; // Exit if a request is already in progress
        isRequestInProgress = true;
        btnContinue.setEnabled(false); // Disable the button to prevent multiple clicks

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve user input from the input fields
        email = etEmailAddress.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input email: " + email);
        Log.i(TAG + " clickContinue", "User Input password: " + password);
        Log.i(TAG + " clickContinue", "User Input confirm password: " + confirmPassword);

        // Validate user input
        if (validateInputs(email, password, confirmPassword)) {
            Log.d(TAG, "User Input: " + email + ", " + password + ", " + confirmPassword);

            // Show the loading dialog when the request starts
            if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
                loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
            }

            // Send verification code to the user's email using Retrofit
            sendVerificationCode(email);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
            resetButtonState(); // Re-enable the button if validation fails
        }
    }

    private void clickLogin() {
        // Create an intent to start the Login activity
        Intent intentLogin = new Intent(ActivityRegister.this, ActivityLogin.class);

        // Start the Login activity
        startActivity(intentLogin);

        // Finish the current activity
        finish();
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        // Validate email field - cannot be empty and must be a valid email format
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e(TAG + " validateInputs", "Invalid email address!");
            etEmailAddress.setError("Invalid email address");
            etEmailAddress.requestFocus();
            llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate password length
        if (password.length() < 8 || password.length() > 16) {
            Log.e(TAG + " validateInputs", "Password must be between 8 and 16 characters!");
            etPassword.setError("Password must be between 8 and 16 characters");
            etPassword.requestFocus();
            llPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate confirm password field - must match the password
        if (!password.equals(confirmPassword)) {
            Log.e(TAG + " validateInputs", "Passwords do not match!");
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            llConfirmPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            llPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // All validation checks passed
        return true;
    }

    private void sendVerificationCode(String email) {
        // Prepare JSON object for the request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("email", email);
        requestBody.addProperty("timestamp", getCurrentTimestamp()); // Add the timestamp

        // Create the Retrofit service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network call
        Call<JsonObject> call = apiService.sendVerificationCode(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dismissLoadingDialog();
                resetButtonState();

                if (response.isSuccessful() && response.body() != null) {
                    boolean success = response.body().get("success").getAsBoolean();
                    String message = response.body().get("message").getAsString();

                    if (success) {
                        // Verification or sending code successful
                        Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                        Log.d(TAG + " onRequestSuccess", "JSON Received: " + response.body());

                        // Save Info
                        RegistrationData registrationData = RegistrationData.getInstance();
                        registrationData.setPatientEmail(email);
                        registrationData.setPatientPassword(password);

                        // Proceed to the next activity
                        Intent intent = new Intent(ActivityRegister.this, ActivityRegisterVerify.class);
                        startActivity(intent);
                    } else {
                        // Verification or sending code failed
                        Log.e(TAG + " onRequestSuccess", "Message Response: " + message);

                        // Display the error message if the email is already in use
                        if (message.equals("This email is already registered. Please use a different email address.")) {
                            etEmailAddress.setError("This email is already registered");
                            etEmailAddress.requestFocus();
                            llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
                        }
                    }
                } else {
                    Log.e(TAG + " onRequestSuccess", "Failed Response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dismissLoadingDialog();
                resetButtonState();
                Log.e(TAG + " onRequestError", "Error Response: " + t.getMessage());
            }
        });
    }

    private void dismissLoadingDialog() {
        if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
            loadingDialogFragment.dismiss();
        }
    }

    private void resetButtonState() {
        isRequestInProgress = false;
        btnContinue.setEnabled(true); // Re-enable the button after request completion
    }

    // Add this method to generate the current timestamp in the desired format
    private String getCurrentTimestamp() {
        // Set the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // Set the timezone to Asia/Manila
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
        // Return the formatted current date and time
        return sdf.format(new Date());
    }
}
