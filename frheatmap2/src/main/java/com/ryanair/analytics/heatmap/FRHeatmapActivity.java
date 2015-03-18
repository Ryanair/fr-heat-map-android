package com.ryanair.analytics.heatmap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;

public class FRHeatmapActivity extends Activity {
    private Storage mStorage;
    private Point mScreenSize = new Point();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(mScreenSize);

        try {
            mStorage = new Storage(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        mStorage.close();

        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            mStorage.create(Math.round(ev.getX()), Math.round(ev.getY()), mScreenSize, this.getLocalClassName());
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        return super.dispatchTouchEvent(ev);
    }
}
