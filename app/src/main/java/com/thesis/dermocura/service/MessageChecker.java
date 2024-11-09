package com.thesis.dermocura.service;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageChecker {

    private static final String TAG = "MessageChecker";
    private Context context;
    private int patientID;
    private String lastCheckedTime;

    public MessageChecker(Context context, int patientID) {
        this.context = context;
        this.patientID = patientID;
        this.lastCheckedTime = getCurrentTime();  // Initialize with current time
    }

    // Method to check for new messages using POST
    public void checkForNewMessages() {
        Log.d(TAG, "Polling server for new messages...");

        // Define keys for the JSON request body
        String keyPatientID = "patientID";
        String keyLastCheckedTime = "lastCheckedTime";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(keyPatientID, patientID);
            requestBody.put(keyLastCheckedTime, lastCheckedTime);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body: " + e.getMessage());
            return;
        }

        // Create the Volley request queue
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // Create the POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "https://backend.dermocura.net/android/notification/message.php", // Update with your actual URL
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        // Log the JSON request body for debugging
        Log.d(TAG, "Request Body: " + requestBody.toString());

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    // On successful request response
    private void onRequestSuccess(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            if (success) {
                Log.d(TAG, "Messages retrieved successfully: " + response.toString());

                JSONArray messages = response.getJSONArray("messages");
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject message = messages.getJSONObject(i);
                    String messageContent = message.getString("telemedicineContent");
                    Log.d(TAG, "Message Content: " + messageContent);

                    // Show notification for each message
                    showNotification("New Message", messageContent, message.getInt("telemedicineID"));
                }

                // Update lastCheckedTime to the current time after fetching new messages
                lastCheckedTime = getCurrentTime();
                Log.d(TAG, "Updated lastCheckedTime to: " + lastCheckedTime);
            } else {
                Log.d(TAG, "No new messages: " + response.getString("message"));
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
        // Show an in-app notification of the error
        Toast.makeText(context, "Network error: Unable to fetch messages", Toast.LENGTH_LONG).show();
    }

    // Helper method to get the current timestamp
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Show a local notification when a new message is detected
    private void showNotification(String title, String content, int notificationId) {
        Log.d(TAG, "Showing notification with content: " + content);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "message_channel";
            NotificationChannel channel = new NotificationChannel(channelId, "Message Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "message_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_email)  // Use a default Android icon for now
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Keep high priority for new messages
                .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }
}
