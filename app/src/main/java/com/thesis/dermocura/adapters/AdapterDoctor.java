package com.thesis.dermocura.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thesis.dermocura.R;
import com.thesis.dermocura.activities.Z_HOLD_ACTIVITY_APPOINTMENT;
import com.thesis.dermocura.models.ModelDoctor;

import java.util.List;

public class AdapterDoctor extends RecyclerView.Adapter<AdapterDoctor.ViewHolder> {

    private static final String BASE_URL = "https://backend.dermocura.net/images/doctor_profile/";
    private Context context;
    private List<ModelDoctor> modelDoctorList;

    public AdapterDoctor(Context context, List<ModelDoctor> modelDoctorList) {
        this.context = context;
        this.modelDoctorList = modelDoctorList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModelDoctor doctor = modelDoctorList.get(position);
        String fullImageUrl = BASE_URL + doctor.getDoctorImage();

        int doctorID = doctor.getDoctorID();
        String doctorName = doctor.getDoctorName();
        String doctorEmail = doctor.getDoctorEmail();

        holder.tvTopText.setText(doctorName);
        holder.tvBottomText.setText(doctorEmail);

        // Load the doctor's image using Glide
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.default_placeholder)
                .into(holder.ivImagePreview);

        holder.ibButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Z_HOLD_ACTIVITY_APPOINTMENT.class);
                intent.putExtra("doctorID", doctorID);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelDoctorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagePreview;
        TextView tvTopText, tvBottomText;
        ImageButton ibButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagePreview = itemView.findViewById(R.id.ivImagePreview);
            tvTopText = itemView.findViewById(R.id.tvTopText);
            tvBottomText = itemView.findViewById(R.id.tvBottomText);
            ibButton = itemView.findViewById(R.id.ibButton);
        }
    }
}
