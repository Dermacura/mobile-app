package com.thesis.dermocura;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.thesis.dermocura.activities.ActivityDashboard;
import com.thesis.dermocura.activities.ActivityDiseaseInfo;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.*;

import com.thesis.dermocura.activities.ActivityLogin;
import com.thesis.dermocura.service.MessagePollingService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the UserData object from SharedPreferences
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();

        // Check if userData exists and print appropriate message
        if (userData != null) {
            Intent serviceIntent = new Intent(this, MessagePollingService.class);
            startService(serviceIntent);
            Intent intentDashboard = new Intent(MainActivity.this, ActivityDashboard.class);
            startActivity(intentDashboard);
            finish();
        } else {
            Intent intentLogin = new Intent(MainActivity.this, ActivityLogin.class);
            startActivity(intentLogin);
            finish();
        }
    }
}