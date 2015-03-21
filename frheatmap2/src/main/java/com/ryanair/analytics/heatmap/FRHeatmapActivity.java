package com.ryanair.analytics.heatmap;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

public class FRHeatmapActivity extends Activity {
    private FRHeatmapHandler mHeatmapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHeatmapHandler = new FRHeatmapHandler(this);
    }

    @Override
    protected void onDestroy() {
        mHeatmapHandler.close();
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mHeatmapHandler.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
