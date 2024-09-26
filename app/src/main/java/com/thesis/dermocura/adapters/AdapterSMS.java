package com.thesis.dermocura.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thesis.dermocura.R;
import com.thesis.dermocura.activities.ActivityTelemedicine;
import com.thesis.dermocura.models.ModelSMS;

import java.util.List;

public class AdapterSMS extends RecyclerView.Adapter<AdapterSMS.MessageViewHolder> {

    private static final String TAG = "AdapterSMS"; // Logging tag
    private Context context;
    private List<ModelSMS> messageList;

    public AdapterSMS(Context context, List<ModelSMS> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sms, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ModelSMS message = messageList.get(position);

        // Bind data to the views
        holder.tvTopText.setText(message.getTempFullName());
        holder.tvBottomText.setText(message.getFormattedDate() + ": " + message.getTelemedicineContent());

        // Log the userAccID and profile for debugging
        Log.d(TAG, "UserAccID: " + message.getUserAccID());
        Log.d(TAG, "Profile: " + message.getProfile());

        // Load profile image using Glide
        String profileUrl = message.getProfile();
        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(context)
                    .load(profileUrl)
                    .placeholder(R.drawable.default_placeholder)
                    .into(holder.ivImagePreview);
        } else {
            holder.ivImagePreview.setImageResource(R.drawable.default_placeholder);
        }

        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            // Log userAccID
            Log.d(TAG, "Item clicked for userAccID: " + message.getUserAccID());

            // Create an intent to start the new activity
            Intent intent = new Intent(context, ActivityTelemedicine.class);

            // Pass userAccID as an extra in the intent
            intent.putExtra("userAccID", message.getUserAccID());

            // Start the new activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        // Views from the item layout
        CardView cvImageHolder;
        ImageView ivImagePreview;
        TextView tvTopText;
        TextView tvBottomText;
        ImageButton ibButton;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            cvImageHolder = itemView.findViewById(R.id.cvImageHolder);
            ivImagePreview = itemView.findViewById(R.id.ivImagePreview);
            tvTopText = itemView.findViewById(R.id.tvTopText);
            tvBottomText = itemView.findViewById(R.id.tvBottomText);
            ibButton = itemView.findViewById(R.id.ibButton);
        }
    }
}

