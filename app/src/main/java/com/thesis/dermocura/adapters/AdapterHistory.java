package com.thesis.dermocura.adapters;

import com.thesis.dermocura.activities.ActivityAnalyzation;
import com.thesis.dermocura.datas.SkinDiseaseData;
import com.thesis.dermocura.models.ModelHistory;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.thesis.dermocura.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.HistoryViewHolder> {

    private static final String BASE_URL = "https://backend.dermocura.net/images/skin_diseases/";
    private List<ModelHistory> modelHistoryList;
    private Context context;

    public AdapterHistory(Context context, List<ModelHistory> modelHistoryList) {
        this.context = context;
        this.modelHistoryList = modelHistoryList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ModelHistory model = modelHistoryList.get(position);
        String fullImageUrl = BASE_URL + model.getSkinDiseaseImageURL();

        int diseaseID = model.getSkinDiseaseID();
        String diagnosis = "Diagnosis: " + model.getSkinDiseaseName();
        String analyzedDate = "Scanned at " + model.getPatientAnalyzedDate();

        SkinDiseaseData skinDiseaseData = SkinDiseaseData.getInstance();
        skinDiseaseData.makeHTTPRequest(context, diseaseID);

        // Load the image from the URL using Glide
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.default_placeholder)  // Placeholder image
                .into(holder.imageViewPlaceholder);

        // Set the diagnosis name
        holder.textViewDiagnosis.setText(diagnosis);

        // Set the analyzed date
        holder.textViewAnalyzedDate.setText(analyzedDate);

        // Set the arrow icon
        holder.imageViewArrow.setImageResource(R.drawable.icon_bend_arrow);
        holder.imageViewArrow.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityAnalyzation.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return modelHistoryList.size();
    }

    // ViewHolder class for RecyclerView
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPlaceholder;
        TextView textViewDiagnosis;
        TextView textViewAnalyzedDate;
        ImageView imageViewArrow;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPlaceholder = itemView.findViewById(R.id.ivImagePreview);
            textViewDiagnosis = itemView.findViewById(R.id.tvTopText);
            textViewAnalyzedDate = itemView.findViewById(R.id.tvBottomText);
            imageViewArrow = itemView.findViewById(R.id.ibButton);
        }
    }
}
