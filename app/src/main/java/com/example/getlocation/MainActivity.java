package com.example.getlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private TextView currentLocation;
    private TextView lastLocation;
    private TextView distance;
    private Button locationBtn;
    private LocationRequest locationRequest;
    private Location prevLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLocation = findViewById((R.id.currPos));
        lastLocation = findViewById((R.id.lastPos));
        distance = findViewById(R.id.dist);
        locationBtn = findViewById((R.id.button));

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        locationBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    if (isGPSEnabled()) {

                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .requestLocationUpdates(locationRequest, new LocationCallback() {
                                    @Override
                                    public void onLocationResult(@NonNull LocationResult locationResult) {
                                        super.onLocationResult(locationResult);

                                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                                .removeLocationUpdates(this);

                                        if (locationResult.getLocations().size() > 0) {

                                            int index = locationResult.getLocations().size() - 1;
                                            Location currentLoc = locationResult.getLocations().get(index);
                                            double latitude = locationResult.getLocations().get(index).getLatitude();
                                            double longitude = locationResult.getLocations().get(index).getLongitude();

                                            if (!currentLocation.getText().toString().equals("Not Available")){
                                                lastLocation.setText(currentLocation.getText().toString());

                                                distance.setText(String.valueOf(currentLoc.distanceTo(prevLocation)));
                                            }

                                            currentLocation.setText("Latitude:    " + latitude + "\n" +
                                                                    "Longitude: " + longitude);
                                            prevLocation = locationResult.getLocations().get(index);
                                        }
                                    }
                                }, Looper.getMainLooper());

                    } else {
                        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest);
                        builder.setAlwaysShow(true);

                        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                                .checkLocationSettings(builder.build());

                        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                                try {
                                    LocationSettingsResponse response = task.getResult(ApiException.class);
                                    Toast.makeText(MainActivity.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();

                                } catch (ApiException e) {

                                    switch (e.getStatusCode()) {
                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                            try {
                                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                                            } catch (IntentSender.SendIntentException ex) {
                                                ex.printStackTrace();
                                            }
                                            break;

                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                            
                                            break;
                                    }
                                }
                            }
                        });
                    }
                } else{
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }

        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }


}