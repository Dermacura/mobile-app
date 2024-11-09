package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.ScanData;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityScanInfo extends AppCompatActivity {

    // Declare views
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle, tvInformation;
    Spinner spinnerSkinType;
    EditText etDuration, etAdditionalInformation;
    ImageView ivDuration, ivAdditionalInformation;
    LinearLayout llHeader, llDuration, llAdditionalInformation;
    MaterialButton btnContinue;

    // Loading dialog instance
    private LoadingDialogFragment loadingDialog;

    // Declare constants
    private static final String TAG = "ActivityScanInfo";
    private static final String PREDICT_URL = "https://zxky.tail07dc9b.ts.net/predict";  // Update with your actual Flask server URL
    private static final String SAVE_RECORD_URL = "https://backend.dermocura.net/android/analysis/save_record.php";  // URL to your PHP script

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

        // Initialize views and loading dialog
        initializeObjects();
        loadingDialog = new LoadingDialogFragment();

        // Set up the skin type spinner
        setupSkinTypeSpinner();

        // Set click listeners
        setOnClickListeners();
    }

    private void initializeObjects() {
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);

        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvInformation = findViewById(R.id.tvInformation);

        spinnerSkinType = findViewById(R.id.spinnerSkinType);
        etDuration = findViewById(R.id.etDuration);
        etAdditionalInformation = findViewById(R.id.etAdditionalInformation);

        ivDuration = findViewById(R.id.ivDuration);
        ivAdditionalInformation = findViewById(R.id.ivAdditionalInformation);

        llHeader = findViewById(R.id.llHeader);
        llDuration = findViewById(R.id.llDuration);
        llAdditionalInformation = findViewById(R.id.llAdditionalInformation);

        btnContinue = findViewById(R.id.btnContinue);
    }

    private void setupSkinTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.skin_type_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSkinType.setAdapter(adapter);
    }

    private void setOnClickListeners() {
        btnContinue.setOnClickListener(v -> clickContinue());
    }

    private void clickContinue() {
        String skinType = spinnerSkinType.getSelectedItem().toString();
        String duration = etDuration.getText().toString();
        String additional = etAdditionalInformation.getText().toString();

        if (validateInputs(skinType, duration, additional)) {
            storeInformation(skinType, duration, additional);

            String base64Image = ScanData.getInstance().getBase64Image();
            if (base64Image != null) {
                showLoadingDialog();  // Show the loading dialog
                sendPredictRequest(base64Image, additional);
            } else {
                Log.e(TAG, "No Base64 image found in ScanData");
            }
        }
    }

    private void sendPredictRequest(String base64Image, String symptoms) {
        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("base64_image", base64Image);
            requestBody.put("symptoms", symptoms);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON request body", e);
            return;
        }

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the POST request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                PREDICT_URL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            // Parsing the result
                            JSONObject result = response.getJSONObject("result");
                            int predictedClassIndex = result.getInt("predicted_class_index");
                            double confidence = result.getDouble("confidence");
                            String filename = result.getString("filename");

                            Log.d(TAG, "Prediction response:");
                            Log.d(TAG, "Predicted class index: " + predictedClassIndex);
                            Log.d(TAG, "Confidence: " + confidence);
                            Log.d(TAG, "Filename: " + filename);

                            // Save the record to the database
                            saveRecordToDatabase(predictedClassIndex, confidence, filename);

                        } else {
                            Log.e(TAG, "Prediction failed: " + message);
                            dismissLoadingDialog();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response JSON", e);
                        dismissLoadingDialog();
                    }
                },
                error -> {
                    Log.e(TAG, "HTTP request failed", error);
                    dismissLoadingDialog();  // Dismiss the loading dialog
                }
        );

        // Add the request to the Volley queue
        queue.add(request);
    }

    private void saveRecordToDatabase(int predictedClassIndex, double confidence, String filename) {
        // Get patientID from SharedPreferences
        UserData userData = MySharedPreferences.getInstance(this).getUserData();
        if (userData == null) {
            Log.e(TAG, "User data not found in SharedPreferences");
            dismissLoadingDialog();
            return;
        }
        int patientID = userData.getPatientID();

        // Get other data from ScanData singleton
        String skinType = ScanData.getInstance().getSkinType();
        String duration = ScanData.getInstance().getDuration();
        String additional = ScanData.getInstance().getAdditional();

        // Get current date and time
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", patientID);
            requestBody.put("skinDiseaseID", predictedClassIndex);
            // requestBody.put("symptomDiseaseID", null); // Ignored as per your instruction
            requestBody.put("patientSkinType", skinType);
            requestBody.put("patientSkinDiseaseConfidence", confidence);
            requestBody.put("patientDuration", duration);
            requestBody.put("patientAdditional", additional);
            requestBody.put("patientAnalyzedDate", currentDate);
            requestBody.put("patientAnalyzedTime", currentTime);
            requestBody.put("patientScanImage", filename);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON request body for saving record", e);
            dismissLoadingDialog();
            return;
        }

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the POST request to save the record
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                SAVE_RECORD_URL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Log.d(TAG, "Record saved successfully: " + message);

                            // Clear ScanData on successful prediction
                            ScanData.getInstance().clearScanData();

                            // Launch the next activity with the predicted class index
                            Intent intent = new Intent(ActivityScanInfo.this, ActivityDiseaseInfo.class);
                            intent.putExtra("skinDiseaseID", String.valueOf(predictedClassIndex));
                            intent.putExtra("confidence", confidence);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "Failed to save record: " + message);
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing save record response JSON", e);
                    } finally {
                        dismissLoadingDialog();  // Dismiss the loading dialog
                    }
                },
                error -> {
                    Log.e(TAG, "HTTP request to save record failed", error);
                    dismissLoadingDialog();  // Dismiss the loading dialog
                }
        );

        // Add the request to the Volley queue
        queue.add(request);
    }

    private boolean validateInputs(String skinType, String duration, String additional) {
        boolean isValid = true;

        if (skinType.isEmpty()) {
            llDuration.setBackgroundResource(R.drawable.shape_edit_text_error);
            isValid = false;
        }
        if (duration.isEmpty()) {
            llDuration.setBackgroundResource(R.drawable.shape_edit_text_error);
            isValid = false;
        }
        if (additional.isEmpty()) {
            llAdditionalInformation.setBackgroundResource(R.drawable.shape_edit_text_error);
            isValid = false;
        }
        return isValid;
    }

    private void storeInformation(String skinType, String duration, String additional) {
        ScanData.getInstance().setSkinType(skinType);
        ScanData.getInstance().setDuration(duration);
        ScanData.getInstance().setAdditional(additional);
    }

    // Show the loading dialog
    private void showLoadingDialog() {
        if (loadingDialog != null && !loadingDialog.isAdded()) {
            loadingDialog.show(getSupportFragmentManager(), "LoadingDialog");
        }
    }

    // Dismiss the loading dialog
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isAdded()) {
            loadingDialog.dismiss();
        }
    }
}
