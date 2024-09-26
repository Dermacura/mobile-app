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
import com.thesis.dermocura.activities.ActivityTelemedicine;
import com.thesis.dermocura.models.ModelContacts;

import java.util.List;

public class AdapterContacts extends RecyclerView.Adapter<AdapterContacts.ContactsViewHolder> {

    private List<ModelContacts> contactsList;
    private Context context;

    public AdapterContacts(Context context, List<ModelContacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout (ensure you use the correct layout file, e.g., item_contact.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        ModelContacts contact = contactsList.get(position);

        // Set full name, handle null case
        String fullName = (contact.getFullname() == null || contact.getFullname().isEmpty()) ? "Unknown" : contact.getFullname();
        holder.tvTopText.setText(fullName);

        // Set clinic name, handle null case
        String clinicName = (contact.getClinicName() == null || contact.getClinicName().isEmpty()) ? "No Clinic" : contact.getClinicName();
        holder.tvBottomText.setText(clinicName);

        // Load the profile image using Glide
        Glide.with(context)
                .load(contact.getProfile()) // Handle the profile path accordingly
                .placeholder(R.drawable.default_placeholder) // Add a placeholder drawable
                .into(holder.ivImagePreview);

        // Set the click listener on the entire item view
        holder.itemView.setOnClickListener(v -> {
            // Create an intent to start the next activity
            Intent intent = new Intent(context, ActivityTelemedicine.class);

            // Pass the userAccID to the next activity
            intent.putExtra("userAccID", contact.getUserAccID());

            // Start the next activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopText, tvBottomText;
        ImageView ivImagePreview;
        ImageButton ibButton;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTopText = itemView.findViewById(R.id.tvTopText);
            tvBottomText = itemView.findViewById(R.id.tvBottomText);
            ivImagePreview = itemView.findViewById(R.id.ivImagePreview);
            ibButton = itemView.findViewById(R.id.ibButton);
        }
    }
}


