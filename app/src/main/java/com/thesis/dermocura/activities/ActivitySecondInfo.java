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
import com.thesis.dermocura.datas.ScanData;

public class ActivitySecondInfo extends AppCompatActivity {

    // Declare views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle, tvInformation;
    EditText etSkinType, etAdditional;
    ImageView ivSkinType, ivAdditional;
    LinearLayout llHeader, llSkinType, llAdditional;
    MaterialButton btnContinue;

    // Declare Strings
    private static final String TAG = "ActivitySecondInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second_info);
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
        tvInformation = findViewById(R.id.tvInformation);
        etSkinType = findViewById(R.id.etSkinType);
        etAdditional = findViewById(R.id.etAdditional);
        ivSkinType = findViewById(R.id.ivSkinType);
        ivAdditional = findViewById(R.id.ivAdditional);
        llHeader = findViewById(R.id.llHeader);
        llSkinType = findViewById(R.id.llSkinType);
        llAdditional = findViewById(R.id.llAdditional);
        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material buttons
        btnContinue.setOnClickListener(v -> clickContinue());
    }

    private void clickContinue() {
        // Retrieve user input from the input fields
        String skin_type = etSkinType.getText().toString();
        String additional = etAdditional.getText().toString();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input Skin Type: " + skin_type);
        Log.i(TAG + " clickContinue", "User Input Additional: " + additional);

        // Validate user input
        if (validateInputs(skin_type, additional)) {
            storeInformation(skin_type, additional);
            Intent intentSecondInfo = new Intent(ActivitySecondInfo.this, ActivityDiseaseInfo.class);
            startActivity(intentSecondInfo);
        } else {
            Log.e(TAG + " clickContinue", "Validation Failed");
        }
    }

    private boolean validateInputs(String skin_type, String additional) {
        // Validate skin_type field - cannot be empty
        if (skin_type.isEmpty()) {
            Log.e(TAG + " validateInputs", "Skin Type is empty!");
            llSkinType.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Validate additional field - cannot be empty
        if (additional.isEmpty()) {
            Log.e(TAG + " validateInputs", "Additional is empty!");
            llAdditional.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // All validation checks passed
        return true;
    }

    private void storeInformation(String skin_type, String additional) {
        ScanData.getInstance().setSkinType(skin_type);
        ScanData.getInstance().setAdditional(additional);
    }

    private void fetchIntentExtra() {
        Intent intent = getIntent();
    }
}