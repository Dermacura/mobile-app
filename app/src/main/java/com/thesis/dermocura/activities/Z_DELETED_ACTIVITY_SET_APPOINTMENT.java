package com.thesis.dermocura.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.thesis.dermocura.adapters.AdapterDoctor;
import com.thesis.dermocura.models.ModelDoctor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Z_DELETED_ACTIVITY_SET_APPOINTMENT extends AppCompatActivity {

    // Declare the variables
    LinearLayout llHeader;
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle;
    RecyclerView rvList;

    private AdapterDoctor adapter;
    private RecyclerView recyclerView;
    private List<ModelDoctor> modelDoctorList;

    private static final String TAG = "ActivityHistory";
    private static final String URL = "https://backend.dermocura.net/android/fetchavailabledoctor.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.z_deleted_activity_set_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI components
        initializeObjects();

        // Initialize the data list and adapter
        modelDoctorList = new ArrayList<>();
        adapter = new AdapterDoctor(this, modelDoctorList);

        // Set the layout manager and adapter
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);

        // Make the HTTP request to fetch data
        makeHTTPRequest();
    }

    private void initializeObjects() {
        llHeader = findViewById(R.id.llHeader);
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        rvList = findViewById(R.id.rvList);
    }

    private void makeHTTPRequest() {
        // Create an empty JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                this::onRequestSuccess, // handle successful response
                this::onRequestError    // handle error response
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

            if (success) {
                // Login successful
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Extract the data array from the response
                JSONArray dataArray = response.getJSONArray("data");

                // Clear the current list to avoid duplications
                modelDoctorList.clear();

                // Iterate through the data array and populate the RecyclerView
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject jsonObject = dataArray.getJSONObject(i);

                    int doctorID = jsonObject.getInt("doctorID");
                    String doctorName = jsonObject.getString("doctorName");
                    String doctorEmail = jsonObject.getString("doctorEmail");
                    String doctorImageURL = jsonObject.getString("doctorImage");

                    // Create a new ModelHistory object and add it to the list
                    ModelDoctor modelDoctor = new ModelDoctor(doctorID, doctorName, doctorEmail, doctorImageURL);
                    modelDoctorList.add(modelDoctor);
                }

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
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
        // Log and handle the error
        Log.e(TAG + " onFetchError", "Error Response: " + error.getMessage());
    }
}