package com.thesis.dermocura.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AdapterHistory;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.models.ModelHistory;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityHistory extends AppCompatActivity {

    LinearLayout llHeader;
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvTitle, tvSubTitle;
    RecyclerView rvHistory;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LoadingDialogFragment loadingDialogFragment; // Loading dialog instance

    private AdapterHistory adapter;
    private List<ModelHistory> modelHistoryList;

    private static final String TAG = "ActivityHistory";
    private static final String URL = "https://backend.dermocura.net/android/patientrecords.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::makeHTTPRequest); // Set pull-to-refresh action

        // Initialize RecyclerView and set reversed layout
        rvHistory = findViewById(R.id.rvHistory);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true); // Reverse the order
        layoutManager.setStackFromEnd(true); // Stack the latest items from the end (bottom)
        rvHistory.setLayoutManager(layoutManager);

        // Initialize the data list and adapter
        modelHistoryList = new ArrayList<>();
        adapter = new AdapterHistory(this, modelHistoryList);
        rvHistory.setAdapter(adapter);

        // Initialize other views
        initializeObjects();

        // Initialize the loading dialog fragment
        loadingDialogFragment = new LoadingDialogFragment();

        // Fetch data initially
        makeHTTPRequest();
    }

    private void initializeObjects() {
        llHeader = findViewById(R.id.llHeader);
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
    }

    private void makeHTTPRequest() {
        // Show the loading dialog when starting a new request
        showLoadingDialog();

        // Define keys for the JSON request body
        String keyPatientID = "patientID";
        MySharedPreferences mySharedPreferences = MySharedPreferences.getInstance(this);
        UserData userData = mySharedPreferences.getUserData();
        int patientID = userData.getPatientID();

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

            // Dismiss the loading dialog when data is fetched
            dismissLoadingDialog();

            // Stop the swipe refresh animation
            swipeRefreshLayout.setRefreshing(false);

            if (success) {
                // Log success
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Extract the data array from the response
                JSONArray dataArray = response.optJSONArray("data");

                // Check if dataArray is null or empty
                if (dataArray == null || dataArray.length() == 0) {
                    // Show a toast if no records are found
                    Toast.makeText(this, "No records found for the patient", Toast.LENGTH_SHORT).show();
                    return; // Exit the method as there's no data to display
                }

                // Clear the current list to avoid duplications
                modelHistoryList.clear();

                // Iterate through the data array and populate the RecyclerView
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject jsonObject = dataArray.getJSONObject(i);

                    int skinDiseaseID = jsonObject.getInt("skinDiseaseID");
                    String skinDiseaseName = jsonObject.getString("skinDiseaseName");
                    String skinDiseaseImageURL = jsonObject.getString("patientScanImage");
                    String patientAnalyzedDate = jsonObject.getString("patientAnalyzedDate");

                    // Create a new ModelHistory object and add it to the list
                    ModelHistory model = new ModelHistory(skinDiseaseID, skinDiseaseName, skinDiseaseImageURL, patientAnalyzedDate);
                    modelHistoryList.add(model);
                }

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            } else {
                // Log failure and show a toast message with the error message from the response
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            // Handle JSON parsing errors
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }


    private void onRequestError(VolleyError error) {
        // Log and handle the error
        Log.e(TAG + " onFetchError", "Error Response: " + error.getMessage());

        // Dismiss the loading dialog in case of an error
        dismissLoadingDialog();

        // Stop the swipe refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showLoadingDialog() {
        // Check if the loading dialog is already added or visible
        if (loadingDialogFragment != null && !loadingDialogFragment.isAdded() && !loadingDialogFragment.isVisible()) {
            try {
                loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error showing loading dialog: " + e.getMessage());
            }
        }
    }

    private void dismissLoadingDialog() {
        // Check if the loading dialog is currently managed by FragmentManager before dismissing
        if (loadingDialogFragment != null && loadingDialogFragment.isAdded()) {
            try {
                loadingDialogFragment.dismissAllowingStateLoss();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error dismissing loading dialog: " + e.getMessage());
            }
        }
    }
}
