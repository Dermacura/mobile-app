package com.thesis.dermocura.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import com.thesis.dermocura.datas.*;

import org.json.JSONException;
import org.json.JSONObject;

public class MySharedPreferences {
    private static final String TAG = "MySharedPreferences";
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String USER_DATA_KEY = "userData";
    private static MySharedPreferences instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    private static final String URL = "https://backend.dermocura.net/android/get_user_data.php";

    private MySharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    public static synchronized MySharedPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new MySharedPreferences(context.getApplicationContext());
        }
        return instance;
    }

    // Method to save UserData object
    public void saveUserData(UserData userData) {
        String json = gson.toJson(userData);
        editor.putString(USER_DATA_KEY, json);
        editor.apply();
    }

    // Method to retrieve UserData object
    public UserData getUserData() {
        String json = sharedPreferences.getString(USER_DATA_KEY, null);
        if (json != null) {
            return gson.fromJson(json, UserData.class);
        }
        return null;
    }

    // Method to clear UserData
    public void clearUserData() {
        editor.remove(USER_DATA_KEY);
        editor.apply();
    }

    // New method to update UserData object from a new UserData instance fetched from the database
    public void updateUserDataFromDatabase(UserData updatedUserData) {
        if (updatedUserData != null) {
            String json = gson.toJson(updatedUserData);
            editor.putString(USER_DATA_KEY, json);
            editor.apply();
        } else {
            Log.e(TAG, "No updated user data found.");
        }
    }

    // Method to update UserData from the database based on patientID stored in SharedPreferences
    public void updateUserDataFromDatabase(Context context) {
        UserData currentUser = getUserData();
        if (currentUser == null) {
            Log.e(TAG, "No user data found in SharedPreferences.");
            return;
        }

        int patientID = currentUser.getPatientID(); // Get patientID from current SharedPreferences
        RequestQueue queue = Volley.newRequestQueue(context);

        // Create a JSONObject with patientID to send with the request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", patientID);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception: " + e.getMessage());
        }

        // Create a Volley request to fetch updated data
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            // Parse the new user data from the response
                            JSONObject userDataJson = response.getJSONObject("userData");
                            String email = userDataJson.getString("patientEmail");
                            int id = userDataJson.getInt("patientID");
                            String imageUrl = userDataJson.getString("patientImageURL");
                            String mobileNumber = userDataJson.getString("patientMobileNumber");
                            String name = userDataJson.getString("patientName");
                            int age = userDataJson.getInt("patientAge");
                            String gender = userDataJson.getString("patientGender");

                            // Create a new UserData object with the updated data
                            UserData updatedUserData = new UserData(email, id, imageUrl, mobileNumber, name, age, gender);

                            // Save the updated data in SharedPreferences
                            saveUserData(updatedUserData);
                            Log.d(TAG, "User data successfully updated from the database.");
                        } else {
                            Log.e(TAG, "Failed to update user data from the database.");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                },
                error -> Log.e(TAG, "Error in network request: " + error.getMessage())
        );

        // Add the request to the Volley queue
        queue.add(request);
    }
}
