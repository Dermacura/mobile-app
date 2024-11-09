package com.thesis.dermocura.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.thesis.dermocura.datas.RegistrationData;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class ActivityRegisterInfo extends AppCompatActivity {

    // Declare input fields
    private EditText etFullName, etAge, etMobileNumber;
    private Spinner spinnerGender;
    private MaterialButton btnContinue, btnSelectBirthday;
    private LoadingDialogFragment loadingDialogFragment;

    String TAG = "ActivityRegisterInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize input fields and loading dialog
        initializeViews();
        loadingDialogFragment = new LoadingDialogFragment();

        // Set up gender spinner
        setupGenderSpinner();

        // Set click listener for the continue button
        btnContinue.setOnClickListener(v -> saveInputData());

        // Set click listener for the birthday button
        btnSelectBirthday.setOnClickListener(v -> openDatePicker());
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        spinnerGender = findViewById(R.id.spinnerGender);  // Gender Spinner
        etAge = findViewById(R.id.etAge);
        btnContinue = findViewById(R.id.btnContinue);
        btnSelectBirthday = findViewById(R.id.btnSelectBirthday);
    }

    private void setupGenderSpinner() {
        // Set up the spinner adapter with the gender options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    // Function to open the date picker dialog
    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    btnSelectBirthday.setText(formattedDate);
                    RegistrationData.getInstance().setPatientBirthDate(formattedDate);
                    calculateAge(selectedYear, selectedMonth, selectedDay);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        etAge.setText(String.valueOf(age)); // Set the calculated age
    }

    private void saveInputData() {
        String fullName = etFullName.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();  // Get the selected gender
        String ageText = etAge.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full Name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        if (mobileNumber.isEmpty()) {
            etMobileNumber.setError("Mobile Number cannot be empty");
            etMobileNumber.requestFocus();
            return;
        }

        Integer age = null;
        try {
            age = ageText.isEmpty() ? null : Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid age input: " + e.getMessage());
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        RegistrationData registrationData = RegistrationData.getInstance();
        registrationData.setPatientName(fullName);
        registrationData.setPatientMobileNumber(mobileNumber);
        registrationData.setPatientGender(gender);  // Save selected gender
        registrationData.setPatientAge(age);

        Log.d(TAG, "Data Saved: " +
                "\nName: " + registrationData.getPatientName() +
                "\nMobile Number: " + registrationData.getPatientMobileNumber() +  // Log mobile number
                "\nGender: " + registrationData.getPatientGender() +
                "\nAge: " + registrationData.getPatientAge() +
                "\nBirthday: " + registrationData.getPatientBirthDate());

        Toast.makeText(this, "Profile information saved successfully.", Toast.LENGTH_SHORT).show();

        makeHTTPRequest();
    }

    private void makeHTTPRequest() {
        if (loadingDialogFragment != null && !loadingDialogFragment.isAdded()) {
            loadingDialogFragment.show(getSupportFragmentManager(), "LoadingDialog");
        }

        RegistrationData registrationData = RegistrationData.getInstance();

        String keyName = "patientName";
        String keyPassword = "patientPassword";
        String keyEmail = "patientEmail";
        String keyGender = "patientGender";
        String keyMobileNumber = "patientMobileNumber";
        String keyAge = "patientAge";
        String keyBirthDate = "patientBirthDate";

        JSONObject requestBody = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(this);

        try {
            requestBody.put(keyName, registrationData.getPatientName());
            requestBody.put(keyPassword, registrationData.getPatientPassword());
            requestBody.put(keyEmail, registrationData.getPatientEmail());
            requestBody.put(keyGender, registrationData.getPatientGender());
            requestBody.put(keyMobileNumber, registrationData.getPatientMobileNumber());
            requestBody.put(keyAge, registrationData.getPatientAge());
            requestBody.put(keyBirthDate, registrationData.getPatientBirthDate());
        } catch (JSONException e) {
            Log.e(TAG + " makeHTTPRequest", String.valueOf(e));
            dismissLoadingDialog();
            return;
        }

        String URL = "https://backend.dermocura.net/android/authentication/registerinfo.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        Log.i(TAG + " makeHTTPRequest", requestBody.toString());

        queue.add(request);
    }

    private void dismissLoadingDialog() {
        if (loadingDialogFragment != null && loadingDialogFragment.isVisible()) {
            loadingDialogFragment.dismiss();
        }
    }

    private void onRequestSuccess(JSONObject response) {
        dismissLoadingDialog();
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                showAlertDialog("Registration Successful", message, true);
            } else {
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
                showAlertDialog("Registration Failed", message, false);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            showAlertDialog("Error", "Error parsing the response.", false);
        }
    }

    private void onRequestError(VolleyError error) {
        dismissLoadingDialog();
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
        showAlertDialog("Request Error", "Error: " + error.getMessage(), false);
    }

    private void showAlertDialog(String title, String message, boolean success) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (success) {
                        RegistrationData.getInstance().clearData();
                        Intent intent = new Intent(ActivityRegisterInfo.this, ActivityLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}
