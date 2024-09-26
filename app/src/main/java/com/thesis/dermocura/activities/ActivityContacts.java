package com.thesis.dermocura.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AdapterContacts;
import com.thesis.dermocura.models.ModelContacts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityContacts extends AppCompatActivity {

    private RecyclerView rvHistory;
    private AdapterContacts adapterContacts;
    private List<ModelContacts> contactsList;
    private String TAG = "ActivityContacts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contacts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView and List
        rvHistory = findViewById(R.id.rvHistory);
        contactsList = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView
        adapterContacts = new AdapterContacts(this, contactsList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapterContacts);

        // Fetch the users data from the server
        fetchUsersData();
    }

    private void fetchUsersData() {
        // URL for the PHP API
        String URL = "https://backend.dermocura.net/android/fetchcontacts.php"; // Replace with your actual API endpoint

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JSON object for the request body (you can pass parameters if needed)
        JSONObject requestBody = new JSONObject();

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,  // Empty body or you can add parameters if needed
                response -> onRequestSuccess(response), // Success listener
                error -> onRequestError(error) // Error listener
        );

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void onRequestSuccess(JSONObject response) {
        try {
            // Extract success status from the JSON response
            boolean success = response.getBoolean("success");

            if (success) {
                // Extract the users' data from the 'data' array
                JSONArray usersData = response.getJSONArray("data");
                contactsList.clear(); // Clear any existing data

                // Loop through each user object
                for (int i = 0; i < usersData.length(); i++) {
                    JSONObject user = usersData.getJSONObject(i);

                    Log.d("JSONArrayLog", "User " + i + ": " + user.toString());

                    // Extract user details
                    int userAccID = user.getInt("userAccID");
                    String fullname = user.isNull("fullname") ? null : user.getString("fullname");
                    String clinicName = user.isNull("clinicName") ? null : user.getString("clinicName");
                    String profile = user.getString("profile");

                    // Add user to the contactsList
                    contactsList.add(new ModelContacts(userAccID, fullname, clinicName, profile));
                }

                // Notify the adapter that data has changed
                adapterContacts.notifyDataSetChanged();

                // Log success
                Log.d(TAG + " onRequestSuccess", "Users fetched successfully.");

            } else {
                // Handle failure case
                String message = response.getString("message");
                Log.e(TAG + " onRequestSuccess", "Error: " + message);
            }

        } catch (JSONException e) {
            // Handle JSON parsing exceptions
            Log.e(TAG + " onRequestSuccess", "JSON Parsing error: " + e.getMessage());
        }
    }

    private void onRequestError(VolleyError error) {
        // Log and handle the error
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
    }

}
