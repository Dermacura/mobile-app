package com.thesis.dermocura.activities;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.thesis.dermocura.R;
import com.thesis.dermocura.adapters.AdapterChat;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;
import com.thesis.dermocura.models.ModelChat;
import com.thesis.dermocura.utils.LoadingDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityTelemedicine extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private AdapterChat adapterChat;
    private List<ModelChat> messageList;
    private TextView tvEmptyMessages;
    private LoadingDialogFragment loadingDialog; // Declare the LoadingDialogFragment

    private int patientID;
    private int userAccID;

    private static final String TAG = "ActivityTelemedicine";
    private static final String FETCH_URL = "https://backend.dermocura.net/android/fetchmessages.php";
    private static final String SEND_URL = "https://backend.dermocura.net/android/sendmessage.php";

    private Handler handler = new Handler();
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_telemedicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve userAccID and doctor's name from the intent
        userAccID = getIntent().getIntExtra("userAccID", 0);
        String doctorName = getIntent().getStringExtra("doctorName");

        // Set the doctor's name to the tvPageTitle TextView
        TextView tvPageTitle = findViewById(R.id.tvPageTitle);
        if (doctorName != null) {
            tvPageTitle.setText(doctorName);
        } else {
            tvPageTitle.setText("Doctor");
        }

        // Initialize other views and fetch patientID
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        tvEmptyMessages = findViewById(R.id.tvNoMessages); // Initialize the empty message TextView
        loadingDialog = new LoadingDialogFragment(); // Initialize the loading dialog

        // Set up RecyclerView and adapter
        messageList = new ArrayList<>();
        adapterChat = new AdapterChat(messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(adapterChat);

        // Fetch the patientID from the SharedPreferences
        MySharedPreferences prefs = MySharedPreferences.getInstance(this);
        UserData userData = prefs.getUserData();
        if (userData != null) {
            patientID = userData.getPatientID();
        } else {
            Log.e(TAG, "User data not found in SharedPreferences.");
        }

        // Show loading dialog and fetch the messages
        showLoadingDialog();
        makeFetchRequest();

        // Set up auto-refresh for messages
        refreshRunnable = () -> {
            makeFetchRequest();
            handler.postDelayed(refreshRunnable, 30000); // 30 seconds interval
        };
        handler.postDelayed(refreshRunnable, 30000);

        // Set click listener for the send button
        buttonSend.setOnClickListener(v -> {
            String messageContent = editTextMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                editTextMessage.setText(""); // Clear the input field after sending
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the callbacks to prevent memory leaks
        handler.removeCallbacks(refreshRunnable);
    }

    private void makeFetchRequest() {
        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        try {
            requestBody.put("patientID", patientID);
            requestBody.put("userAccID", userAccID);
        } catch (JSONException e) {
            Log.e(TAG + " makeFetchRequest", String.valueOf(e));
            dismissLoadingDialog();
            return;
        }

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest for a POST request to fetch messages
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                FETCH_URL,
                requestBody,
                this::onFetchSuccess,
                this::onRequestError
        );

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void sendMessage(String messageContent) {
        // Create a JSON object for the request body
        JSONObject requestBody = new JSONObject();

        try {
            // Get current timestamp in the format 'yyyy-MM-dd HH:mm:ss'
            String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Add the parameters to the JSON object
            requestBody.put("patientID", patientID);
            requestBody.put("userAccID", userAccID);
            requestBody.put("telemedicineContent", messageContent);
            requestBody.put("telemedicineTime", currentTimestamp);  // Add the generated timestamp
            requestBody.put("recipientRole", 0);  // Assuming 0 means outgoing
        } catch (JSONException e) {
            Log.e(TAG + " sendMessage", String.valueOf(e));
            return;
        }

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest for a POST request to send the message
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                SEND_URL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Log.d(TAG + " sendMessage", "Message sent successfully: " + message);

                            // Add the message to the RecyclerView (No profile image for outgoing)
                            ModelChat newMessage = new ModelChat(messageContent, "Now", true, null);
                            messageList.add(newMessage);
                            adapterChat.notifyItemInserted(messageList.size() - 1);
                            recyclerViewMessages.scrollToPosition(messageList.size() - 1);

                            // Refresh the message list
                            makeFetchRequest();
                        } else {
                            Log.e(TAG + " sendMessage", "Failed to send message: " + message);
                            Toast.makeText(this, "Failed to send message: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG + " sendMessage", String.valueOf(e));
                    }
                },
                this::onRequestError
        );

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void onFetchSuccess(JSONObject response) {
        dismissLoadingDialog();
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Log.d(TAG + " onFetchSuccess", "Messages retrieved successfully: " + message);
                JSONArray dataArray = response.getJSONArray("data");

                messageList.clear();  // Clear the previous messages

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject jsonObject = dataArray.getJSONObject(i);

                    String telemedicineContent = jsonObject.getString("telemedicineContent");
                    String telemedicineTime = jsonObject.getString("telemedicineTime");
                    boolean isOutgoing = jsonObject.getString("recipientRole").equals("outgoing");

                    // Use profile image URL only for incoming messages
                    String profileImageUrl = null;
                    if (!isOutgoing) {
                        profileImageUrl = jsonObject.getString("profile");
                    }

                    // Create the message model with profile image URL for incoming messages
                    ModelChat messageItem = new ModelChat(telemedicineContent, telemedicineTime, isOutgoing, profileImageUrl);
                    messageList.add(messageItem);
                }

                // Check if the list is empty after fetching
                if (messageList.isEmpty()) {
                    tvEmptyMessages.setVisibility(View.VISIBLE); // Show the empty message text
                } else {
                    tvEmptyMessages.setVisibility(View.GONE); // Hide the empty message text
                }

                adapterChat.notifyDataSetChanged();  // Notify the adapter to refresh the view

                // Scroll to the last message
                if (!messageList.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1); // Scroll to the bottom
                }

            } else {
                Log.e(TAG + " onFetchSuccess", "Failed to retrieve messages: " + message);
                tvEmptyMessages.setVisibility(View.VISIBLE); // Show the empty message text
            }
        } catch (JSONException e) {
            Log.e(TAG + " onFetchSuccess", String.valueOf(e));
        }
    }

    private void onRequestError(VolleyError error) {
        dismissLoadingDialog();
        Log.e(TAG + " onRequestError", "Error: " + error.getMessage());
        Toast.makeText(this, "Failed to fetch/send messages", Toast.LENGTH_SHORT).show();
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
            loadingDialog.dismissAllowingStateLoss();
        }
    }
}
