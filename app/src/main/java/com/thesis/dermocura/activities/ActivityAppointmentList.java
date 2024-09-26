package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
                                String status = appointmentObj.getString("status");

                                // Add to the appointment list
                                appointmentList.add(new Appointment(appointmentID, doctorName, clinicName, clinicLogo, startTime, endTime, availDate, status));
                            }

                            // Notify the adapter of data changes
                            appointmentAdapter.notifyDataSetChanged();

                            // No need to scroll, as the newest items are already shown at the top
                            Log.d("Refresh", "Refresh completed on Appointment List");
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
}
