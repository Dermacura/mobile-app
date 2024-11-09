package com.thesis.dermocura.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thesis.dermocura.R;
import com.thesis.dermocura.classes.ClassBase64Convert;
import com.thesis.dermocura.datas.ScanData;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class ActivityCamera extends AppCompatActivity {

    // Declare Views
    ImageButton ibLeftArrow, ibRightArrow, ibGallery, ibCamera, ibSend;
    ImageView ivCameraPreview;
    TextView tvPageTitle, tvHint;
    LinearLayout llHeader, llControls;
    CardView cvPreviewFrame;

    // Declare Strings
    private static final String TAG = "ActivityCamera";
    private static final int CAMERA_PERMISSION_CODE = 300;
    private Uri imageUri;
    public String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Methods
        initializeObjects();
        setOnClickListeners();
    }

    private void initializeObjects() {
        ibLeftArrow = findViewById(R.id.ibLeftArrow);
        ibRightArrow = findViewById(R.id.ibRightArrow);
        ibGallery = findViewById(R.id.ibGallery);
        ibSend = findViewById(R.id.ibSend);
        ibCamera = findViewById(R.id.ibCamera);

        tvPageTitle = findViewById(R.id.tvPageTitle);
        tvHint = findViewById(R.id.tvHint);

        ivCameraPreview = findViewById(R.id.ivCameraPreview);

        llHeader = findViewById(R.id.llHeader);
        llControls = findViewById(R.id.llControls);

        cvPreviewFrame = findViewById(R.id.cvPreviewFrame);
    }

    private void setOnClickListeners() {
        ibCamera.setOnClickListener(v -> clickCamera());
        ibGallery.setOnClickListener(v -> clickGallery());
        ibSend.setOnClickListener(v -> clickSend());
        ibLeftArrow.setOnClickListener(v -> clickLeftArrow());
    }

    private void clickSend() {
        Intent intentFirstInfo = new Intent(ActivityCamera.this, ActivityScanInfo.class);
        startActivity(intentFirstInfo);
    }

    private void clickLeftArrow() {
        Intent intent = new Intent(ActivityCamera.this, ActivityDashboard.class);
        startActivity(intent);
        finish();
    }

    private void clickCamera() {
        if (isCameraPermissionGranted()) {
            openCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void clickGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 200);
    }

    private boolean isCameraPermissionGranted() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        String[] permissions = {Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) == null) {
            Log.e(TAG, "No camera app available to handle the intent");
            return;
        }

        File photoFile = createPhotoFileSafely();
        if (photoFile == null) {
            Log.e(TAG, "Failed to create photo file");
            return;
        }

        imageUri = getUriForFile(photoFile);
        if (imageUri == null) {
            Log.e(TAG, "Failed to get URI for photo file");
            return;
        }

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, 100);
    }

    private File createPhotoFileSafely() {
        // Initialize the file to null
        File photoFile = null;
        // Attempt to create the image file
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error occurred while creating the file", ex);
        }
        // Return the created file, or null if an error occurred
        return photoFile;
    }

    private Uri getUriForFile(File photoFile) {
        // Define the authority string for the FileProvider
        String authority = "com.thesis.dermocura.fileprovider";
        // Return the generated URI
        return FileProvider.getUriForFile(this, authority, photoFile);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                // Convert the imageUri to Base64 directly after capturing from the camera
                convertAndDisplayImage(imageUri);
            } else if (requestCode == 200 && data != null && data.getData() != null) {
                // Convert the imageUri to Base64 after selecting from the gallery
                imageUri = data.getData();
                convertAndDisplayImage(imageUri);
            }
        }
    }

    private void convertAndDisplayImage(Uri uri) {
        try {
            // Convert image to Base64
            String base64Image = ClassBase64Convert.convertUriToBase64(this, uri);

            // Store Base64 string in ScanData
            ScanData.getInstance().setBase64Image(base64Image); // Store the Base64 string

            // Display the image in ImageView
            ivCameraPreview.setImageURI(uri);

        } catch (IOException e) {
            Log.e(TAG, "Failed to convert Uri to Base64", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Log.e(TAG, "Camera Permission Denied");
            }
        }
    }
}
