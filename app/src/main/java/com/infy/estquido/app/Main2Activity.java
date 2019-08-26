package com.infy.estquido.app;

import android.location.Location;
import android.os.Bundle;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.infy.estquido.R;
import com.infy.estquido.app.model.GeoSpatialRequest;
import com.infy.estquido.app.model.Query;
import com.infy.estquido.app.services.EstquidoCBLService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        checkVolley();
        try {
            inferCenter();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void inferCenter() throws JSONException {

        try {

            RequestQueue queue = Volley.newRequestQueue(Main2Activity.this);

            String userValidationURL = EstquidoCBLService.inferCenterUrl;


            JSONObject jsonWholeObject = new JSONObject();
            JSONObject jsonQueryObject = new JSONObject();
            JSONObject jsonLocationObject = new JSONObject();

            jsonLocationObject.put("lat",20.299358);
            jsonLocationObject.put("lon",85.825143);


            jsonQueryObject.put("location",jsonLocationObject);
            jsonQueryObject.put("distance","200mi");
            jsonQueryObject.put("field","geo");

            jsonWholeObject.put("from", 0);
            jsonWholeObject.put("size",2);
            jsonWholeObject.put("query",jsonQueryObject);

            final String[] mRequestBody = {jsonWholeObject.toString()};

            StringRequest request = new StringRequest(Request.Method.POST, userValidationURL, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {

                    Log.d("volley", response );
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.d("volley", response );

                        JSONArray nearestCentres=jsonObject.getJSONArray("hits");
                        JSONObject nearestCentre=(JSONObject) nearestCentres.get(0);
                        Log.d("volley",nearestCentre.get("id").toString());


                    }catch (JSONException err){
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
                    String json="";
                    try {
                        json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        Log.d("volley",json);
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
    }

}
