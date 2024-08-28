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

public class ActivityChangePassword extends AppCompatActivity {

    // Declare Views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle;
    ImageView ivPassword, ivConfirmPassword;
    EditText etPassword, etConfirmPassword;
    LinearLayout llHeader, llPassword, llConfirmPassword;
    MaterialButton btnContinue;

    // Declare Strings
    String email;
    private static final String TAG = "ActivitySetPassword";
    private static final String URL = "https://backend.dermocura.net/android/changepassword.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
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
            makeHTTPRequest(password);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
        }
    }

    private void receiveDataIntent() {
        // Retrieve the Intent that started this activity
        Intent intent = getIntent();

        // Extract data passed from the previous activity
        email = intent.getStringExtra("email");
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

    private void makeHTTPRequest(String password) {
        // Define keys for the JSON request body
        String keyEmail = "patientEmail";
        String keyPassword = "newPassword";

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
                // successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);
                Intent intentLogin = new Intent(ActivityChangePassword.this, ActivityLogin.class);
                startActivity(intentLogin);
                finish();
            } else {
                // failed
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