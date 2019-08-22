package com.infy.estquido;

import android.os.Bundle;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.FullTextIndexItem;
import com.couchbase.lite.IndexBuilder;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.sceneform.math.Vector3;
import com.infy.estquido.utility.DatabaseManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.infy.estquido.utility.DatabaseManager.mSyncGatewayEndpoint;

public class SyncActivity extends AppCompatActivity {

    private static final String DB_NAME = "Estquido";
    private Database database;
    private MutableDocument document;
    private Set<WayPoint> mWayPoints = Collections.synchronizedSet(new LinkedHashSet<>());
    private int wayPointCounter = 0;
    private DatabaseManager dbMgr;
    public static String mSyncGatewayEndpoint = "ws://192.168.43.227:4984/hackathon";
    private ReplicatorConfiguration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        initDB();
    }

    private void initDB() {
        DatabaseConfiguration config = new DatabaseConfiguration(getApplicationContext());
        try {
            database = new Database(DB_NAME, config);
            createFTSQueryIndex();
            Document doc = database.getDocument("B32F0");

            if (doc == null) {
                document = new MutableDocument("B32F0");
                document.setValue("WayPoints", new ArrayList<Map<String, Object>>());
                document.setValue("WayPointIDs", new ArrayList<Integer>(Arrays.asList(0)));
                database.save(document);
            } else {
                document = doc.toMutable();
                Map<Integer, WayPoint> newWayPoints = Collections.synchronizedMap(new LinkedHashMap<>());
                ((MutableArray) document.getValue("WayPoints")).toList().stream().forEachOrdered(m -> {
                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) m;
                    Map<String, Object> wpMap = map.values().stream().findFirst().get();
                    WayPoint wayPoint = new WayPoint(((Long) wpMap.get("id")).intValue(), new Vector3((float) wpMap.get("x"), (float) wpMap.get("y"), (float) wpMap.get("z")));
                    mWayPoints.add(wayPoint);
                    newWayPoints.put(wayPoint.getId(), wayPoint);
                });
                wayPointCounter = ((MutableArray) document.getValue("WayPointIDs")).toList().stream().mapToInt(value -> ((Long) value).intValue()).max().getAsInt();
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void createFTSQueryIndex() {
        try {
            database.createIndex("descFTSIndex", IndexBuilder.fullTextIndex(FullTextIndexItem.property("description")));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void sync(View view) {

        startPushAndPullReplicationForCurrentUser("esquido","esquido");
    }

    public void startPushAndPullReplicationForCurrentUser(String username, String password) {
        URI url = null;
        try {
            url = new URI(mSyncGatewayEndpoint);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        config = new ReplicatorConfiguration(database, new URLEndpoint(url));
        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
        config.setContinuous(true);
        config.setAuthenticator(new BasicAuthenticator(username, password));

        Replicator replicator = new Replicator(config);
        replicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {

                if (change.getReplicator().getStatus().getActivityLevel().equals(Replicator.ActivityLevel.IDLE)) {

                    Log.e("Replication Comp Log", "Schedular Completed");
                    Toast.makeText(getApplicationContext(),"replciation completed log",Toast.LENGTH_LONG).show();

                }
                if (change.getReplicator().getStatus().getActivityLevel().equals(Replicator.ActivityLevel.STOPPED) || change.getReplicator().getStatus().getActivityLevel().equals(Replicator.ActivityLevel.OFFLINE)) {
                    // stopReplication();
                    Toast.makeText(getApplicationContext(),"Rep schedular  Log",Toast.LENGTH_LONG).show();
                    Log.e("Rep schedular  Log", "ReplicationTag Stopped");
                }
            }
        });
        replicator.start();
    }
}

