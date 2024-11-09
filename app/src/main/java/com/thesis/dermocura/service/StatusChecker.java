package com.thesis.dermocura.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public class StatusChecker {

    private static final String TAG = "StatusChecker";
    private Context context;
    private int patientID;
    private SharedPreferences sharedPreferences;
    private static final String APPOINTMENT_STATUS_KEY = "appointmentStatus_";

    public StatusChecker(Context context, int patientID) {
        this.context = context;
        this.patientID = patientID;
        this.sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
    }

    // Method to check for status updates
    public void checkForStatusUpdates() {
        Log.d(TAG, "Polling server for status updates...");

        // Define keys for the JSON request body
        String keyPatientID = "patientID";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(keyPatientID, patientID);  // Only send patientID
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body: " + e.getMessage());
            return;
        }

        // Create the Volley request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Create the POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://backend.dermocura.net/android/notification/appointments.php", // Update with your actual URL
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    // On successful request response
    private void onRequestSuccess(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            if (success) {
                Log.d(TAG, "Status updates retrieved successfully: " + response.toString());

                JSONArray appointments = response.getJSONArray("appointments");
                for (int i = 0; i < appointments.length(); i++) {
                    JSONObject appointment = appointments.getJSONObject(i);
                    int appointmentID = appointment.getInt("appointmentID");
                    int currentStatus = appointment.getInt("status");
                    String remarkInput = appointment.optString("remarkInput", "");  // Retrieve remarkInput (if any)

                    // Compare the current status with the last known status
                    int lastKnownStatus = getLastKnownStatus(appointmentID);

                    if (currentStatus != lastKnownStatus) {
                        // If the status has changed, show a notification
                        String statusMessage = getStatusMessage(currentStatus, remarkInput);
                        if (statusMessage != null) {
                            showNotification("Appointment Status Update", statusMessage, appointmentID);
                        }

                        // Update the stored status in SharedPreferences
                        saveLastKnownStatus(appointmentID, currentStatus);
                    }
                }
            } else {
                Log.d(TAG, "No status updates: " + response.getString("message"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
        }
    }

    // Handle request error
    private void onRequestError(VolleyError error) {
        Log.e(TAG, "Error in network request: " + error.getMessage(), error);
        if (error.networkResponse != null) {
            Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
        }
        Toast.makeText(context, "Network error: Unable to fetch status updates", Toast.LENGTH_LONG).show();
    }

    // Retrieve the last known status of the appointment from SharedPreferences
    private int getLastKnownStatus(int appointmentID) {
        return sharedPreferences.getInt(APPOINTMENT_STATUS_KEY + appointmentID, 0);  // Default to 0 (Pending)
    }

    // Save the current status of the appointment to SharedPreferences
    private void saveLastKnownStatus(int appointmentID, int currentStatus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(APPOINTMENT_STATUS_KEY + appointmentID, currentStatus);
        editor.apply();
    }

    // Map status values to notification messages, including remarkInput for declined appointments
    private String getStatusMessage(int status, String remarkInput) {
        switch (status) {
            case 1:
                return "An Appointment has been accepted.";
            case 2:
                return "An Appointment has been declined due to " + remarkInput;  // Include remarkInput for declined
            case 0:
            default:
                return null;  // We don't notify if the status is still pending (0)
        }
    }

    // Show a local notification when a status change is detected
    private void showNotification(String title, String content, int notificationId) {
        Log.d(TAG, "Showing notification for status change.");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "status_channel";
            NotificationChannel channel = new NotificationChannel(channelId, "Status Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "status_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }
}

