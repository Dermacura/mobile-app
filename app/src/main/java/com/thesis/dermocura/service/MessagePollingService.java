package com.thesis.dermocura.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.thesis.dermocura.MainActivity;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;

public class MessagePollingService extends Service {

    private static final long POLLING_INTERVAL = 15 * 1000;  // Poll every 60 seconds
    private Handler handler;
    private MessageChecker messageChecker;
    private StatusChecker statusChecker;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "message_polling_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

        // Retrieve the patientID from SharedPreferences
        MySharedPreferences sharedPreferences = MySharedPreferences.getInstance(getApplicationContext());
        UserData userData = sharedPreferences.getUserData();
        if (userData != null) {
            int patientID = userData.getPatientID();  // Get patientID from stored user data

            // Initialize MessageChecker and StatusChecker
            messageChecker = new MessageChecker(getApplicationContext(), patientID);
            statusChecker = new StatusChecker(getApplicationContext(), patientID);
        }

        // Start the polling
        startPolling();

        // Create notification channel and start the foreground service
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildForegroundNotification().build());
    }

    // Periodic polling using Handler
    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check for new messages
                if (messageChecker != null) {
                    messageChecker.checkForNewMessages();
                }

                // Check for appointment status updates
                if (statusChecker != null) {
                    statusChecker.checkForStatusUpdates();
                }

                // Re-run this runnable in the next interval
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        }, POLLING_INTERVAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;  // Ensures the service is restarted if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);  // Stop polling when the service is destroyed
    }

    // Create a notification to keep the service running in the foreground
    private NotificationCompat.Builder buildForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);  // Clicking the notification will open the app
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Message Polling Service")
                .setContentText("Checking for new messages and status updates...")
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);  // Set priority to low
    }

    // Create a notification channel (required for Android O and above)
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Message Polling Channel",
                    NotificationManager.IMPORTANCE_LOW  // Set importance to low
            );
            channel.setDescription("This notification is used by the message polling service.");

            NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

