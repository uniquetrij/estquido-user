package com.infy.estquido;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.infy.estquido.app.This;
import com.infy.estquido.app.services.EstquidoCBLService;
import com.infy.estquido.app.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private FusedLocationProviderClient mFusedLocationClient;
    private Uri mGoogleMapsIntentURI;

    private AutoCompleteTextView tv_destinationName;

    private String center;
    private Map<String, Object> spots;
    private String building;
    private String destination;
    private Location location;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CAMERA_REQUEST_CODE = 0;
    private boolean mPermissionDenied = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());
        This.MAIN_ACTIVITY.set(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tv_destinationName = findViewById(R.id.tv_destinationName);

        enablePermissions();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, location -> {
                            if (location != null) {
                                MainActivity.this.location = location;
                                EstquidoCBLService.inferCenter(location, center -> {
                                    MainActivity.this.center = center;
                                    EstquidoCBLService.fetchSpots(center, map -> {
                                        MainActivity.this.spots = map;
                                        String[] array = map.keySet().toArray(new String[0]);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, array);
                                        tv_destinationName.setAdapter(adapter);
                                    });
                                });
                                timer.cancel();
                            }
                        });
            }
        }, 0, 1000);
    }

    private void enablePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, REQUEST_CAMERA_REQUEST_CODE,
                    Manifest.permission.CAMERA, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Enable the my location layer if the permission has been granted.
                enablePermissions();
            }
        }
        else if (requestCode == REQUEST_CAMERA_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.CAMERA)) {
                // Enable the my location layer if the permission has been granted.
                enablePermissions();
            } else {
                // Display the missing permission error dialog when the fragments resume.
                mPermissionDenied = true;
            }
        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void startOutdoorNav(View view) {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mGoogleMapsIntentURI);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    public void searchDestination(View view) {
        destination = tv_destinationName.getText().toString();
        if (spots.containsKey(destination)) {
            Map<String, Object> map = (Map<String, Object>) spots.get(destination);
            building = (String) map.get("building");
            Long floor = (Long) map.get("floor");
            EstquidoCBLService.fetchBuilding(center, building, new EstquidoCBLService.OnBuildingsFetchedCallback() {
                @Override
                public void onBuildingFetched(Map<String, Object> map) {
                    Log.i("INFO", map.toString());
                    ArrayList<Double> location = (ArrayList<Double>) map.get("location");
                    mGoogleMapsIntentURI = Uri.parse("geo:0,0?q=" + location.get(0) + ", " + location.get(1));
                }
            });

        }
    }

    public void startIndoorNav(View view) {
        Intent intent = new Intent(MainActivity.this, NavigateActivity.class);
        intent.putExtra("center", center);
        intent.putExtra("location", location);
        intent.putExtra("building", building);
        startActivity(intent);
    }
}