package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityDashboard extends AppCompatActivity {

    ConstraintLayout clInnerContainer;
    LinearLayout llProfileFrame, llUserInfo, llScanInfo, llLastScanContent, llTotalScanContent, llQuickActions1, llQuickActions2, llAppointmentContent, llGeolocationContent, llInformationContent, llSupportContent, llMessageContent, llScanHistoryContent;
    CardView cvProfileImage, cvLastScan, cvTotalScan, cvPreviousScan, cvAppointment, cvGeolocation, cvInformation, cvSupport, cvMessage, cvScanHistory;
    ImageView ivProfileImage, ivAppointmentIcon, ivGeolocationIcon, ivInformationIcon, ivSupportIcon, ivMessageIcon, ivScanHistoryIcon;
    TextView tvUserName, tvUserEmail, tvUserPhone, tvLastScanLabel, tvLastScanDays, tvTotalScanLabel, tvTotalScanCount, tvPreviousScanDetails, tvAppointmentLabel, tvGeolocationLabel, tvInformationLabel, tvSupportLabel, tvMessageLabel, tvScanHistoryLabel;
    MaterialButton btnAnalyzeSkinDisease;

    // Declare Strings
    private static final String TAG = "ActivityDashboard";
    private static final String URL = "https://backend.dermocura.net/android/userscananalytics.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Construct Profile Picture Link
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();
        String ProfileLink = "https://backend.dermocura.net/images/user_profile/" + userData.getPatientImageURL();
        String name = userData.getPatientName();
        String email = userData.getPatientEmail();
        String phone = userData.getPatientMobileNumber();
        int id = userData.getPatientID();

        // Initialize Methods
        initializeObjects();
        setOnClickListeners();
        loadImageFromUrl(ProfileLink, ivProfileImage);
        loadUserData(name, email, phone);
        makeHTTPRequest(id);
    }

    private void initializeObjects() {
        clInnerContainer = findViewById(R.id.clInnerContainer);

        llProfileFrame = findViewById(R.id.llProfileFrame);
        llUserInfo = findViewById(R.id.llUserInfo);
        llScanInfo = findViewById(R.id.llScanInfo);
        llLastScanContent = findViewById(R.id.llLastScanContent);
        llTotalScanContent = findViewById(R.id.llTotalScanContent);
        llQuickActions1 = findViewById(R.id.llQuickActions1);
        llQuickActions2 = findViewById(R.id.llQuickActions2);
        llAppointmentContent = findViewById(R.id.llAppointmentContent);
        llGeolocationContent = findViewById(R.id.llGeolocationContent);
        llInformationContent = findViewById(R.id.llInformationContent);
        llSupportContent = findViewById(R.id.llSupportContent);
        llMessageContent = findViewById(R.id.llMessageContent);
        llScanHistoryContent = findViewById(R.id.llScanHistoryContent);

        cvProfileImage = findViewById(R.id.cvProfileImage);
        cvLastScan = findViewById(R.id.cvLastScan);
        cvTotalScan = findViewById(R.id.cvTotalScan);
        cvPreviousScan = findViewById(R.id.cvPreviousScan);
        cvAppointment = findViewById(R.id.cvAppointment);
        cvGeolocation = findViewById(R.id.cvGeolocation);
        cvInformation = findViewById(R.id.cvInformation);
        cvSupport = findViewById(R.id.cvSupport);
        cvMessage = findViewById(R.id.cvMessage);
        cvScanHistory = findViewById(R.id.cvScanHistory);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        ivAppointmentIcon = findViewById(R.id.ivAppointmentIcon);
        ivGeolocationIcon = findViewById(R.id.ivGeolocationIcon);
        ivInformationIcon = findViewById(R.id.ivInformationIcon);
        ivSupportIcon = findViewById(R.id.ivSupportIcon);
        ivMessageIcon = findViewById(R.id.ivMessageIcon);
        ivScanHistoryIcon = findViewById(R.id.ivScanHistoryIcon);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvLastScanLabel = findViewById(R.id.tvLastScanLabel);
        tvLastScanDays = findViewById(R.id.tvLastScanDays);
        tvTotalScanLabel = findViewById(R.id.tvTotalScanLabel);
        tvTotalScanCount = findViewById(R.id.tvTotalScanCount);
        tvPreviousScanDetails = findViewById(R.id.tvPreviousScanDetails);
        tvAppointmentLabel = findViewById(R.id.tvAppointmentLabel);
        tvGeolocationLabel = findViewById(R.id.tvGeolocationLabel);
        tvInformationLabel = findViewById(R.id.tvInformationLabel);
        tvSupportLabel = findViewById(R.id.tvSupportLabel);
        tvMessageLabel = findViewById(R.id.tvMessageLabel);
        tvScanHistoryLabel = findViewById(R.id.tvScanHistoryLabel);

        btnAnalyzeSkinDisease = findViewById(R.id.btnAnalyzeSkinDisease);
    }

    private void setOnClickListeners() {
        cvScanHistory.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivityHistory.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        llScanHistoryContent.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivityHistory.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        ivScanHistoryIcon.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivityHistory.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        tvScanHistoryLabel.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivityHistory.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        ivAppointmentIcon.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivitySetAppointment.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        tvAppointmentLabel.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivitySetAppointment.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        llAppointmentContent.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivitySetAppointment.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        btnAnalyzeSkinDisease.setOnClickListener(v -> {
            Intent intentScan = new Intent(ActivityDashboard.this, ActivityCamera.class);
            startActivity(intentScan);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        ivGeolocationIcon.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivityGeoLocation.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        ivMessageIcon.setOnClickListener(v -> {
            Intent intentHistory = new Intent(ActivityDashboard.this, ActivityTelemedicine.class);
            startActivity(intentHistory);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    private void loadImageFromUrl(String url, ImageView imageView) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_placeholder) // Optional placeholder
                .into(imageView);
    }

    private void loadUserData(String name, String email, String phone) {
        tvUserName.setText(name);
        tvUserEmail.setText(email);
        tvUserPhone.setText(phone);
    }

    private void loadUserAnalytics(String lastScan, int totalScan, String previousScan) {
        tvLastScanDays.setText(lastScan);
        tvTotalScanCount.setText(String.valueOf(totalScan));
        tvPreviousScanDetails.setText(previousScan);
    }

    private void makeHTTPRequest(int patientID) {
        // Define keys for the JSON request body
        String keyPatientID = "patientID";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Populate the JSON request body
        try {
            requestBody.put(keyPatientID, patientID);
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
            String difference = response.getString("difference");
            int amount = response.getInt("amount");
            int total = response.getInt("totalRows");
            String skinDisease = response.getString("skinDiseaseName");

            // String Manipulations
            String total_amount = amount + " " + difference;
            String previous_scan = "Previous scan was " + skinDisease;

            if (success) {
                // Login successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);
                Log.d(TAG + " onRequestSuccess", "Difference Received: " + difference);
                Log.d(TAG + " onRequestSuccess", "Difference Amount Received: " + amount);
                Log.d(TAG + " onRequestSuccess", "Skin Disease Received: " + skinDisease);
                loadUserAnalytics(total_amount, total, previous_scan);
            } else {
                // Login failed
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
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