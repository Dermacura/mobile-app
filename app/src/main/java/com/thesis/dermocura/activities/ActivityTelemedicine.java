package com.thesis.dermocura.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.thesis.dermocura.models.ModelChat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityTelemedicine extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private AdapterChat adapterChat;
    private List<ModelChat> messageList;

    private int patientID = 2;
    private int userAccID = 0;

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

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        messageList = new ArrayList<>();
        adapterChat = new AdapterChat(messageList);

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(adapterChat);

        makeFetchRequest();

        // Create the Runnable to refresh messages every 30 seconds
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                makeFetchRequest();
                handler.postDelayed(this, 30000); // 30 seconds interval
            }
        };

        // Start the refresh Runnable
        handler.postDelayed(refreshRunnable, 30000);

        // Set up the send button click listener
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = editTextMessage.getText().toString().trim();
                if (!messageContent.isEmpty()) {
                    sendMessage(messageContent);
                    editTextMessage.setText(""); // Clear the input field
                }
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
            requestBody.put("patientID", patientID);
            requestBody.put("clinicInfoID", userAccID);
            requestBody.put("telemedicineContent", messageContent);
            requestBody.put("recipientRole", 0); // Assuming 0 means outgoing
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

                            // Add the message to the RecyclerView
                            ModelChat newMessage = new ModelChat(messageContent, "Now", true);
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
        try {
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                Log.d(TAG + " onFetchSuccess", "Messages retrieved successfully: " + message);
                JSONArray dataArray = response.getJSONArray("data");

                messageList.clear();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject jsonObject = dataArray.getJSONObject(i);

                    String telemedicineContent = jsonObject.getString("telemedicineContent");
                    String telemedicineDate = jsonObject.getString("telemedicineDate");
                    boolean isOutgoing = jsonObject.getString("recipientRole").equals("outgoing");

                    ModelChat messageItem = new ModelChat(telemedicineContent, telemedicineDate, isOutgoing);
                    messageList.add(messageItem);
                }

                adapterChat.notifyDataSetChanged();
            } else {
                Log.e(TAG + " onFetchSuccess", "Failed to retrieve messages: " + message);
                Toast.makeText(this, "Failed to retrieve messages: " + message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG + " onFetchSuccess", String.valueOf(e));
            Toast.makeText(this, "Error parsing messages", Toast.LENGTH_SHORT).show();
        }
    }


    private void onRequestError(VolleyError error) {
        Log.e(TAG + " onRequestError", "Error: " + error.getMessage());
        Toast.makeText(this, "Failed to fetch/send messages", Toast.LENGTH_SHORT).show();
    }
}