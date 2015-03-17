package com.ryanair.analytics.heatmap;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.couchbase.lite.CouchbaseLiteException;

import java.io.IOException;

public class FRHeatmap {
    private Storage mStorage;
    private WindowManager mWindowManager;
    private FrameLayout mFrameLayout;
    private Activity mActivity;
    private Point mScreenSize = new Point();

    public FRHeatmap(Activity activity) {
        mActivity = activity;

        mFrameLayout = new FrameLayout(mActivity);
        mFrameLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mFrameLayout.setOnTouchListener(onTouchListener);

        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mWindowManager.addView(mFrameLayout, params);

        Display display = mWindowManager.getDefaultDisplay();
        display.getSize(mScreenSize);

        try {
            mStorage = new Storage(mActivity.getBaseContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                mStorage.create(Math.round(event.getX()), Math.round(event.getY()), mScreenSize, mActivity.getLocalClassName());
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }

            return true;
        }
    };
}
