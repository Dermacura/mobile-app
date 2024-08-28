package com.thesis.dermocura.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AdapterRecommendation;
import com.thesis.dermocura.classes.ClassBase64Convert;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.ScanData;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.models.ModelRecommendation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityScanSummary extends AppCompatActivity {

    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvInformation;
    ImageView ivPreview;
    LinearLayout llHeader;
    CardView cvPreviewFrame;
    MaterialButton btnContinue;
    RecyclerView recommendation_recycler_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Methods
        initializeObjects();
        setOnClickListeners();
        fetchIntentExtra();
        displayImage();

    }

    private void initializeObjects() {
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvInformation = findViewById(R.id.tvInformation);
        ivPreview = findViewById(R.id.ivPreview);
        llHeader = findViewById(R.id.llHeader);
        cvPreviewFrame = findViewById(R.id.cvPreviewFrame);
        btnContinue = findViewById(R.id.btnContinue);
        recommendation_recycler_view = findViewById(R.id.recommendation_recycler_view);
    }

    private void setOnClickListeners() {
        // Clickable Material buttons
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityScanSummary.this, NewDashboard.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchIntentExtra() {
        Intent intent = getIntent();
        String skin_disease = intent.getStringExtra("description");
        String recommendation = intent.getStringExtra("recommendation");
        String treatment_suggestion = intent.getStringExtra("treatment");
        String symptoms = intent.getStringExtra("symptoms");

        setInformationCards(skin_disease, recommendation, treatment_suggestion, symptoms);
    }

    private void setInformationCards(String skinDisease, String recommendation, String treatmentSuggestion, String symptoms) {
        // Set LayoutManager to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recommendation_recycler_view.setLayoutManager(layoutManager);

        // Create a list of ModelRecommendation objects with different titles and content
        List<ModelRecommendation> recommendations = new ArrayList<>();
        recommendations.add(new ModelRecommendation("Skin Disease", skinDisease));
        recommendations.add(new ModelRecommendation("Symptoms", treatmentSuggestion));
        recommendations.add(new ModelRecommendation("Recommendation", recommendation));
        recommendations.add(new ModelRecommendation("Treatment Suggestion", treatmentSuggestion));

        // Set the Adapter
        AdapterRecommendation adapter = new AdapterRecommendation(this, recommendations);
        recommendation_recycler_view.setAdapter(adapter);

        // Attach LinearSnapHelper to RecyclerView
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recommendation_recycler_view);
    }

    private void displayImage() {
        ScanData scanData = ScanData.getInstance();
        Uri imageUri = scanData.getImageUri();

        if (imageUri != null) {
            String imageUrl = imageUri.toString();
            loadImageFromUrl(imageUrl, ivPreview);
        } else {
            Log.d("displayImage", "No imageUri available");
        }
    }

    private void loadImageFromUrl(String url, ImageView imageView) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_placeholder)
                .into(imageView);
    }
}