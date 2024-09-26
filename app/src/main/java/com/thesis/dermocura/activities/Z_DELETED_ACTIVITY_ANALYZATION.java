package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.datas.SkinDiseaseData;

public class Z_DELETED_ACTIVITY_ANALYZATION extends AppCompatActivity {

    ImageButton ibLeftArrow, ibRightArrow;
    TextView tvPageTitle, tvHeadText, tvBodyText;
    ImageView ivPreview;
    LinearLayout llHeader;
    MaterialButton btnContinue;
    CardView cvPreviewFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.z_deleted_activity_analyzation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeObjects();
        setOnClickListeners();
        setObjects();
    }

    private void initializeObjects() {
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);

        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvHeadText = findViewById(R.id.tvHeadText);
        tvBodyText = findViewById(R.id.tvBodyText);

        ivPreview = findViewById(R.id.ivPreview);

        llHeader = findViewById(R.id.llHeader);

        btnContinue = findViewById(R.id.btnContinue);

        cvPreviewFrame = findViewById(R.id.cvPreviewFrame);
    }

    private void setOnClickListeners() {
        btnContinue.setOnClickListener(v -> {
            SkinDiseaseData skinDiseaseData = SkinDiseaseData.getInstance();
            skinDiseaseData.clearData();
            Intent intentRecommendation = new Intent(this, Z_DELETED_ACTIVITY_DASHBOARD.class);
            startActivity(intentRecommendation);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        ibLeftArrow.setOnClickListener(v -> {
            Intent intentLeft = new Intent(this, Z_DELETED_ACTIVITY_TREATMENT.class);
            startActivity(intentLeft);
            overridePendingTransition(0, 0);
        });
        ibRightArrow.setOnClickListener(v -> {
            Intent intentRight = new Intent(this, Z_DELETED_ACTIVITY_RECOMMENDATION.class);
            startActivity(intentRight);
            overridePendingTransition(0, 0);
        });
    }

    private void setObjects() {
        SkinDiseaseData skinDiseaseData = SkinDiseaseData.getInstance();
        tvHeadText.setText(skinDiseaseData.getSkinDiseaseName());
        tvBodyText.setText(skinDiseaseData.getSkinDiseaseDescription());
        Glide.with(this)
                .load(skinDiseaseData.getSkinDiseaseImageURL())
                .placeholder(R.drawable.default_placeholder)
                .into(ivPreview);
    }
}