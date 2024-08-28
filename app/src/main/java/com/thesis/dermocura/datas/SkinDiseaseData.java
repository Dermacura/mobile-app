package com.thesis.dermocura.datas;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class SkinDiseaseData {

    private static final String TAG = "SkinDiseaseData";
    private static final String URL = "https://backend.dermocura.net/android/fetchdisease.php";

    private static SkinDiseaseData instance;

    private int skinDiseaseID;
    private String skinDiseaseName;
    private String skinDiseaseDescription;
    private String skinDiseaseTreatment;
    private String skinDiseaseRecommendation;
    private String skinDiseaseImageURL;

    // Private constructor to enforce singleton pattern
    private SkinDiseaseData() {
        clearData();
    }

    // Synchronized method to get the single instance of the class
    public static synchronized SkinDiseaseData getInstance() {
        if (instance == null) {
            instance = new SkinDiseaseData();
        }
        return instance;
    }

    // Getters and setters for the fields
    public int getSkinDiseaseID() {
        return skinDiseaseID;
    }

    public void setSkinDiseaseID(int skinDiseaseID) {
        this.skinDiseaseID = skinDiseaseID;
    }

    public String getSkinDiseaseName() {
        return "Skin Disease:  " + skinDiseaseName;
    }

    public String getSkinDiseaseDescription() {
        return skinDiseaseDescription;
    }

    public String getSkinDiseaseTreatment() {
        return skinDiseaseTreatment;
    }

    public String getSkinDiseaseRecommendation() {
        return skinDiseaseRecommendation;
    }

    public String getSkinDiseaseImageURL() {
        return "https://backend.dermocura.net/images/skin_diseases/" + skinDiseaseImageURL;
    }

    // Method to clear all the data in the singleton instance
    public void clearData() {
        this.skinDiseaseID = 0;
        this.skinDiseaseName = null;
        this.skinDiseaseDescription = null;
        this.skinDiseaseTreatment = null;
        this.skinDiseaseRecommendation = null;
        this.skinDiseaseImageURL = null;

        Log.i(TAG + " clearData", "All data cleared in SkinDiseaseData");
    }

    // Method to make an HTTP request to the PHP script using Volley
    public void makeHTTPRequest(Context context, int skinDiseaseID) {
        // Define keys for the JSON request body
        String keyID = "skinDiseaseID";

        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(context);

        // Populate the JSON request body
        try {
            requestBody.put(keyID, skinDiseaseID);
        } catch (JSONException e) {
            Log.e(TAG + " makeHTTPRequest", String.valueOf(e));
            return;
        }

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                requestBody,
                this::onRequestSuccess,
                this::onRequestError
        );

        // Log the JSON request body for debugging
        String stringJSON = requestBody.toString();
        Log.i(TAG + " makeHTTPRequest", stringJSON);

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void onRequestSuccess(JSONObject response) {
        try {
            // Extract success status and message from the JSON response
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Extract the disease data from the response
                JSONObject diseaseDataJson = response.getJSONObject("data");

                // Set the data to the singleton fields
                this.skinDiseaseName = diseaseDataJson.getString("skinDiseaseName");
                this.skinDiseaseDescription = diseaseDataJson.getString("skinDiseaseDescription");
                this.skinDiseaseTreatment = diseaseDataJson.getString("skinDiseaseTreatment");
                this.skinDiseaseRecommendation = diseaseDataJson.getString("skinDiseaseRecommendation");
                this.skinDiseaseImageURL = diseaseDataJson.getString("skinDiseaseImageURL");

                // Log the extracted data for debugging
                Log.d(TAG + " SkinDiseaseData", "Skin Disease Name: " + this.skinDiseaseName);
                Log.d(TAG + " SkinDiseaseData", "Skin Disease Description: " + this.skinDiseaseDescription);
                Log.d(TAG + " SkinDiseaseData", "Skin Disease Treatment: " + this.skinDiseaseTreatment);
                Log.d(TAG + " SkinDiseaseData", "Skin Disease Recommendation: " + this.skinDiseaseRecommendation);
                Log.d(TAG + " SkinDiseaseData", "Skin Disease Image URL: " + this.skinDiseaseImageURL);
            } else {
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }

    private void onRequestError(VolleyError error) {
        // Log the error response
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
    }
}
