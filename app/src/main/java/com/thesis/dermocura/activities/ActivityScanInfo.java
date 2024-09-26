package com.thesis.dermocura.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.ClassBase64Convert;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.ScanData;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ActivityScanInfo extends AppCompatActivity {

    // Declare views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle, tvInformation;
    EditText etDuration, etSkinType, etAdditionalInformation;
    ImageView ivAge, ivDuration, ivGender;
    LinearLayout llHeader, llAge, llDuration, llGender;
    MaterialButton btnContinue;

    // Declare Strings
    private static final String TAG = "ActivityScanInfo";
    private static final String URL = "https://zxky.tail07dc9b.ts.net/predict";
    private LoadingDialogFragment loadingDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the loading dialog
        loadingDialogFragment = new LoadingDialogFragment();

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
        etDuration = findViewById(R.id.etDuration);
        etAdditionalInformation = findViewById(R.id.etAdditionalInformation);

        ivAge = findViewById(R.id.ivSkinType);
        ivDuration = findViewById(R.id.ivAdditional);
        ivGender = findViewById(R.id.ivGender);

        llHeader = findViewById(R.id.llHeader);
        llAge = findViewById(R.id.llSkinType);
        llDuration = findViewById(R.id.llAdditional);
        llGender = findViewById(R.id.llGender);

        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setOnClickListeners() {
        // Clickable Material buttons
        btnContinue.setOnClickListener(v -> {
            clickContinue();
        });
    }

    private void clickContinue() {
        // Show loading dialog
        if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
        }

        // Retrieve user input from the input fields
        String skinType = etSkinType.getText().toString();
        String duration = etDuration.getText().toString();
        String additional = etAdditionalInformation.getText().toString();
        Uri imageUri = ScanData.getInstance().getImageUri();

        // Log user input for debugging purposes
        Log.i(TAG + " clickContinue", "User Input skinType: " + skinType);
        Log.i(TAG + " clickContinue", "User Input duration: " + duration);
        Log.i(TAG + " clickContinue", "User Input additional: " + additional);

        if (imageUri != null) {
            try {
                String base64String = ClassBase64Convert.convertUriToBase64(this, imageUri);
                // Validate user input
                if (validateInputs(skinType, duration, additional, imageUri)) {
                    storeInformation(skinType, duration, additional);
                } else {
                    Log.e(TAG + " clickContinue", "Validation Failed");
                }
                makeHTTPRequest(base64String);
            } catch (IOException e) {
                Log.e(TAG, "Failed to convert Uri to Base64", e);
            }
        } else {
            Log.d(TAG, "No imageUri found in ClassScanData.");
        }
    }

    private boolean validateInputs(String age, String duration, String gender, Uri base64) {
        if (age.isEmpty()) {
            Log.e(TAG + " validateInputs", "age is empty!");
            llAge.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        if (duration.isEmpty()) {
            Log.e(TAG + " validateInputs", "duration is empty!");
            llDuration.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        if (gender.isEmpty()) {
            Log.e(TAG + " validateInputs", "gender is empty!");
            llGender.setBackgroundResource(R.drawable.shape_edit_text_error);
            return false;
        }

        // Check if image is missing (base64 is null)
        if (base64 == null) {
            Log.e(TAG + " validateInputs", "Image is empty!");
            return false;
        }

        return true;
    }

    private void storeInformation(String skinType, String duration, String additional) {
        ScanData.getInstance().setSkinType(skinType);
        ScanData.getInstance().setDuration(duration);
        ScanData.getInstance().setAdditional(additional);
    }

    private void makeHTTPRequest(String base64String) {
        // Define keys for the JSON request body
        String keyImage = "image";
        String keyPatientID = "patientID";
        String keyPatientSkinType = "patientSkinType";
        String keyPatientDuration = "patientDuration";
        String keyPatientAdditional = "patientAdditional";

        // Retrieve the UserData object from SharedPreferences
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();

        int patient_id = userData.getPatientID();
        String skin_type = ScanData.getInstance().getSkinType();
        String duration = ScanData.getInstance().getDuration();
        String additional = ScanData.getInstance().getAdditional();


        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d(TAG, "makeHTTPRequest: Image" + base64String);

        // Populate the JSON request body
        try {
            requestBody.put(keyImage, base64String);
            requestBody.put(keyPatientID, patient_id);
            requestBody.put(keyPatientSkinType, skin_type);
            requestBody.put(keyPatientDuration, duration);
            requestBody.put(keyPatientAdditional, additional);
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // Timeout in milliseconds (e.g., 10 seconds)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Backoff multiplier
        ));

        // Log the JSON request body for debugging
        String stringJSON = requestBody.toString();
        Log.i(TAG + " makeHTTPRequest", stringJSON);

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void onRequestSuccess(JSONObject response) {
        dismissLoadingDialog(); // Dismiss loading dialog on success
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                String diseaseName = response.getString("disease_name");
                String description = response.getString("disease_description");
                String recommendation = response.getString("disease_recommendation");
                String treatment = response.getString("disease_treatment");
                String symptoms = response.getString("disease_symptoms");

                Intent intent = new Intent(this, ActivityDiseaseInfo.class);
                intent.putExtra("disease_name", diseaseName);
                intent.putExtra("description", description);
                intent.putExtra("recommendation", recommendation);
                intent.putExtra("treatment", treatment);
                intent.putExtra("symptoms", symptoms);
                startActivity(intent);
            } else {
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }

    private void onRequestError(VolleyError error) {
        dismissLoadingDialog(); // Dismiss loading dialog on error
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
        if (error.networkResponse != null) {
            Log.e(TAG + " onRequestError", "Status Code: " + error.networkResponse.statusCode);
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                Log.e(TAG + " onRequestError", "Response Body: " + responseBody);
            } catch (Exception e) {
                Log.e(TAG + " onRequestError", "Failed to parse error response", e);
            }
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
            loadingDialogFragment.dismiss();
        }
    }
}