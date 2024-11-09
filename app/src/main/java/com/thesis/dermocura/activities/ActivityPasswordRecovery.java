// ActivityPasswordRecovery.java
package com.thesis.dermocura.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;
import com.thesis.dermocura.R;
import com.thesis.dermocura.retrof.ApiService;
import com.thesis.dermocura.retrof.RetrofitClient;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPasswordRecovery extends AppCompatActivity {

    // Declare views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle;
    EditText etEmailAddress, etRecoveryCode, etNewPassword;
    ImageView ivEmailAddress, ivRecoveryCode, ivNewPassword;
    LinearLayout llHeader, llEmailAddress, llRecoveryCode, llNewPassword;
    MaterialButton btnContinue;

    // Declare constants
    private static final String TAG = "ActivityPasswordRecovery";
    // The endpoints are relative to the base URL in RetrofitClient
    private static final String URL_SEND_CODE = "phpmailer/send_password_reset_code.php";
    private static final String URL_VERIFY_CODE = "phpmailer/verify_password_reset_code.php";
    private static final String URL_UPDATE_PASSWORD = "phpmailer/update_password.php";

    // Step counter
    private int currentStep = 1;

    // Loading dialog and request flag
    private LoadingDialogFragment loadingDialogFragment;
    private boolean isRequestInProgress = false; // Flag to prevent multiple requests

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

        // Initialize methods
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
        etNewPassword = findViewById(R.id.etNewPassword);

        ivEmailAddress = findViewById(R.id.ivEmailAddress);
        ivRecoveryCode = findViewById(R.id.ivRecoveryCode);
        ivNewPassword = findViewById(R.id.ivNewPassword);

        llHeader = findViewById(R.id.llHeader);
        llEmailAddress = findViewById(R.id.llEmailAddress);
        llRecoveryCode = findViewById(R.id.llRecoveryCode);
        llNewPassword = findViewById(R.id.llNewPassword);

        btnContinue = findViewById(R.id.btnContinue);

        // Hide Recovery Code and New Password fields initially
        llRecoveryCode.setVisibility(View.GONE);
        llNewPassword.setVisibility(View.GONE);

        // Set initial button text to "Send Code"
        btnContinue.setText("Send Code");

        // Initialize Loading Dialog
        loadingDialogFragment = new LoadingDialogFragment();
    }

    private void setOnClickListeners() {
        // Clickable Material Buttons
        btnContinue.setOnClickListener(v -> clickContinue());
    }

    private void clickContinue() {
        // Prevent multiple clicks by disabling the button and setting the flag
        if (isRequestInProgress) return; // Exit if a request is already in progress
        isRequestInProgress = true;
        btnContinue.setEnabled(false); // Disable the button to prevent multiple clicks

        switch (currentStep) {
            case 1:
                handleStep1();
                break;
            case 2:
                handleStep2();
                break;
            case 3:
                handleStep3();
                break;
            default:
                resetButtonState(); // Re-enable the button if an invalid step occurs
                break;
        }
    }

    private void handleStep1() {
        String email = etEmailAddress.getText().toString().trim();

        if (validateEmail(email)) {
            // Show loading dialog
            showLoadingDialog();

            // Send password reset code using Retrofit
            sendPasswordResetCode(email);
        } else {
            resetButtonState(); // Re-enable the button if validation fails
        }
    }

    private void handleStep2() {
        String email = etEmailAddress.getText().toString().trim();
        String code = etRecoveryCode.getText().toString().trim();

        if (validateCode(code)) {
            // Show loading dialog
            showLoadingDialog();

            // Verify password reset code using Retrofit
            verifyPasswordResetCode(email, code);
        } else {
            resetButtonState(); // Re-enable the button if validation fails
        }
    }

    private void handleStep3() {
        String email = etEmailAddress.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (validatePassword(newPassword)) {
            // Show loading dialog
            showLoadingDialog();

            // Update password using Retrofit
            updatePassword(email, newPassword);
        } else {
            resetButtonState(); // Re-enable the button if validation fails
        }
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
            Log.e(TAG, "Invalid email");
            return false;
        }
        llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text);
        return true;
    }

    private boolean validateCode(String code) {
        if (TextUtils.isEmpty(code)) {
            llRecoveryCode.setBackgroundResource(R.drawable.shape_edit_text_error);
            Log.e(TAG, "Recovery code is empty");
            return false;
        }
        llRecoveryCode.setBackgroundResource(R.drawable.shape_edit_text);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            llNewPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
            Log.e(TAG, "Password is empty");
            return false;
        }
        llNewPassword.setBackgroundResource(R.drawable.shape_edit_text);
        return true;
    }

    private void sendPasswordResetCode(String email) {
        // Prepare JSON object for the request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("patientEmail", email);

        // Create the Retrofit service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network call
        Call<JsonObject> call = apiService.sendPasswordResetCode(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dismissLoadingDialog();
                resetButtonState();

                if (response.isSuccessful() && response.body() != null) {
                    boolean success = response.body().get("success").getAsBoolean();
                    String message = response.body().get("message").getAsString();

                    if (success) {
                        // Password reset code sent successfully
                        Log.d(TAG + " sendPasswordResetCode", "Message Response: " + message);
                        currentStep = 2;
                        showRecoveryCodeField();
                        btnContinue.setText("Verify Code"); // Update button text
                    } else {
                        // Failed to send password reset code
                        Log.e(TAG + " sendPasswordResetCode", "Message Response: " + message);
                        llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
                    }
                } else {
                    Log.e(TAG + " sendPasswordResetCode", "Failed Response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dismissLoadingDialog();
                resetButtonState();
                Log.e(TAG + " sendPasswordResetCode", "Error Response: " + t.getMessage());
            }
        });
    }

    private void verifyPasswordResetCode(String email, String code) {
        // Prepare JSON object for the request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("patientEmail", email);
        requestBody.addProperty("recoveryCode", code);

        // Create the Retrofit service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network call
        Call<JsonObject> call = apiService.verifyPasswordResetCode(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dismissLoadingDialog();
                resetButtonState();

                if (response.isSuccessful() && response.body() != null) {
                    boolean success = response.body().get("success").getAsBoolean();
                    String message = response.body().get("message").getAsString();

                    if (success) {
                        // Recovery code verified successfully
                        Log.d(TAG + " verifyPasswordResetCode", "Message Response: " + message);
                        currentStep = 3;
                        showNewPasswordField();
                        btnContinue.setText("Change Password"); // Update button text
                    } else {
                        // Invalid recovery code
                        Log.e(TAG + " verifyPasswordResetCode", "Message Response: " + message);
                        llRecoveryCode.setBackgroundResource(R.drawable.shape_edit_text_error);
                    }
                } else {
                    Log.e(TAG + " verifyPasswordResetCode", "Failed Response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dismissLoadingDialog();
                resetButtonState();
                Log.e(TAG + " verifyPasswordResetCode", "Error Response: " + t.getMessage());
            }
        });
    }

    private void updatePassword(String email, String newPassword) {
        // Prepare JSON object for the request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("patientEmail", email);
        requestBody.addProperty("newPassword", newPassword);

        // Create the Retrofit service
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Make the network call
        Call<JsonObject> call = apiService.updatePassword(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                dismissLoadingDialog();
                resetButtonState();

                if (response.isSuccessful() && response.body() != null) {
                    boolean success = response.body().get("success").getAsBoolean();
                    String message = response.body().get("message").getAsString();

                    if (success) {
                        // Password updated successfully
                        Log.d(TAG + " updatePassword", "Message Response: " + message);
                        // Optionally, navigate to login screen or show a success message
                        finish(); // Close the activity
                    } else {
                        // Failed to update password
                        Log.e(TAG + " updatePassword", "Message Response: " + message);
                        llNewPassword.setBackgroundResource(R.drawable.shape_edit_text_error);
                    }
                } else {
                    Log.e(TAG + " updatePassword", "Failed Response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                dismissLoadingDialog();
                resetButtonState();
                Log.e(TAG + " updatePassword", "Error Response: " + t.getMessage());
            }
        });
    }

    private void showRecoveryCodeField() {
        llRecoveryCode.setVisibility(View.VISIBLE);
        // Update button constraint
        updateButtonConstraint(llRecoveryCode.getId());
    }

    private void showNewPasswordField() {
        llNewPassword.setVisibility(View.VISIBLE);
        // Update button constraint
        updateButtonConstraint(llNewPassword.getId());
    }

    private void updateButtonConstraint(int aboveViewId) {
        // Update the button's top constraint to be below the specified view
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btnContinue.getLayoutParams();
        params.topToBottom = aboveViewId;
        btnContinue.setLayoutParams(params);
    }

    private void showLoadingDialog() {
        if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
        }
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
}
