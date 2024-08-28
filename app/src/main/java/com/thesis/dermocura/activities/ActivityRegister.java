package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;

public class ActivityRegister extends AppCompatActivity {

    // THIS ACTIVITY CLASS IS DONE AND CHECKED AT
    // MAINTAINABILITY:
    // SCALABILITY:
    // READABILITY:

    // Declare Views
    TextView tvTitle, tvSubTitle, tvLogin;
    EditText etFullName, etEmailAddress, etPhoneNumber;
    ImageView ivFullName, ivEmailAddress, ivPhoneNumber;
    LinearLayout llFullName, llEmailAddress, llPhoneNumber;
    MaterialButton btnContinue;

    // Declare Strings
    private static final String TAG = "ActivityRegister";

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
        etFullName = findViewById(R.id.etFullName);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        // Image Views
        ivFullName = findViewById(R.id.ivFullName);
        ivEmailAddress = findViewById(R.id.ivEmailAddress);
        ivPhoneNumber = findViewById(R.id.ivPhoneNumber);
        // Linear Layouts
        llFullName = findViewById(R.id.llFullName);
        llEmailAddress = findViewById(R.id.llEmailAddress);
        llPhoneNumber = findViewById(R.id.llPhoneNumber);
        // Material Buttons
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material buttons
        btnContinue.setOnClickListener(v -> clickContinue());
        // Clickable Text Views
        tvLogin.setOnClickListener(v -> clickLogin());
    }

    private void clickContinue() {
        // Retrieve user input from the input fields
        String name = etFullName.getText().toString();
        String email = etEmailAddress.getText().toString();
        String phone = etPhoneNumber.getText().toString();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input name: " + name);
        Log.i(TAG + " clickContinue", "User Input email: " + email);
        Log.i(TAG + " clickContinue", "User Input phone: " + phone);

        // Validate user input
        if (validateInputs(name, email, phone)) {
            sendDataIntent(name, email, phone);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
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

    private boolean validateInputs(String name, String email, String phone) {
        // Validate name field - cannot be empty
        if (name.isEmpty()) {
            Log.e(TAG + " validateInputs", "Name cannot be empty!");
            llFullName.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate email field - cannot be empty and must be a valid email format
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.e(TAG + " validateInputs", "Email cannot be empty!");
            llEmailAddress.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate phone field - cannot be empty and must be a valid phone format
        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
            Log.e(TAG + " validateInputs", "Phone cannot be empty!");
            llPhoneNumber.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // All validation checks passed
        return true;
    }

    private void sendDataIntent(String name, String email, String phone) {
        // Create an intent to start the SetPassword activity
        Intent intentRegister = new Intent(ActivityRegister.this, ActivitySetPassword.class);

        // Pass user data (name, email, phone) to the SetPassword activity
        intentRegister.putExtra("name", name);
        intentRegister.putExtra("email", email);
        intentRegister.putExtra("phone", phone);

        // Start the SetPassword activity
        startActivity(intentRegister);
    }
}