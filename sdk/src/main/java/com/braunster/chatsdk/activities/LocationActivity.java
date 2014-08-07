package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

/**
 * Created by braunster on 19/06/14.
 */
public class LocationActivity extends FragmentActivity
                        implements GooglePlayServicesClient.ConnectionCallbacks,
                                    GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = LocationActivity.class.getSimpleName();
    public static final boolean DEBUG = true;

    public static final String ERROR = "Error";
    public static final String ERROR_SNAPSHOT = "error getting snapshot";
    public static final String ERROR_SAVING_IMAGE = "error saving image";

    // TODO listen to marker and add option to send marker position.
    // TODO show close locations: http://stackoverflow.com/questions/13488048/google-maps-show-close-places-to-current-user-poisition

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 120;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;

    private GoogleMap map;
    private LocationClient locationClient;
    private Button btnSendLocation;
    private LatLng requestedLocation;
    private Marker selectedLocation;

    public static final String SHOW_LOCATION = "show_location";
    public static final String LANITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SNAP_SHOT_PATH = "snap_shot_path";
    public static final String ZOOM = "zoom";
    public static final String BASE_64_FILE = "base_64_file";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_locaction);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(SHOW_LOCATION))
        {
            requestedLocation = new LatLng(getIntent().getExtras().getLong(LANITUDE), getIntent().getExtras().getLong(LONGITUDE));
        }
        else
        {
            // Show current location.
            makeLocationRequest();
        }

        locationClient = new LocationClient(this, this, this);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        if (map == null)
        {
            reportError("Map is null");
            return;
        }

        // Add find my location button
        map.setMyLocationEnabled(true);

        btnSendLocation = (Button) findViewById(R.id.chat_sdk_btn_send_location);
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (map == null)
            return;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (selectedLocation != null)
                {
                    selectedLocation.remove();
                    selectedLocation = null;
                }

                // TODO show the adress of that position http://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
                selectedLocation = map.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            }
        });

        btnSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeSnapShot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        Bitmap bitmapLocation = snapshot;
                        try {
                            File savedFile = Utils.LocationImageHandler.saveLocationImage(LocationActivity.this, bitmapLocation, null);
                            if ( savedFile == null)
                                reportError(ERROR_SAVING_IMAGE);
                            else
                                reportSuccess(savedFile);

                        } catch (Exception e) {
                            e.printStackTrace();
                            reportError(ERROR_SNAPSHOT);
                        }
                    }
                });
            }
        });
    }

    private void reportError(String error){
        if (DEBUG) Log.e(TAG, "Failed");
        Intent intent = new Intent();
        intent.putExtra(ERROR, error);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void reportSuccess(File file){
        // Reporting to the caller activity of the location picked and the snapshot image taken file location.
        Intent intent = new Intent();

        // If no marker has been added send the user current location.
        if (selectedLocation == null) {
            intent.putExtra(LANITUDE, map.getMyLocation().getLatitude());
            intent.putExtra(LONGITUDE, map.getMyLocation().getLongitude());
        }
        else
        {
            intent.putExtra(LANITUDE, selectedLocation.getPosition().latitude);
            intent.putExtra(LONGITUDE, selectedLocation.getPosition().longitude);
        }

        intent.putExtra(SNAP_SHOT_PATH, file.getPath());
        intent.putExtra(ZOOM, map.getCameraPosition().zoom);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onStop() {
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

//        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        //Register to location change if not null.
        if (mLocationRequest != null)
        {
            locationClient.requestLocationUpdates(mLocationRequest, LocationActivity.this, getMainLooper());
            setLocation(locationClient.getLastLocation());
        }
        else
        {
            // Show requested location.
            setLocation(requestedLocation);
        }
    }

    private void setLocation(Location location){
        setLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void setLocation(LatLng location){
        map.moveCamera(CameraUpdateFactory.newLatLng(location));

        map.animateCamera(CameraUpdateFactory.zoomBy(calculateZoomLevel(findViewById(R.id.map).getWidth())));
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                *//*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 *//*
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            *//*
             * If no resolution is available, display a dialog to the
             * user with the error.
             *//*
            showErrorDialog(connectionResult.getErrorCode());
        }*/
    }

    @Override
    public void onLocationChanged(Location location) {
//        Toast.makeText(LocationActivity.this, "Location Changed", Toast.LENGTH_SHORT).show();
        setLocation(location);
    }

    public void takeSnapShot(GoogleMap.SnapshotReadyCallback callback){
        map.snapshot(callback);
    }

    private void makeLocationRequest(){
        // Register to current location changes.
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private int calculateZoomLevel(int screenWidth) {
        double equatorLength = 40075004; // in meters
        double widthInPixels = screenWidth;
        double metersPerPixel = equatorLength / 256;
        int zoomLevel = 1;
        while ((metersPerPixel * widthInPixels) > 20000) {
            metersPerPixel /= 2;
            ++zoomLevel;
        }
        if (DEBUG) Log.i(TAG, "zoom level = " + zoomLevel);
        return zoomLevel;
    }

}
