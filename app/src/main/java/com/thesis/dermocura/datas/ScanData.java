package com.thesis.dermocura.datas;

import android.net.Uri;
import android.util.Log;

public class ScanData {

    private static ScanData instance;

    private static final String TAG = "ClassScanData";
    private Uri imageUri;
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

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
        Log.d(TAG + " setImageUri", "uri received: " + imageUri.toString());
    }

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
        Log.d(TAG + " setDuration", "Duration received: " + skin_type);
    }

    public String getAdditional() {
        return additional;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
        Log.d(TAG + " setDuration", "Duration received: " + additional);
    }

    public void clearScanData() {
        this.imageUri = null;
        this.duration = null;
        this.skinType = null;
        this.additional = null;
    }
}
