package com.thesis.dermocura.datas;

import android.util.Log;

public class ScanData {

    private static ScanData instance;

    private static final String TAG = "ClassScanData";
    private String base64Image; // Add this to store the Base64 image string
    private String duration;
    private String skinType;
    private String additional;

    private ScanData() {
        // Initialize variables if needed
    }

    public static synchronized ScanData getInstance() {
        if (instance == null) {
            instance = new ScanData();
        }
        return instance;
    }

    // Get and Set methods for Base64 Image
    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
        Log.d(TAG + " setBase64Image", "Base64 image set: " + (base64Image != null ? base64Image.substring(0, 30) : "null")); // Log part of the base64 for safety
    }

    // Existing methods for duration, skinType, and additional information
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
        Log.d(TAG + " setDuration", "Duration received: " + duration);
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skin_type) {
        this.skinType = skin_type;
        Log.d(TAG + " setSkinType", "Skin Type received: " + skin_type);
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
        Log.d(TAG + " setAdditional", "Additional Info received: " + additional);
    }

    public void clearScanData() {
        this.base64Image = null;
        this.duration = null;
        this.skinType = null;
        this.additional = null;
    }
}
