package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AdapterRecommendation;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.models.ModelRecommendation;
import com.thesis.dermocura.utils.LoadingDialogFragment;
import com.thesis.dermocura.datas.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityDiseaseInfo extends AppCompatActivity {

    private static final String TAG = "ActivityDiseaseInfo";
    private static final String URL = "https://backend.dermocura.net/android/analysis/fetch_disease_properties.php"; // Replace with your actual API URL
    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvInformation, tvOverlayText;
    ImageView ivPreview;
    LinearLayout llHeader;
    RecyclerView recommendation_recycler_view;
    AdapterRecommendation adapterRecommendation;
    List<ModelRecommendation> recommendations;
    MaterialButton btnContinue;

    // Loading dialog instance
    private LoadingDialogFragment loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_info);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the loading dialog
        loadingDialog = new LoadingDialogFragment();

        // Show loading dialog when activity starts
        showLoadingDialog();

        // Initialize UI elements
        initializeObjects();

        // Fetch skinDiseaseID and confidence from Intent Extra
        Intent intent = getIntent();
        String skinDiseaseID = intent.getStringExtra("skinDiseaseID");
        double confidence = intent.getDoubleExtra("confidence", -1);  // Default to -1 if no confidence is provided

        // Conditionally update tvInformation based on confidence
        if (confidence != -1) {
            tvInformation.setText(String.format("With an AI confidence of %.0f%%, this result provides valuable insights. For personalized guidance, a visit to a healthcare professional is always a great choice.", confidence * 100));
        }

        if (skinDiseaseID != null && !skinDiseaseID.isEmpty()) {
            int diseaseID = Integer.parseInt(skinDiseaseID);
            fetchDiseaseData(diseaseID);
        } else {
            Toast.makeText(this, "No valid skinDiseaseID provided", Toast.LENGTH_LONG).show();
            Log.e(TAG, "No valid skinDiseaseID found in Intent");
        }
    }

    private void initializeObjects() {
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvInformation = findViewById(R.id.tvInformation);
        tvOverlayText = findViewById(R.id.tvOverlayText); // Overlay TextView to set disease name
        ivPreview = findViewById(R.id.ivPreview);
        llHeader = findViewById(R.id.llHeader);
        recommendation_recycler_view = findViewById(R.id.recommendation_recycler_view);
        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityDashboard.class);
            startActivity(intent);
            finish();
        });

        // Initialize RecyclerView and Adapter
        recommendations = new ArrayList<>();
        adapterRecommendation = new AdapterRecommendation(this, recommendations);
        recommendation_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendation_recycler_view.setAdapter(adapterRecommendation);
        new LinearSnapHelper().attachToRecyclerView(recommendation_recycler_view);
    }

    // Method to make the HTTP request using Volley
    private void fetchDiseaseData(int skinDiseaseID) {
        Log.d(TAG, "Sending skinDiseaseID: " + skinDiseaseID);

        // Retrieve patientID from SharedPreferences
        MySharedPreferences sharedPreferences = MySharedPreferences.getInstance(this);
        UserData userData = sharedPreferences.getUserData();
        if (userData == null) {
            Toast.makeText(this, "No patient data found", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "UserData is null in SharedPreferences");
            dismissLoadingDialog();
            return;
        }

        int patientID = userData.getPatientID();  // Retrieve patientID from UserData
        Log.d(TAG, "Retrieved patientID from SharedPreferences: " + patientID);

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("skinDiseaseID", skinDiseaseID); // Sending skinDiseaseID in request
            requestBody.put("patientID", patientID);         // Sending patientID in request
        } catch (JSONException e) {
            Log.e(TAG + " fetchDiseaseData", "Failed to create request body", e);
            return;
        }

        // Initialize Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a POST request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        Log.i(TAG + " fetchDiseaseData", requestBody.toString());

        // Add the request to the queue
        queue.add(request);
    }

    // Success handler for the Volley request
    private void onRequestSuccess(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                JSONObject data = response.getJSONObject("data");
                String diseaseName = data.getString("skinDiseaseName");
                String imageURL = data.getString("skinDiseaseImageURL"); // Image URL from API response
                JSONArray properties = data.getJSONArray("properties");

                tvOverlayText.setText(diseaseName);  // Set the disease name

                // Load the image into ivPreview using Glide with blur and opacity
                Glide.with(this)
                        .load(imageURL)
                        .apply(RequestOptions.bitmapTransform(new jp.wasabeef.glide.transformations.BlurTransformation(25)))
                        .placeholder(R.drawable.default_placeholder)
                        .error(R.drawable.default_placeholder)
                        .into(ivPreview);

                ivPreview.setAlpha(0.5f);  // Set opacity to 50%

                // Clear existing recommendations
                recommendations.clear();

                for (int i = 0; i < properties.length(); i++) {
                    JSONObject property = properties.getJSONObject(i);
                    String title = property.getString("sdiProperty");
                    String content = property.getString("sdiValue");

                    // Add the property and value to the list
                    recommendations.add(new ModelRecommendation(title, content));
                }

                adapterRecommendation.notifyDataSetChanged();  // Notify the adapter of data change

                Log.d(TAG + " onRequestSuccess", "Data fetched and displayed successfully");
            } else {
                Log.e(TAG + " onRequestSuccess", "Failed to fetch data: " + message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", "Failed to parse response", e);
        } finally {
            dismissLoadingDialog();  // Dismiss the loading dialog when request completes
        }
    }

    // Error handler for the Volley request
    private void onRequestError(VolleyError error) {
        Log.e(TAG + " onRequestError", "Volley request error: " + error.getMessage());
        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
        dismissLoadingDialog();  // Dismiss the loading dialog on error
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
