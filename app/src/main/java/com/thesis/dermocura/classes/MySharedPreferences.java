package com.thesis.dermocura.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import com.thesis.dermocura.datas.*;

public class MySharedPreferences {
    private static final String TAG = "MySharedPreferences";
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String USER_DATA_KEY = "userData";
    private static MySharedPreferences instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

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
}
