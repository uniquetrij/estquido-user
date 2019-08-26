package com.infy.estquido;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.infy.estquido.app.Main2Activity;
import com.infy.estquido.app.NetworkRequest;
import com.infy.estquido.app.This;
import com.infy.estquido.app.model.GeoSpatialRequest;
import com.infy.estquido.app.model.Query;
import com.infy.estquido.app.services.EstquidoCBLService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private Uri mGoogleMapsIntentURI;

    private AutoCompleteTextView tv_destinationName;

    private String center;
    private Map<String, Object> spots;
    private String building;
    private String destination;
    private Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());
        This.MAIN_ACTIVITY.set(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tv_destinationName = findViewById(R.id.tv_destinationName);
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

                                try {
                                    center=inferCenter(location);
                                    Log.d("volley",center);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                    mGoogleMapsIntentURI = Uri.parse("geo:0,0?q=" + location.get(0)+", "+location.get(1));
                }
            });

        }
    }

    private String inferCenter(Location location) throws JSONException {

        String centerName="null";

        try {

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

            String userValidationURL = EstquidoCBLService.inferCenterUrl;


            JSONObject jsonWholeObject = new JSONObject();
            JSONObject jsonQueryObject = new JSONObject();
            JSONObject jsonLocationObject = new JSONObject();

            jsonLocationObject.put("lat", location.getLatitude());
            jsonLocationObject.put("lon", location.getLongitude());


            jsonQueryObject.put("location", jsonLocationObject);
            jsonQueryObject.put("distance", "200mi");
            jsonQueryObject.put("field", "geo");

            jsonWholeObject.put("from", 0);
            jsonWholeObject.put("size", 2);
            jsonWholeObject.put("query", jsonQueryObject);

            final String[] mRequestBody = {jsonWholeObject.toString()};

            StringRequest request = new StringRequest(Request.Method.POST, userValidationURL, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    Log.d("volley", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.d("volley", response);

                        JSONArray nearestCentres = jsonObject.getJSONArray("hits");
                        JSONObject nearestCentre = (JSONObject) nearestCentres.get(0);
                        Log.d("volley", nearestCentre.get("id").toString());


                    } catch (JSONException err) {
                        Log.d("Error", err.toString());
                    }

                    if (response != null && !response.startsWith("<HTML>")) {

                        Log.e("onResponse", "" + response);


                    } else {

                        Toast.makeText(getApplicationContext(), "Internet Toast Message", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null) {
                        Log.d("volley", error.toString());

                    }
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                //
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json");
                    String credentials = "Administrator:Happy#544";
                    String auth = "Basic "
                            + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    headers.put("Accept", "application/json");
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {

                        mRequestBody[0] = jsonWholeObject.toString();

                        return mRequestBody[0] == null ? null : mRequestBody[0].getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                                mRequestBody[0], "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    String json = "";
                    try {
                        json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        Log.d("volley", json);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (response != null) {
                        responseString = String.valueOf(response);
                        // can get more details such as response.headers
                    }
                    return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
                }

            };
            request.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(request);

        } catch (JSONException je) {

            je.printStackTrace();

        }

        return centerName;
    }

    public void startIndoorNav(View view) {
        Intent intent = new Intent(MainActivity.this, NavigateActivity.class);
        intent.putExtra("center", center);
        intent.putExtra("location", location);
        intent.putExtra("building", building);
        startActivity(intent);
    }
}