package com.ryanair.analytics.heatmap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;

/**
 * Handles the touch event by storing it into the database.
 */
public class FRHeatmapHandler {
    private static final String TAG = FRHeatmapHandler.class.getSimpleName();
    private Point mScreenSize;
    private Storage mStorage;
    private String mLocalClassName;

    public FRHeatmapHandler(Activity activity) {
        mLocalClassName = activity.getLocalClassName();

        // prepare the database
        try {
            mStorage = new Storage(activity);
        } catch (IOException | CouchbaseLiteException e) {
            Log.e(TAG, "Error creating couchbase storage for storing heatmap.", e);
        }

        // get the size of the screen for tagging the data
        mScreenSize = new Point();
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(mScreenSize);
    }


    public void close() {
        mStorage.close();
    }

    public void dispatchTouchEvent(MotionEvent ev) {
        // if error occurred while creating the database
        if (mStorage == null) {
            return;
        }

        // don't record the UP event (when user lifts the finger from the screen)
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            return;
        }


        Log.d(TAG, "Touch event logged: " + ev);
        try {
            mStorage.create(Math.round(ev.getX()), Math.round(ev.getY()), mScreenSize, mLocalClassName);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Could not save the data to the database", e);
        }
    }
}
