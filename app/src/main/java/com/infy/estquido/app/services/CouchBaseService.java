package com.infy.estquido.app.services;

import android.location.Location;
import android.util.Log;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.infy.estquido.app.This;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CouchBaseService {

    private static final String TAG = CouchBaseService.class.getName();

    private static DatabaseConfiguration dbConfig;
    private static Database database;
    private static URLEndpoint endpoint;
    private static ReplicatorConfiguration reConfig;

    private static String center;

    static {

        dbConfig = new DatabaseConfiguration(This.CONTEXT.get());
        try {
            database = new Database(This.Static.COUCHBASE_DB, dbConfig);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        try {
            endpoint = new URLEndpoint(new URI(This.Static.COUCHBASE_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        reConfig = new ReplicatorConfiguration(database, endpoint);
        reConfig.setAuthenticator(new BasicAuthenticator(This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
    }

    public static void inferCenter(Location location) {
        reConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PULL);
        reConfig.setDocumentIDs(Arrays.asList("centers"));
        Replicator replicator = new Replicator(reConfig);
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                Log.e(TAG, "ERROR");
            }
            if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED)) {
                Document centers = database.getDocument("centers");
                Log.i(TAG, "CENTERS: " + centers.toMap().toString());
                Map<String, Object> map = centers.toMap();
                Optional<Map.Entry<String, Object>> min = map.entrySet().stream().min((Map.Entry<String, Object> e1, Map.Entry<String, Object> e2) -> {
                    HashMap<String, Object> map1 = (HashMap<String, Object>) e1.getValue();
                    HashMap<String, Object> map2 = (HashMap<String, Object>) e2.getValue();
                    ArrayList<Double> location1 = (ArrayList<Double>) map1.get("location");
                    ArrayList<Double> location2 = (ArrayList<Double>) map2.get("location");

                    Location l1 = new Location("A");
                    l1.setLatitude(location1.get(0));
                    l1.setLongitude(location1.get(1));

                    Location l2 = new Location("B");
                    l2.setLatitude(location2.get(0));
                    l2.setLongitude(location2.get(1));

                    return Float.compare(location.distanceTo(l1), location.distanceTo(l2));
                });
                center = min.get().getKey();
                Log.i(TAG, "CENTER: " + center);
            }
        });
        replicator.start();


    }


}
