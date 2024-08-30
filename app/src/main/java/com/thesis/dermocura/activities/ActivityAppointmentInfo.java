package com.thesis.dermocura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityAppointmentInfo extends AppCompatActivity {

    private static final String TAG = "ActivityAppointmentInfo";
    private static final String URL = "https://backend.dermocura.net/android/fetchavailabledays.php";
    private static final String TIMEURL = "https://backend.dermocura.net/android/fetchavailabletime.php";
    private Spinner spinner, TimeOfTheWeek;
    MaterialButton btnContinue;
    private ArrayList<String> dayOfWeekList = new ArrayList<>();
    private ArrayList<String> timeOfWeekList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        int doctorID = getIntent().getIntExtra("doctorID", -1);

        // Initialize the spinner
        spinner = findViewById(R.id.custom_spinner);
        TimeOfTheWeek = findViewById(R.id.TimeOfTheWeek);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            String selectedDocAvailabilityID = getSelectedDocAvailabilityID();
            if (selectedDocAvailabilityID != null) {
                registerAppointment(selectedDocAvailabilityID);
            } else {
                Log.e(TAG, "No time slot selected for registration.");
            }
        });

        // Make the HTTP request
        makeHTTPRequest(doctorID);
    }

    private void makeHTTPRequest(int doctorID) {
        String keyDoctor = "doctorID";
        JSONObject requestBody = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(this);

        try {
            requestBody.put(keyDoctor, doctorID);
        } catch (JSONException e) {
            Log.e(TAG + " makeHTTPRequest", String.valueOf(e));
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        String stringJSON = requestBody.toString();
        Log.i(TAG + " makeHTTPRequest", stringJSON);

        queue.add(request);
    }

    private void onRequestSuccess(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Extract the data array
                JSONArray dataArray = response.getJSONArray("data");

                // Clear the existing list to avoid duplicate entries
                dayOfWeekList.clear();

                // Loop through the array to extract day_of_week
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String dayOfWeek = dataObject.getString("day_of_week");
                    dayOfWeekList.add(dayOfWeek); // Add to the list
                }

                // Update the spinner with the new data
                updateDaySpinner(dayOfWeekList);
            } else {
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }

    private void onRequestError(VolleyError error) {
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
    }

    private void updateDaySpinner(ArrayList<String> dayOfWeekList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, dayOfWeekList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_item_text);
                textView.setText(getItem(position));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_item_text);
                textView.setText(getItem(position));
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection
                String selectedDay = dayOfWeekList.get(position);
                Log.d(TAG, "Selected Day: " + selectedDay);
                dayHTTPRequest(selectedDay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected case
            }
        });
    }

    private void dayHTTPRequest(String Day) {
        String keyDay = "day_of_week";
        String keyDoctorID = "doctorID";
        JSONObject requestBody = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(this);

        int doctorID = getIntent().getIntExtra("doctorID", -1);

        try {
            requestBody.put(keyDay, Day);
            requestBody.put(keyDoctorID, doctorID);
        } catch (JSONException e) {
            Log.e(TAG + " makeHTTPRequest", String.valueOf(e));
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                TIMEURL,
                requestBody,
                this::daySuccess,
                this::dayError
        );

        String stringJSON = requestBody.toString();
        Log.i(TAG + " makeHTTPRequest", stringJSON);

        queue.add(request);
    }

    private void daySuccess(JSONObject response) {
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Extract the data array
                JSONArray dataArray = response.getJSONArray("data");

                // Clear the existing list to avoid duplicate entries
                timeOfWeekList.clear();

                // Loop through the array to extract the time_range and docAvailabilityID
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String timeOfWeek = dataObject.getString("time_range");
                    String docAvailabilityID = dataObject.getString("docAvailabilityID");
                    timeOfWeekList.add(timeOfWeek + "|" + docAvailabilityID); // Combine both into a single string
                }

                // Update the time spinner with the new data
                updateTimeSpinner(timeOfWeekList);
            } else {
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }


    private void dayError(VolleyError error) {
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
    }

    private void updateTimeSpinner(ArrayList<String> timeOfWeekList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item, timeOfWeekList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_item_text);
                textView.setText(getItem(position).split("\\|")[0]); // Display only the time_range
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_item_text);
                textView.setText(getItem(position).split("\\|")[0]); // Display only the time_range
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        TimeOfTheWeek.setAdapter(adapter);

        // Automatically select the first item if it exists
        if (!timeOfWeekList.isEmpty()) {
            TimeOfTheWeek.setSelection(0);
        }

        TimeOfTheWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedTime = timeOfWeekList.get(position).split("\\|")[0];
                Log.d(TAG, "Selected Time: " + selectedTime);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle nothing selected case
            }
        });
    }


    private void registerAppointment(String docAvailabilityID) {
        String registerURL = "https://backend.dermocura.net/android/setappointments.php"; // Update with your actual URL
        UserData userData = MySharedPreferences.getInstance(this).getUserData(); // Assuming this is how you retrieve UserData

        int patientID = userData.getPatientID();

        // Prepare the JSON request body
        JSONObject requestBody = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(this);

        try {
            requestBody.put("patientID", patientID);
            requestBody.put("docAvailabilityID", docAvailabilityID);
        } catch (JSONException e) {
            Log.e(TAG + " registerAppointment", String.valueOf(e));
            return;
        }

        Log.i(TAG + " registerAppointment", String.valueOf(requestBody));

        // Create the request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                registerURL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Log.d(TAG + " registerAppointment", "Appointment registered successfully: " + message);
                            Intent intent = new Intent(this, ActivityOldDashboard.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG + " registerAppointment", "Failed to register appointment: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG + " registerAppointment", String.valueOf(e));
                    }
                },
                error -> Log.e(TAG + " registerAppointment", "Error Response: " + error.getMessage())
        );

        queue.add(request);
    }

    private String getSelectedDocAvailabilityID() {
        int selectedPosition = TimeOfTheWeek.getSelectedItemPosition();
        if (selectedPosition != AdapterView.INVALID_POSITION && !timeOfWeekList.isEmpty()) {
            // Extract docAvailabilityID from the selected item
            String selectedItem = timeOfWeekList.get(selectedPosition);
            String[] parts = selectedItem.split("\\|");
            return parts.length > 1 ? parts[1] : null;
        }
        return null;
    }


}