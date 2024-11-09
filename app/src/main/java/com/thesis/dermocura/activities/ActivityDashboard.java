package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityDashboard extends AppCompatActivity {

    // Declare Views
    private TextView greetingText, usernameText, lastScanDaysText, totalScanCountText,
            appointmentDateText, doctorNameText, doctorSpecialtyText, appointmentTimeText, view_all_user_info_text, tvAppointmentLabel, tvGeolocationLabel;

    private ImageView doctorImageView, camera_button, messages_button, location_button, appointment_button, profile_picture, ivAppointmentIcon, ivGeolocationIcon;

    private LinearLayout scanInfoLayout, llAppointmentInformation, appointment_header;

    private ScrollView mainScrollView;

    private String username;

    private View divider_appointment;

    // Declare new CardViews for the additional features
    private CardView cvNewFeature1, cvNewFeature2, cvInformation;

    // Declare Strings
    private static final String TAG = "ActivityDashboard";
    private static final String DASHBOARD_URL = "https://backend.dermocura.net/android/userdashboard.php";

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

        // Initialize views
        initializeViews();

        // Retrieve user data from shared preferences
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();

        if (userData != null) {
            username = userData.getPatientName();
            int userId = userData.getPatientID();
            String profileImageUrl = userData.getPatientImageURL();

            // Set user data in UI
            setUserDataInView(profileImageUrl);

            // Make HTTP request to fetch dashboard data
            fetchDashboardData(userId);
        } else {
            Log.e(TAG, "UserData is null. Unable to retrieve user information.");
            // Handle the case when user data is null, e.g., navigate to login screen
        }

        // Set up buttons
        setObjects();
    }

    private void initializeViews() {
        // Initialize views that are being used
        greetingText = findViewById(R.id.greeting_text);
        usernameText = findViewById(R.id.username_text);
        lastScanDaysText = findViewById(R.id.tvLastScanDays);
        totalScanCountText = findViewById(R.id.tvTotalScanCount);
        appointmentDateText = findViewById(R.id.tvAppointmentDate);
        doctorNameText = findViewById(R.id.tvDoctorName);
        doctorSpecialtyText = findViewById(R.id.tvDoctorSpecialty);
        appointmentTimeText = findViewById(R.id.tvAppointmentTime);
        view_all_user_info_text = findViewById(R.id.view_all_user_info_text);
        tvAppointmentLabel = findViewById(R.id.tvAppointmentLabel);
        tvGeolocationLabel = findViewById(R.id.tvGeolocationLabel);

        doctorImageView = findViewById(R.id.ivDoctorImage);
        camera_button = findViewById(R.id.camera_button);
        messages_button = findViewById(R.id.messages_button);
        location_button = findViewById(R.id.location_button);
        appointment_button = findViewById(R.id.appointment_button);
        profile_picture = findViewById(R.id.profile_picture);
        ivAppointmentIcon = findViewById(R.id.ivAppointmentIcon);
        ivGeolocationIcon = findViewById(R.id.ivGeolocationIcon);

        scanInfoLayout = findViewById(R.id.llScanInfo);
        llAppointmentInformation = findViewById(R.id.llAppointmentInformation);
        appointment_header = findViewById(R.id.appointment_header);

        mainScrollView = findViewById(R.id.main_scrollview);

        divider_appointment = findViewById(R.id.divider_appointment);

        // Initialize the new CardViews
        cvNewFeature1 = findViewById(R.id.cvNewFeature1);
        cvNewFeature2 = findViewById(R.id.cvNewFeature2);
        cvInformation = findViewById(R.id.cvInformation);
    }

    private void setObjects() {
        // Setting objects values
        camera_button.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityCamera.class);
            startActivity(intent);
        });

        messages_button.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivitySMS.class);
            startActivity(intent);
        });

        location_button.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityGeoLocation.class);
            startActivity(intent);
        });

        view_all_user_info_text.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityHistory.class);
            startActivity(intent);
        });

        appointment_button.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityAppointment.class);
            startActivity(intent);
        });

        profile_picture.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityProfile.class);
            startActivity(intent);
        });

        ivAppointmentIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityAppointmentList.class);
            startActivity(intent);
        });

        tvAppointmentLabel.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityAppointmentList.class);
            startActivity(intent);
        });

        ivGeolocationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivitySMS.class);
            startActivity(intent);
        });

        tvGeolocationLabel.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivitySMS.class);
            startActivity(intent);
        });

        // Set click listeners for the new feature cards
        cvNewFeature1.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityHistory.class);
            startActivity(intent);
        });

        cvNewFeature2.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityCamera.class);
            startActivity(intent);
        });

        cvInformation.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityDashboard.this, ActivityGeoLocation.class);
            startActivity(intent);
        });
    }

    private void setUserDataInView(String profileImageUrl) {
        if (username != null && !username.isEmpty()) {
            Log.i(TAG, "Setting username: " + username);
            usernameText.setText(username);
        } else {
            Log.w(TAG, "Username is null or empty");
        }

        // Load profile image
        loadImageFromUrl(profileImageUrl, findViewById(R.id.profile_picture));
    }

    private void fetchDashboardData(int userId) {
        // Create JSON request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", userId);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON request body", e);
            return;
        }

        // Send HTTP request
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                DASHBOARD_URL,
                requestBody,
                this::handleResponse,
                this::handleError
        );

        queue.add(request);
    }

    private void handleResponse(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            if (success) {
                // Extract values from the response
                String lastScan = response.getString("timeDifference");
                String lastResult = response.getString("skinDiseaseName");
                String appointmentDate = response.optString("appointmentSchedule");
                String doctorName = response.optString("doctorName");
                String doctorSpecialty = response.optString("doctorSpecialty");
                String appointmentTime = response.optString("availabilityTime");
                String doctorImage = response.optString("doctorImage");

                // Log the received values
                Log.d(TAG, "Received Data:");
                Log.d(TAG, "Last Scan: " + lastScan);
                Log.d(TAG, "Last Result: " + lastResult);
                Log.d(TAG, "Appointment Date: " + appointmentDate);
                Log.d(TAG, "Doctor Name: " + doctorName);
                Log.d(TAG, "Doctor Specialty: " + doctorSpecialty);
                Log.d(TAG, "Appointment Time: " + appointmentTime);
                Log.d(TAG, "Doctor Image: " + doctorImage);

                // Check if any critical fields are null
                if (appointmentDate.equals("null")) {
                    // Show a toast message if any of the important fields are null
                    Log.w(TAG, "No upcoming appointments found.");
                } else {
                    String doctorProfileImageUrl = "https://backend.dermocura.net/images/doctor_profile/" + doctorImage;
                    // Update UI with retrieved data
                    updateUI(lastScan, lastResult, appointmentDate, doctorName, doctorSpecialty, appointmentTime, doctorProfileImageUrl);
                }
            } else {
                Log.e(TAG, "Dashboard data retrieval failed: " + response.getString("message"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse dashboard data", e);
        }
    }

    private void handleError(VolleyError error) {
        Log.e(TAG, "Error retrieving dashboard data", error);
    }

    private void appointmentList() {
        Intent intent = new Intent(ActivityDashboard.this, ActivityAppointmentList.class);
        startActivity(intent);
    }

    private void updateUI(String lastScan, String lastResult, String appointmentDate,
                          String doctorName, String doctorSpecialty, String appointmentTime,
                          String doctorProfileImageUrl) {
        // Update TextViews
        Log.i(TAG, "username = " + username);
        usernameText.setText(username);
        lastScanDaysText.setText(lastScan);
        totalScanCountText.setText(lastResult);
        appointmentDateText.setText(appointmentDate);
        doctorNameText.setText(doctorName);
        doctorSpecialtyText.setText(doctorSpecialty);
        appointmentTimeText.setText(appointmentTime);

        // Load doctor image
        loadImageFromUrl(doctorProfileImageUrl, doctorImageView);
    }

    private void loadImageFromUrl(String url, ImageView imageView) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_placeholder)
                .into(imageView);
    }

    private void showNoAppointment() {
        divider_appointment.setVisibility(View.VISIBLE);
        appointment_header.setVisibility(View.VISIBLE);
        llAppointmentInformation.setVisibility(View.VISIBLE);
    }
}
