package com.ryanair.analytics.heatmap;

import android.content.Context;
import android.graphics.Point;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.ryanair.analytics.heatmap.util.UniqueId;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private static final String DB_NAME = "frheatmap";
    private static final String SYNC_URL = "http://192.168.0.11:4984/" + DB_NAME;

    private static final String FIELD_X = "x";
    private static final String FIELD_Y = "y";
    private static final String FIELD_SCREEN_X = "screenX";
    private static final String FIELD_SCREEN_Y = "screenY";
    private static final String FIELD_ACTIVITY_NAME = "activity_name";
    private static final String FIELD_APP_VERSION = "app_version";
    private static final String FIELD_HIT_COUNT = "hit_count";

    private Context mContext;

    protected static Manager manager;
    private Database database;
    Replication pushReplication;

    public Storage(Context context) throws IOException, CouchbaseLiteException {
        mContext = context;

        manager = new Manager(new AndroidContext(mContext), Manager.DEFAULT_OPTIONS);
        database = manager.getDatabase(DB_NAME);

        startSync();
    }

    public void create(int x, int y, Point screenSize, String className) throws CouchbaseLiteException {
        Document document = database.getDocument(generateId(BuildConfig.VERSION_CODE, className, x, y));

        if (document.getCurrentRevision() == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(FIELD_X, x);
            properties.put(FIELD_Y, y);
            properties.put(FIELD_SCREEN_X, screenSize.x);
            properties.put(FIELD_SCREEN_Y, screenSize.y);
            properties.put(FIELD_ACTIVITY_NAME, className);
            properties.put(FIELD_APP_VERSION, BuildConfig.VERSION_CODE);
            properties.put(FIELD_HIT_COUNT, 1);

            document.putProperties(properties);
        } else {
            document.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision unsavedRevision) {
                    Map<String, Object> properties = unsavedRevision.getProperties();
                    properties.put(FIELD_HIT_COUNT, (int) properties.get(FIELD_HIT_COUNT) + 1);
                    return true;
                }
            });
        }
    }

    private String generateId(int appVersion, String activityName, int x, int y) {
        return String.format("%s::%d::%d::%d::%s",
                activityName,
                appVersion,
                x,
                y,
                UniqueId.getUniqueID(mContext));
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
