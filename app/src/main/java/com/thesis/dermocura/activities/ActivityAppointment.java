package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ActivityAppointment extends AppCompatActivity {

    private static final String TAG = "ActivityAppointment";
    private static final String URL_FETCH_CLINICS = "https://backend.dermocura.net/android/appointment/fetchclinic.php";
    private static final String URL_FETCH_DOCTORS_BY_CLINIC = "https://backend.dermocura.net/android/appointment/fetchdoctorbyclinic.php";
    private static final String URL_FETCH_AVAILABILITY = "https://backend.dermocura.net/android/appointment/fetchavailability.php";
    private static final String URL_FETCH_TIMESLOTS = "https://backend.dermocura.net/android/appointment/fetchslots.php";
    private static final String URL_SUBMIT_APPOINTMENT = "https://backend.dermocura.net/android/appointment/setappointment.php";

    private Spinner spinnerClinic, spinnerDoctor, spinnerDay, spinnerTimeSlot;
    private List<String> clinicList = new ArrayList<>();
    private List<Integer> clinicIDs = new ArrayList<>();
    private List<String> doctorList = new ArrayList<>();
    private List<Integer> doctorIDs = new ArrayList<>();
    private List<String> availabilityList = new ArrayList<>();
    private List<String> timeSlotList = new ArrayList<>();
    private List<Integer> docAvailIDs = new ArrayList<>();
    private int selectedClinicID;
    private int selectedDoctorID;

    // Loading dialog instance
    private LoadingDialogFragment loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // EdgeToEdge and content view setup
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the loading dialog
        loadingDialog = new LoadingDialogFragment();

        // Find views by ID
        spinnerClinic = findViewById(R.id.spinnerClinic);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerTimeSlot = findViewById(R.id.spinnerTimeSlot);

        // Initially disable dependent spinners
        spinnerDoctor.setEnabled(false);
        spinnerDay.setEnabled(false);
        spinnerTimeSlot.setEnabled(false);

        // Show loading dialog and fetch clinics on activity start
        showLoadingDialog();
        fetchClinics();

        // Handle clinic selection
        spinnerClinic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedClinicID = clinicIDs.get(position - 1);  // Get the clinicID for the selected clinic
                    Log.i(TAG, "Selected Clinic ID: " + selectedClinicID);
                    showLoadingDialog();
                    fetchDoctorsByClinic(selectedClinicID);
                } else {
                    // Disable dependent spinners when no clinic is selected
                    spinnerDoctor.setEnabled(false);
                    spinnerDay.setEnabled(false);
                    spinnerTimeSlot.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No clinic selected");
            }
        });

        // Handle doctor selection
        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedDoctorID = doctorIDs.get(position - 1);  // Get the doctorID for the selected doctor
                    Log.i(TAG, "Selected Doctor ID: " + selectedDoctorID);
                    showLoadingDialog();
                    fetchAvailability(selectedDoctorID);
                } else {
                    // Disable the day and time slot spinners if no doctor is selected
                    spinnerDay.setEnabled(false);
                    spinnerTimeSlot.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No doctor selected");
            }
        });

        // Handle day selection
        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // Get the corresponding docAvailID for the selected day
                    int selectedDocAvailID = docAvailIDs.get(position - 1);
                    Log.i(TAG, "Selected docAvailID: " + selectedDocAvailID);
                    showLoadingDialog();
                    fetchTimeSlots(selectedDocAvailID);
                } else {
                    // Disable the time slot spinner if no day is selected
                    spinnerTimeSlot.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No day selected");
            }
        });

        // Submit appointment button
        MaterialButton btnSubmitAppointment = findViewById(R.id.btnSubmitAppointment);
        btnSubmitAppointment.setOnClickListener(v -> {
            if (spinnerClinic.getSelectedItemPosition() > 0 && spinnerDoctor.getSelectedItemPosition() > 0
                    && spinnerDay.getSelectedItemPosition() > 0 && spinnerTimeSlot.getSelectedItemPosition() > 0) {
                int selectedDocAvailID = docAvailIDs.get(spinnerDay.getSelectedItemPosition() - 1);
                String selectedTimeSlot = spinnerTimeSlot.getSelectedItem().toString();

                // Split the selected time slot into start and end times
                String[] timeParts = selectedTimeSlot.split(" - ");
                String startTime = timeParts[0];  // Extract start time
                String endTime = timeParts[1];    // Extract end time

                Log.i(TAG, "Proceeding to additional input with docAvailID: " + selectedDocAvailID +
                        ", startTime: " + startTime + ", endTime: " + endTime);

                // Move to the next screen to get phone number and additional input
                Intent intent = new Intent(ActivityAppointment.this, ActivityAppointmentContact.class);
                intent.putExtra("docAvailID", selectedDocAvailID);
                intent.putExtra("startTime", startTime);
                intent.putExtra("endTime", endTime);
                startActivity(intent);
            } else {
                Toast.makeText(ActivityAppointment.this, "Please select all fields", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Appointment submission failed due to unselected fields");
            }
        });
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

    // Fetch clinics method
    private void fetchClinics() {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d(TAG, "Fetching clinics...");
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_FETCH_CLINICS,
                null,
                this::onFetchClinicsSuccess,
                this::onRequestError
        );
        queue.add(request);
    }

    private void onFetchClinicsSuccess(JSONObject response) {
        dismissLoadingDialog(); // Dismiss dialog on response
        try {
            Log.d(TAG, "Clinics fetch success: " + response.toString());
            boolean success = response.getBoolean("success");
            if (success) {
                JSONArray clinicArray = response.getJSONArray("clinics");
                clinicList.clear();
                clinicList.add("Select a Clinic");

                for (int i = 0; i < clinicArray.length(); i++) {
                    JSONObject clinicObj = clinicArray.getJSONObject(i);
                    String clinicName = clinicObj.getString("clinicName");
                    int clinicID = clinicObj.getInt("clinicID");

                    clinicList.add(clinicName);  // Add clinic name to list
                    clinicIDs.add(clinicID);     // Store corresponding clinicID
                }

                // Populate spinner with clinics
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, clinicList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerClinic.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No clinics available", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No clinics available");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing clinics JSON", e);
        }
    }

    // Fetch doctors based on selected clinic using the updated PHP script
    private void fetchDoctorsByClinic(int clinicID) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("clinicID", clinicID);
            Log.d(TAG, "Fetching doctors for clinicID: " + clinicID);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body for doctors", e);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_FETCH_DOCTORS_BY_CLINIC,
                requestBody,
                this::onFetchDoctorsByClinicSuccess,
                this::onRequestError
        );

        queue.add(request);
    }

    // Adjust the onSuccess method of fetchDoctorsByClinic
    private void onFetchDoctorsByClinicSuccess(JSONObject response) {
        dismissLoadingDialog(); // Dismiss dialog on response
        try {
            Log.d(TAG, "Doctors fetch success: " + response.toString());
            boolean success = response.getBoolean("success");
            if (success) {
                JSONArray doctorArray = response.getJSONArray("doctors");
                doctorList.clear();
                doctorIDs.clear(); // Clear existing IDs
                doctorList.add("Select a Doctor");

                // Use a set to track unique doctor IDs
                Set<Integer> uniqueDoctorIds = new HashSet<>();

                for (int i = 0; i < doctorArray.length(); i++) {
                    JSONObject doctorObj = doctorArray.getJSONObject(i);
                    String doctorName = doctorObj.getString("doctorName");
                    int doctorID = doctorObj.getInt("doctorID");

                    // Add the doctor only if the ID hasn't been added before
                    if (!uniqueDoctorIds.contains(doctorID)) {
                        doctorList.add(doctorName);  // Add doctor name to list
                        doctorIDs.add(doctorID);     // Store corresponding doctorID
                        uniqueDoctorIds.add(doctorID); // Mark this ID as seen
                    }
                }

                if (doctorList.size() > 1) { // Enable spinner only if there are doctors
                    spinnerDoctor.setEnabled(true);
                } else {
                    spinnerDoctor.setEnabled(false);
                    spinnerDay.setEnabled(false);
                    spinnerTimeSlot.setEnabled(false);
                }

                // Populate spinner with doctors
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, doctorList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDoctor.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No available doctors found for this clinic.", Toast.LENGTH_SHORT).show();
                spinnerDoctor.setEnabled(false);
                spinnerDay.setEnabled(false);
                spinnerTimeSlot.setEnabled(false);
                Log.w(TAG, "No available doctors found for clinicID: " + selectedClinicID);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing doctors JSON", e);
        }
    }


    // Fetch availability based on selected doctor
    private void fetchAvailability(int doctorID) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("doctorID", doctorID);
            Log.d(TAG, "Fetching availability for doctorID: " + doctorID);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body for availability", e);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_FETCH_AVAILABILITY,
                requestBody,
                this::onFetchAvailabilitySuccess,
                this::onRequestError
        );

        queue.add(request);
    }

    private void onFetchAvailabilitySuccess(JSONObject response) {
        dismissLoadingDialog(); // Dismiss dialog on response
        try {
            Log.d(TAG, "Availability fetch success: " + response.toString());
            boolean success = response.getBoolean("success");
            if (success) {
                JSONArray availabilityArray = response.getJSONArray("data");
                availabilityList.clear();
                docAvailIDs.clear();  // Clear previous docAvailIDs
                availabilityList.add("Select a Day");

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar currentCalendar = Calendar.getInstance();  // Get the current date

                for (int i = 0; i < availabilityArray.length(); i++) {
                    JSONObject availObj = availabilityArray.getJSONObject(i);
                    String availDay = availObj.getString("avail_day");
                    String availDate = availObj.getString("avail_date");
                    int docAvailID = availObj.getInt("docAvailID");

                    // Parse the avail_date and compare with the current date
                    Calendar availCalendar = Calendar.getInstance();
                    availCalendar.setTime(dateFormat.parse(availDate));

                    // Only add dates that are today or in the future
                    if (!availCalendar.before(currentCalendar)) {
                        String combinedDayDate = availDay + " | " + availDate;
                        availabilityList.add(combinedDayDate);
                        docAvailIDs.add(docAvailID);  // Store corresponding docAvailID
                    }
                }

                if (availabilityList.size() > 1) {
                    spinnerDay.setEnabled(true);  // Enable the day spinner if future availability is found
                } else {
                    spinnerDay.setEnabled(false);
                    spinnerTimeSlot.setEnabled(false);
                    Toast.makeText(this, "No future availability for this doctor", Toast.LENGTH_SHORT).show();
                }

                // Populate the spinner with future availability days and dates
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, availabilityList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDay.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No availability for this doctor", Toast.LENGTH_SHORT).show();
                spinnerDay.setEnabled(false);
                spinnerTimeSlot.setEnabled(false);
                Log.w(TAG, "No availability found for doctorID: " + selectedDoctorID);
            }
        } catch (JSONException | ParseException e) {
            Log.e(TAG, "Error parsing availability JSON", e);
        }
    }

    // Fetch time slots based on selected docAvailID
    private void fetchTimeSlots(int docAvailID) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("docAvailID", docAvailID);
            Log.d(TAG, "Fetching time slots for docAvailID: " + docAvailID);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body for time slots", e);
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_FETCH_TIMESLOTS,
                requestBody,
                this::onFetchTimeSlotsSuccess,
                this::onRequestError
        );

        queue.add(request);
    }

    // Adjust the onSuccess method of fetchTimeSlots
    private void onFetchTimeSlotsSuccess(JSONObject response) {
        dismissLoadingDialog(); // Dismiss dialog on response
        try {
            Log.d(TAG, "Time slots fetch success: " + response.toString());
            boolean success = response.getBoolean("success");
            if (success) {
                JSONArray slotsArray = response.getJSONArray("slots");
                timeSlotList.clear();
                timeSlotList.add("Select a Time Slot");

                if (slotsArray.length() > 0) {
                    spinnerTimeSlot.setEnabled(true);  // Enable the time slot spinner if slots are available
                } else {
                    spinnerTimeSlot.setEnabled(false);
                }

                for (int i = 0; i < slotsArray.length(); i++) {
                    JSONObject slotObj = slotsArray.getJSONObject(i);
                    String startTime = slotObj.getString("slot_start_time");
                    String endTime = slotObj.getString("slot_end_time");

                    // Combine start and end time (e.g., "09:00 AM - 09:15 AM")
                    String timeSlot = startTime + " - " + endTime;
                    timeSlotList.add(timeSlot);
                }

                // Sort the time slots before displaying
                sortTimeSlots();

                // Populate the spinner with sorted time slots
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, timeSlotList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTimeSlot.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No time slots available", Toast.LENGTH_SHORT).show();
                spinnerTimeSlot.setEnabled(false);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing time slots JSON", e);
        }
    }

    // Sort time slots based on AM/PM order
    private void sortTimeSlots() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Collections.sort(timeSlotList.subList(1, timeSlotList.size()), new Comparator<String>() {
            @Override
            public int compare(String slot1, String slot2) {
                try {
                    String startTime1 = slot1.split(" - ")[0];
                    String startTime2 = slot2.split(" - ")[0];
                    return sdf.parse(startTime1).compareTo(sdf.parse(startTime2));
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing time slot: " + e.getMessage());
                    return 0;
                }
            }
        });
    }

    // Method to submit the appointment
    private void submitAppointment(int docAvailID, String startTime, String endTime) {
        // Get user data from shared preferences
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();
        int patientID = userData.getPatientID();  // Fetch the patientID from shared preferences

        // Calculate notif_datetime (current time + 10 seconds)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 20);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String notifDateTime = dateFormat.format(calendar.getTime());

        // Prepare JSON request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", patientID);
            requestBody.put("docAvailabilityID", docAvailID);
            requestBody.put("start_time", startTime);
            requestBody.put("end_time", endTime);
            requestBody.put("notif_datetime", notifDateTime);
            requestBody.put("status", "Pending");  // Default status
            requestBody.put("notif_active", "active");  // Default notification status
            requestBody.put("notif_count", 1);  // Default notification count
            Log.d(TAG, "Submitting appointment: " + requestBody.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body for appointment", e);
        }

        // Send the request using Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_SUBMIT_APPOINTMENT,
                requestBody,
                response -> {
                    dismissLoadingDialog(); // Dismiss dialog on response
                    Log.d(TAG, "Appointment submission response: " + response.toString());
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Toast.makeText(ActivityAppointment.this, "Appointment registered successfully", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Appointment registered successfully");
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(ActivityAppointment.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error registering appointment: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        Toast.makeText(ActivityAppointment.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    dismissLoadingDialog(); // Dismiss dialog on error
                    Log.e(TAG, "Error submitting appointment: " + error.getMessage());
                    Toast.makeText(ActivityAppointment.this, "Failed to submit appointment", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the Volley queue
        queue.add(request);
    }

    // General request error handling remains the same
    private void onRequestError(VolleyError error) {
        dismissLoadingDialog(); // Dismiss dialog on error
        Log.e(TAG, "Request Error: " + error.getMessage());
        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
    }
}
