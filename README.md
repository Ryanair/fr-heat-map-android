# FRHeatMap

FRHeatMap library is an Android tool that helps the application owners determine which areas of the application are most used and which are totally ignored.

The library catches every touch on the screen inside the application, records it and synchronizes it to the server. The technology used for storing the data locally is *Couchbase Lite* and for sending *Couchbase SyncGateway*.

## Usage

To use this library you have to:

1. Clone the project

    ```
    git clone https://github.com/Ryanair/fr-heat-map-android.git
    ```

2. Build the library:

    ```
    <library_home>: gradlew build
    ```

3. Include the library in the project by adding file **<library_home>/frheatmap2/build/outputs/aar/frheatmap2-release.aar** to your projects lib folder.
4. When creating new Activity for which you want to monitor user's touches, simply extend our FRHeatmapActivity instead of the native Android Activity.

That's it. The data will be gathered and sent to the server automatically.

### Alternative usage

If you're unable to extend the provided FRHeatmapActivity (for example if you're already extending some other Activity subclass, like ActionBarActivity etc.) you can still add the Heatmap functionality manually.

To do so you must add the following code to your Activity subclass:

```Java
private FRHeatmapHandler mHeatmapHandler;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mHeatmapHandler = new FRHeatmapHandler(this);
    // your code
}

@Override
protected void onDestroy() {
    // your code
    mHeatmapHandler.close();
    super.onDestroy();
}

@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
    mHeatmapHandler.dispatchTouchEvent(ev);
    // your code
    return super.dispatchTouchEvent(ev);
}
```