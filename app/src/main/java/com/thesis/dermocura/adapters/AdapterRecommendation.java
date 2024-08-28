package com.thesis.dermocura.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.thesis.dermocura.R;
import com.thesis.dermocura.models.ModelRecommendation;

import java.util.List;

public class AdapterRecommendation extends RecyclerView.Adapter<AdapterRecommendation.ViewHolder> {

    private List<ModelRecommendation> mData;
    private LayoutInflater mInflater;

    public AdapterRecommendation(Context context, List<ModelRecommendation> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // Inflate the card layout from XML
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recommendation_card, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to the TextView in each card
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ModelRecommendation recommendation = mData.get(position);
        holder.titleTextView.setText(recommendation.getTitle());
        holder.contentTextView.setText(recommendation.getContent());
    }

    // Number of items in the list
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // Store and recycle views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recommendation_title);
            contentTextView = itemView.findViewById(R.id.recommendation_text);
        }
    }
}