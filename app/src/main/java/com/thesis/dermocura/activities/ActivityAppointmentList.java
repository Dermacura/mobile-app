package com.thesis.dermocura.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AppointmentAdapter;
import com.thesis.dermocura.models.Appointment;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityAppointmentList extends AppCompatActivity {

    private static final String TAG = "ActivityAppointmentList";
    private static final String URL_FETCH_APPOINTMENTS = "https://backend.dermocura.net/android/appointment/fetchappointments.php";
    private RecyclerView recyclerViewAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private LoadingDialogFragment loadingDialogFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchAppointments);

        // Initialize RecyclerView and set its layout manager with reverse layout
        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true); // This will show the newest data at the top
        layoutManager.setStackFromEnd(true);  // Optional: to control scroll position when loading
        recyclerViewAppointments.setLayoutManager(layoutManager);

        appointmentList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        recyclerViewAppointments.setAdapter(appointmentAdapter);

        // Initialize and show the loading dialog
        loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");

        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityAppointmentList.this, ActivityAppointment.class);
            startActivity(intent);
        });

        // Fetch appointments
        fetchAppointments();
    }

    private void fetchAppointments() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Get patientID from shared preferences
        int patientID = MySharedPreferences.getInstance(this).getUserData().getPatientID();

        // Create request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", patientID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_FETCH_APPOINTMENTS,
                requestBody,
                response -> {
                    // Dismiss the loading dialog once the response is received
                    if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
                        loadingDialogFragment.dismiss();
                    }

                    // Stop the refresh animation
                    swipeRefreshLayout.setRefreshing(false);

                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray appointmentsArray = response.getJSONArray("appointments");
                            appointmentList.clear();  // Clear any old data

                            for (int i = 0; i < appointmentsArray.length(); i++) {
                                JSONObject appointmentObj = appointmentsArray.getJSONObject(i);
                                int appointmentID = appointmentObj.getInt("appointmentID");
                                String doctorName = appointmentObj.getString("doctor_name");
                                String clinicName = appointmentObj.getString("clinic_name");
                                String clinicLogo = appointmentObj.getString("clinic_logo");
                                String startTime = appointmentObj.getString("start_time");
                                String endTime = appointmentObj.getString("end_time");
                                String availDate = appointmentObj.getString("avail_date");
                                int statusValue = appointmentObj.getInt("status");
                                String remarkInput = appointmentObj.optString("remarkInput", "No remarks available"); // Get remarks

                                String status;
                                switch (statusValue) {
                                    case 0:
                                        status = "Pending";
                                        break;
                                    case 1:
                                        status = "Accepted";
                                        break;
                                    case 2:
                                        status = "Declined";
                                        break;
                                    default:
                                        status = "Unknown";
                                        break;
                                }

                                // Add to the appointment list with remarkInput
                                appointmentList.add(new Appointment(appointmentID, doctorName, clinicName, clinicLogo, startTime, endTime, availDate, status, remarkInput));
                            }

                            // Notify adapter
                            appointmentAdapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(this, "No appointments found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Dismiss the loading dialog in case of an error
                    if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
                        loadingDialogFragment.dismiss();
                    }

                    // Stop the refresh animation
                    swipeRefreshLayout.setRefreshing(false);

                    Log.e(TAG, "Error fetching appointments: " + error.getMessage());
                }
        );

        queue.add(request);
    }

    // Show the custom remarks dialog
    public void showRemarksDialog(String remarkMessage) {
        // Create a custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_remarks); // Set the custom layout for the dialog

        // Set the dialog title
        TextView title = dialog.findViewById(R.id.dialog_title);
        title.setText("Remarks");

        // Set the remark content in the dialog
        TextView remarksContent = dialog.findViewById(R.id.dialog_remarks_content);
        remarksContent.setText(remarkMessage.isEmpty() ? "No remarks available" : remarkMessage);

        // Set the action for the Continue button
        Button continueButton = dialog.findViewById(R.id.dialog_button_continue);
        continueButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }
}
