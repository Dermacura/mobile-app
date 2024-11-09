package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AdapterSMS;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.models.ModelSMS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivitySMS extends AppCompatActivity {

    private RecyclerView rvHistory;
    private AdapterSMS adapterSMS;
    private List<ModelSMS> messageList;
    private static final String TAG = "ActivitySMS";
    private MaterialButton btnNewContact;
    private RequestQueue queue;
    private SwipeRefreshLayout swipeRefreshLayout; // Declare SwipeRefreshLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnNewContact = findViewById(R.id.btnContinue);
        btnNewContact.setOnClickListener(v -> {
            // Create an intent to start the ActivityNewContact
            Intent intentNewContact = new Intent(ActivitySMS.this, ActivityContacts.class);

            // Start the new contact activity
            startActivity(intentNewContact);
        });

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData); // Set the listener to call refreshData()

        // Initialize RecyclerView
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // Initialize message list
        messageList = new ArrayList<>();

        // Initialize adapter
        adapterSMS = new AdapterSMS(this, messageList);
        rvHistory.setAdapter(adapterSMS);

        // Get the shared preferences and user data
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();
        int userId = userData.getPatientID();

        // Initialize the request queue
        queue = Volley.newRequestQueue(this);

        // Fetch data from the PHP API
        fetchMessagesData(userId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests when the activity is destroyed to prevent memory leaks
        if (queue != null) {
            queue.cancelAll(TAG);  // Use the TAG to cancel all requests in the queue
        }
    }

    private void fetchMessagesData(int userId) {
        // URL for the PHP API
        String URL = "https://backend.dermocura.net/android/fetchmessage.php"; // Replace with your actual API endpoint

        // Create a JSON object for the request body (you can pass patientID or other parameters if needed)
        JSONObject requestBody = new JSONObject();
        try {
            // Set parameters (for example, if patientID is required)
            requestBody.put("patientID", userId); // Pass the actual patientID as needed
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,  // The body with the patientID or any required data
                this::onRequestSuccess, // Success listener
                this::onRequestError // Error listener
        );

        // Add a TAG to the request so it can be cancelled later
        request.setTag(TAG);

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void refreshData() {
        // Get the shared preferences and user data
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();
        int userId = userData.getPatientID();

        // Fetch the data again
        fetchMessagesData(userId);
    }

    private void onRequestSuccess(JSONObject response) {
        swipeRefreshLayout.setRefreshing(false); // Stop the refresh indicator
        try {
            // Extract success status from the JSON response
            boolean success = response.getBoolean("success");

            if (success) {
                // Extract the messages data from the 'data' array
                JSONArray messagesData = response.getJSONArray("data");
                messageList.clear(); // Clear any existing data

                // Loop through each message object
                for (int i = 0; i < messagesData.length(); i++) {
                    JSONObject message = messagesData.getJSONObject(i);

                    // Extract message details
                    int telemedicineID = message.getInt("telemedicineID");
                    int userAccID = message.getInt("userAccID"); // Get userAccID
                    String tempFullName = message.getString("temp_fullname");
                    String telemedicineContent = message.getString("telemedicineContent");
                    String formattedDate = message.getString("formattedDate");
                    String profile = message.getString("profile"); // Get profile

                    // Add message to the messageList
                    messageList.add(new ModelSMS(telemedicineID, userAccID, tempFullName, telemedicineContent, formattedDate, profile));
                }

                // Notify the adapter of data changes
                adapterSMS.notifyDataSetChanged();

                // Log success
                Log.d(TAG, "Messages fetched successfully.");

            } else {
                // Handle failure case
                String message = response.getString("message");
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + message);
            }

        } catch (JSONException e) {
            // Handle JSON parsing exceptions
            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
            Toast.makeText(this, "Parsing error", Toast.LENGTH_LONG).show();
        }
    }

    private void onRequestError(VolleyError error) {
        swipeRefreshLayout.setRefreshing(false); // Stop the refresh indicator
        // Log and handle the error
        Log.e(TAG, "Error Response: " + error.getMessage());
        Toast.makeText(this, "Error fetching data", Toast.LENGTH_LONG).show();
    }
}

