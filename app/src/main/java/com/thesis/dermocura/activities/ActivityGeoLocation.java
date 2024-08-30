package com.thesis.dermocura.activities;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thesis.dermocura.R;
import com.thesis.dermocura.datas.LocationData;

import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityGeoLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private Spinner spinnerMarkers;
    private List<LocationData> locations; // To hold the list of locations
    private static final String TAG = "ActivityGeoLocation";
    private static final String URL = "https://backend.dermocura.net/android/fetchcliniclocation.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_geo_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the Spinner
        spinnerMarkers = findViewById(R.id.spinnerMarkers);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Button to center on the user's current location
        findViewById(R.id.btnCenterOnLocation).setOnClickListener(v -> centerOnUserLocation());

        // Set listener for the Spinner
        spinnerMarkers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                moveToSelectedMarker(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
            return;
        }

        // Enable My Location Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Get the current location of the device and set the position of the map
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                        // Fetch and display clinic locations
                        fetchClinicLocations();
                    }
                });
    }

    private void centerOnUserLocation() {
        if (mMap != null && currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    private void fetchClinicLocations() {
        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest for a POST request to the specified URL
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL,
                null, // No request body needed for this GET-like request
                this::onRequestSuccess,
                this::onRequestError
        );

        // Add the request to the Volley request queue
        queue.add(request);
    }

    private void onRequestSuccess(JSONObject response) {
        try {
            // Extract success status and message from the JSON response
            boolean success = response.getBoolean("success");
            String message = response.getString("message");

            if (success) {
                // Success: Parse the data and add markers
                Log.d(TAG + " onRequestSuccess", "Message Response: " + message);
                Log.d(TAG + " onRequestSuccess", "JSON Received: " + response);

                // Initialize the locations list
                locations = new ArrayList<>();

                // Get the array of clinic locations
                JSONArray data = response.getJSONArray("data");

                // List to hold the names of the clinics for the Spinner
                List<String> clinicNames = new ArrayList<>();

                // Loop through the array and add each clinic as a marker
                for (int i = 0; i < data.length(); i++) {
                    JSONObject clinic = data.getJSONObject(i);

                    String clinicName = clinic.getString("clinicName");
                    double clinicLatitude = clinic.getDouble("clinicLatitude");
                    double clinicLongitude = clinic.getDouble("clinicLongitude");

                    // Add marker to the map
                    LatLng position = new LatLng(clinicLatitude, clinicLongitude);
                    mMap.addMarker(new MarkerOptions().position(position).title(clinicName));

                    // Add location data to the list
                    locations.add(new LocationData(clinicLatitude, clinicLongitude, clinicName, "Marker"));

                    // Add clinic name to the list for the Spinner
                    clinicNames.add(clinicName);
                }

                // Populate the Spinner with the clinic names
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clinicNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMarkers.setAdapter(adapter);
            } else {
                // Failed to fetch locations
                Log.e(TAG + " onRequestSuccess", "Message Response: " + message);
            }
        } catch (JSONException e) {
            Log.e(TAG + " onRequestSuccess", String.valueOf(e));
            Log.e(TAG + " onRequestSuccess", "Error parsing JSON response");
        }
    }

    private void onRequestError(VolleyError error) {
        // Log the error
        Log.e(TAG + " onRequestError", "Error Response: " + error.getMessage());
    }

    private void moveToSelectedMarker(int position) {
        if (locations != null && !locations.isEmpty()) {
            LocationData selectedLocation = locations.get(position);
            LatLng latLng = new LatLng(selectedLocation.getLatitude(), selectedLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    private String getCityFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                Log.d(TAG + " getCityFromLocation", "User City: " + city); // Add this line to log the city
                return city; // Returns the city name
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<LocationData> filterLocationsByCity(String userCity, List<LocationData> allLocations) {
        List<LocationData> filteredLocations = new ArrayList<>();
        for (LocationData locationData : allLocations) {
            if (userCity != null && userCity.equalsIgnoreCase(locationData.getCity())) {
                filteredLocations.add(locationData);
            }
        }
        return filteredLocations;
    }

    private void addMarkers(List<LocationData> locations) {
        for (LocationData locationData : locations) {
            LatLng position = new LatLng(locationData.getLatitude(), locationData.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title(locationData.getName()));
        }
    }

    private List<LocationData> getAllLocations() {
        List<LocationData> locations = new ArrayList<>();

        // Example locations
        locations.add(new LocationData(6.1122175, 125.1724019, "STI Gensan", "Marker in STI Gensan"));
        // Add more locations as needed

        return locations;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, reinitialize the map
                onMapReady(mMap);
            }
        }
    }
}
