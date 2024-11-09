package com.thesis.dermocura.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.MySharedPreferences;
import com.thesis.dermocura.datas.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class ActivityProfile extends AppCompatActivity {

    private static final String TAG = "ActivityProfile";
    private static final String UPDATE_URL = "https://backend.dermocura.net/android/authentication/patientImage.php";

    private TextView tvName, tvEmail;
    private MaterialButton btnLogout, btnEditProfile;
    private ImageView profileImageView;
    private MySharedPreferences prefs;
    private UserData userData;

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate: Initializing Views");

        // Initialize Views
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        profileImageView = findViewById(R.id.circularImageView); // ImageView for profile picture
        btnLogout = findViewById(R.id.btnContinue);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Retrieve UserData from MySharedPreferences
        prefs = MySharedPreferences.getInstance(this);
        userData = prefs.getUserData();

        if (userData != null) {
            // Set data to the Views
            tvName.setText(userData.getPatientName());
            tvEmail.setText(userData.getPatientEmail());

            // Load the image into the ImageView using Glide
            String imageUrl = userData.getPatientImageURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.default_placeholder) // Default image
                        .error(R.drawable.default_placeholder)      // In case of error
                        .into(profileImageView);
                Log.d(TAG, "onCreate: Profile image loaded from URL: " + imageUrl);
            } else {
                // Set default image if no image URL is available
                profileImageView.setImageResource(R.drawable.default_placeholder);
                Log.d(TAG, "onCreate: No profile image found, setting default image.");
            }
        } else {
            Log.e(TAG, "onCreate: UserData is null. Unable to retrieve user information.");
        }

        // Logout functionality
        btnLogout.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Logging out");
            prefs.clearUserData();
            Intent intent = new Intent(ActivityProfile.this, ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Edit Profile Button Click Listener
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Set image view click listener to select image
        profileImageView.setOnClickListener(v -> showImageSelectionDialog());
    }

    private void showImageSelectionDialog() {
        String[] options = {"Choose from Gallery", "Take a Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Log.d(TAG, "showImageSelectionDialog: Opening gallery");
                openGallery();
            } else {
                Log.d(TAG, "showImageSelectionDialog: Opening camera");
                takePhoto();
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "takePhoto: Image file created: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e(TAG, "takePhoto: Error creating image file: " + ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.thesis.dermocura.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        } else {
            Log.e(TAG, "takePhoto: Camera app not available.");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                Log.d(TAG, "onActivityResult: Image selected from gallery: " + selectedImageUri.toString());
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    handleImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onActivityResult: Error loading image from gallery: " + e.getMessage());
                }
            } else if (requestCode == TAKE_PHOTO) {
                File file = new File(currentPhotoPath);
                if (file.exists()) {
                    Log.d(TAG, "onActivityResult: Photo taken and saved: " + currentPhotoPath);
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    handleImage(bitmap);
                } else {
                    Log.e(TAG, "onActivityResult: Photo file not found at: " + currentPhotoPath);
                }
            }
        } else {
            Log.e(TAG, "onActivityResult: Result not OK.");
        }
    }

    private void handleImage(Bitmap bitmap) {
        Log.d(TAG, "handleImage: Handling selected/taken image.");
        profileImageView.setImageBitmap(bitmap);
        String base64Image = encodeImageToBase64(bitmap);
        uploadImage(base64Image);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        Log.d(TAG, "encodeImageToBase64: Image encoded to Base64.");
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void uploadImage(String base64Image) {
        Log.d(TAG, "uploadImage: Uploading image to server.");
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JSON request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", userData.getPatientID());
            requestBody.put("base64Image", base64Image);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "uploadImage: Error creating JSON request body: " + e.getMessage());
        }

        // Create a POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                UPDATE_URL,
                requestBody,
                response -> {
                    try {
                        Log.d(TAG, "uploadImage: Response: " + response.toString());
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String imageUrl = response.getString("imageUrl");
                            Log.d(TAG, "uploadImage: Image uploaded successfully, URL: " + imageUrl);

                            // Save the new image URL in SharedPreferences
                            userData.setPatientImageURL(imageUrl); // Update userData object
                            prefs.saveUserData(userData); // Save updated userData in SharedPreferences

                            // Update ImageView with the new image
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_placeholder)
                                    .error(R.drawable.default_placeholder)
                                    .into(profileImageView);

                            Toast.makeText(ActivityProfile.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = response.getString("message");
                            Log.e(TAG, "uploadImage: Image upload failed: " + message);
                            Toast.makeText(ActivityProfile.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "uploadImage: Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    String responseBody = new String(error.networkResponse.data);
                    Log.e(TAG, "uploadImage: Server error response: " + responseBody);
                    Toast.makeText(ActivityProfile.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the Volley queue
        queue.add(jsonObjectRequest);
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        // Initialize the EditTexts and RadioGroup from the dialog
        EditText etEditName = dialogView.findViewById(R.id.etEditName);
        EditText etEditEmail = dialogView.findViewById(R.id.etEditEmail);
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);

        // Set current values
        etEditName.setText(userData.getPatientName());
        etEditEmail.setText(userData.getPatientEmail());

        // Pre-select the current gender
        String currentGender = userData.getPatientGender();
        if ("Male".equalsIgnoreCase(currentGender)) {
            rgGender.check(R.id.rbMale);
        } else if ("Female".equalsIgnoreCase(currentGender)) {
            rgGender.check(R.id.rbFemale);
        } else {
            rgGender.check(R.id.rbOther);
        }

        // Get the Save and Cancel buttons
        MaterialButton btnSaveProfile = dialogView.findViewById(R.id.btnSaveProfile);
        MaterialButton btnCancelProfile = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        // Set up the Save button click listener
        btnSaveProfile.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();
            String newEmail = etEditEmail.getText().toString().trim();

            // Determine selected gender
            String newGender = "";
            int selectedGenderId = rgGender.getCheckedRadioButtonId();

            if (selectedGenderId == R.id.rbMale) {
                newGender = "Male";
            } else if (selectedGenderId == R.id.rbFemale) {
                newGender = "Female";
            } else if (selectedGenderId == R.id.rbOther) {
                newGender = "Other";
            }

            if (!newName.isEmpty() && !newEmail.isEmpty()) {
                Log.d(TAG, "showEditProfileDialog: Updating profile information.");
                updateProfile(newName, newEmail, newGender);
                dialog.dismiss();
            } else {
                Log.e(TAG, "showEditProfileDialog: All fields must be filled out.");
                Toast.makeText(ActivityProfile.this, "All fields must be filled out!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the Cancel button click listener
        btnCancelProfile.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Method to update multiple fields
    private void updateProfile(String newName, String newEmail, String newGender) {
        // Check if the name has changed
        if (!newName.equals(userData.getPatientName())) {
            Log.d(TAG, "updateProfile: Updating name.");
            updateUserDataInDatabase("patientName", userData.getPatientName(), newName, success -> {
                if (success) {
                    userData.setPatientName(newName);
                    tvName.setText(newName);
                    prefs.saveUserData(userData);
                } else {
                    Log.e(TAG, "updateProfile: Failed to update name.");
                    Toast.makeText(ActivityProfile.this, "Failed to update name.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Check if the email has changed
        if (!newEmail.equals(userData.getPatientEmail())) {
            Log.d(TAG, "updateProfile: Updating email.");
            updateUserDataInDatabase("patientEmail", userData.getPatientEmail(), newEmail, success -> {
                if (success) {
                    userData.setPatientEmail(newEmail);
                    tvEmail.setText(newEmail);
                    prefs.saveUserData(userData);
                } else {
                    Log.e(TAG, "updateProfile: Failed to update email.");
                    Toast.makeText(ActivityProfile.this, "Failed to update email.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Check if the gender has changed
        if (!newGender.equals(userData.getPatientGender())) {
            Log.d(TAG, "updateProfile: Updating gender.");
            updateUserDataInDatabase("patientGender", userData.getPatientGender(), newGender, success -> {
                if (success) {
                    userData.setPatientGender(newGender);
                    prefs.saveUserData(userData);
                } else {
                    Log.e(TAG, "updateProfile: Failed to update gender.");
                    Toast.makeText(ActivityProfile.this, "Failed to update gender.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUserDataInDatabase(String field, String oldValue, String newValue, UpdateCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the request body with patientID, field, oldValue, and newValue
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("patientID", userData.getPatientID());
            requestBody.put("field", field);
            requestBody.put("oldValue", oldValue);
            requestBody.put("newValue", newValue);
            Log.d(TAG, "updateUserDataInDatabase: Updating Field: " + field + ", Old Value: " + oldValue + ", New Value: " + newValue);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "updateUserDataInDatabase: Error creating JSON request body: " + e.getMessage());
            callback.onUpdate(false);
            return;
        }

        // Create a request to update the server
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                UPDATE_URL,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        callback.onUpdate(success);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "updateUserDataInDatabase: Error parsing response: " + e.getMessage());
                        callback.onUpdate(false);
                    }
                },
                error -> {
                    Log.e(TAG, "updateUserDataInDatabase: Network error: " + error.getMessage());
                    callback.onUpdate(false);
                }
        );

        // Add the request to the Volley queue
        queue.add(request);
    }

    interface UpdateCallback {
        void onUpdate(boolean success);
    }
}
