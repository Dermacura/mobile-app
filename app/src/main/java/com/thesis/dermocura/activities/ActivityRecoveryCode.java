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

import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;

public class ActivityRecoveryCode extends AppCompatActivity {

    // THIS ACTIVITY CLASS IS DONE AND CHECKED AT 2:01PM 8/6/2024
    // MAINTAINABILITY: CHECK
    // SCALABILITY: CHECK
    // READABILITY: CHECK

    // Declare Views
    TextView tvPageTitle, tvTitle, tvSubTitle, tvForgotPassword;
    ImageButton ibLeftArrow, ibRightArrow;
    ImageView ivCode1, ivCode2, ivCode3;
    EditText etCode1, etCode2, etCode3;
    LinearLayout llHeader, llCode1, llCode2, llCode3;
    MaterialButton btnContinue;

    // Declare Strings
    String code1, code2, code3;
    private static final String TAG = "ActivityRecoveryCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recovery_code);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Methods
        receiveDataIntent();
        initializeObjects();
        setOnClickListeners();
        setEditText();
    }

    private void initializeObjects() {
        // Text Views
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvForgotPassword = findViewById(R.id.tvInformation);
        // Image Buttons
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        // Image Views
        ivCode1 = findViewById(R.id.ivSkinType);
        ivCode2 = findViewById(R.id.ivAdditional);
        ivCode3 = findViewById(R.id.ivGender);
        // Edit Texts
        etCode1 = findViewById(R.id.etSkinType);
        etCode2 = findViewById(R.id.etAdditional);
        etCode3 = findViewById(R.id.etGender);
        // Linear Layouts
        llHeader = findViewById(R.id.llHeader);
        llCode1 = findViewById(R.id.llSkinType);
        llCode2 = findViewById(R.id.llAdditional);
        llCode3 = findViewById(R.id.llGender);
        // Material Buttons
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material Buttons
        btnContinue.setOnClickListener(v -> clickContinue());
    }

    private void receiveDataIntent() {
        // Retrieve the Intent that started this activity
        Intent intent = getIntent();

        // Extract data passed from the previous activity
        code1 = intent.getStringExtra("code1");
        code2 = intent.getStringExtra("code2");
        code3 = intent.getStringExtra("code3");

        // Log received data
        Log.d(TAG + " receiveDataIntent", "Code #1: " + code1);
        Log.d(TAG + " receiveDataIntent", "Code #2: " + code2);
        Log.d(TAG + " receiveDataIntent", "Code #3: " + code3);
    }

    private void setEditText() {
        // Set the text values of the EditText fields with the received recovery codes
        etCode1.setText(code1);
        etCode2.setText(code2);
        etCode3.setText(code3);
    }

    private void clickContinue() {
        // Create an intent to start the Login activity
        Intent intentLogin = new Intent(ActivityRecoveryCode.this, ActivityLogin.class);

        // Start the Login activity
        startActivity(intentLogin);

        // Finish the current activity
        finish();
    }
}