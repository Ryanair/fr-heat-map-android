package com.ryanair.analytics.heatmap;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.support.LazyJsonObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Storage {
    private static final String DB_NAME = "frheatmap";
    private static final String SYNC_URL = "http://192.168.0.11:4984/" + DB_NAME;
    private static final String COORDINATES_VIEW = "byCoordinates";

    private static final String FIELD_X = "x";
    private static final String FIELD_Y = "y";
    private static final String FIELD_SCREEN_X = "screenX";
    private static final String FIELD_SCREEN_Y = "screenY";
    private static final String FIELD_ACTIVITY_NAME = "activity_name";
    private static final String FIELD_APP_VERSION = "app_version";


    protected static Manager manager;
    private Database database;
    Replication pushReplication;

    public Storage(Context context) throws IOException, CouchbaseLiteException {
        manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        database = manager.getDatabase(DB_NAME);

        createCoordinatesView();

        startSync();
    }

    private void createCoordinatesView() {
        final View coordinatesView = database.getView(COORDINATES_VIEW);
        coordinatesView.setMap(
                new Mapper() {
                    @Override
                    public void map(Map<String, Object> document, Emitter emitter) {
                        Object version = document.get(FIELD_APP_VERSION);
                        Object x = document.get(FIELD_X);
                        Object y = document.get(FIELD_Y);

                        HashMap<String, Object> record = null;

                        if (x != null && y != null) {
                            record = new HashMap<>();

                            record.put(FIELD_X, x);
                            record.put(FIELD_Y, y);
                            record.put(FIELD_APP_VERSION, version);
                        }

                        if (version != null && record != null) {
                            emitter.emit(version, record);
                        }
                    }
                },
                "4");

        LiveQuery liveQuery = coordinatesView.createQuery().toLiveQuery();

        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                Map<String, List<Object>> map = new HashMap<>();

                for (Iterator<QueryRow> it = event.getRows(); it.hasNext(); ) {
                    QueryRow row = it.next();
                    LazyJsonObject lazy = (LazyJsonObject) row.getValue();

                    String key = String.format("%s-%s-%s", lazy.get(FIELD_X), lazy.get(FIELD_Y), lazy.get(FIELD_APP_VERSION));

                    if (map.get(key) == null) {
                        map.put(key, new ArrayList<>());
                    }

                    HashMap<String, Object> point = new HashMap<>();
                    point.put("x", lazy.get(FIELD_X));
                    point.put("y", lazy.get(FIELD_Y));
                    point.put("version", lazy.get(FIELD_APP_VERSION));

                    map.get(key).add(point);
                }

                Log.i("frheatmap", map.toString());

                // todo store the aggregated data in another document and sync it
            }
        });

        liveQuery.start();
    }

    public void create(int x, int y, Point screenSize, String className) throws CouchbaseLiteException {
        Document document = database.createDocument();

        Map<String, Object> properties = new HashMap<>();
        properties.put(FIELD_X, x);
        properties.put(FIELD_Y, y);
        properties.put(FIELD_SCREEN_X, screenSize.x);
        properties.put(FIELD_SCREEN_Y, screenSize.y);
        properties.put(FIELD_ACTIVITY_NAME, className);
        properties.put(FIELD_APP_VERSION, BuildConfig.VERSION_CODE);

        document.putProperties(properties);
    }

    private void startSync() {
        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setContinuous(true);

        pushReplication.start();
    }

    public void close() {
        pushReplication.stop();

        database.close();
        manager.close();
    }
}
