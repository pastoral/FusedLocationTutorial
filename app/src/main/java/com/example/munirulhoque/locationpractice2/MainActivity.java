package com.example.munirulhoque.locationpractice2;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<LocationSettingsResult> {
    private GoogleApiClient googleApiClient;
    String permisionList[] = {"android.Manifest.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private static final int permsRequestCode = 200;
    private static final String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected Location mCurrentlocation;
    private View view;
    TextView latitude, longitude, distanceValue;
    Button btnLocationUpdate;
    public LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    public boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    protected final static double FIXED_LAT = 23.751047;
    protected final static double FIXED_LANG = 90.390745;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = (TextView) findViewById(R.id.text_lat);
        longitude = (TextView) findViewById(R.id.text_lang);
        btnLocationUpdate = (Button) findViewById(R.id.btn_loc_update);
        distanceValue = (TextView)findViewById(R.id.text_distance) ;

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        isGooglePlayServicesAvailable(MainActivity.this);
        createLocationRequest();
        buildLocationSettingsRequest();




    }

    @Override
    protected void onStart() {

        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        // googleApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        /*if(googleApiClient.isConnected() && mRequestingLocationUpdates){
            startLocationUpdates();
           // checkLocationSettings();
        }*/
        LocationAsyncRunner runner = new LocationAsyncRunner();
        runner.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        stopLocationUpdates();

    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    protected synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected LocationRequest createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mCurrentlocation==null){
                mCurrentlocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());
                //updateLocationUI();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
       // if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    mRequestingLocationUpdates = true;
                    setButtonsEnabledState();
                }
            });
       // }
       // else{
           // Toast.makeText(this, "Start Location update gets hampered :(( " , Toast.LENGTH_SHORT).show();
       // }
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        mRequestingLocationUpdates = false;
                    }
                }
        );
    }



    /**
     * Callback that fires when the location changes.
     * Before requesting location updates, your app must connect to location services and make a location request.
     *  Once a location request is in place you can start the
     * regular updates by calling requestLocationUpdates(). Do this in the onConnected() callback provided by Google API Client,
     * which is called when the client is ready.
     */

    @Override
    public void onLocationChanged(Location location) {
        mCurrentlocation = location;
        mLastUpdateTime = java.text.DateFormat.getDateTimeInstance().format(new Date());
        updateLocationUI();
        Toast.makeText(this, "Last updated on " + mLastUpdateTime, Toast.LENGTH_SHORT).show();
    }


    private void setButtonsEnabledState()
    {
        if(mRequestingLocationUpdates){
            btnLocationUpdate.setEnabled(true);
        }
        else{
            btnLocationUpdate.setEnabled(true);
        }
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings(){
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch(status.getStatusCode()){
            case LocationSettingsStatusCodes.SUCCESS :
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try{
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                }
                catch(IntentSender.SendIntentException e){
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE :
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_CHECK_SETTINGS :
                switch (resultCode){
                    case Activity.RESULT_OK :
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED :
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    private void updateLocationUI(){
        if (mCurrentlocation != null) {
            // Toast.makeText(this, "Latitude : " + String.valueOf(location.getLatitude()) + "Longitude: " + String.valueOf(location.getLongitude()), Toast.LENGTH_SHORT).show();
            latitude.setText("Latitude : " + String.valueOf(mCurrentlocation.getLatitude()));
            longitude.setText("Longitude: " + String.valueOf(mCurrentlocation.getLongitude()));
            distanceValue.setText(String.valueOf(calculteDistance(mCurrentlocation.getLatitude(),mCurrentlocation.getLongitude())));
        } else {
            Toast.makeText(this, "Mara Kha ", Toast.LENGTH_SHORT).show();
            //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,);
        }
    }


    public void locUpdate(View view){

        //checkLocationSettings();
        LocationAsyncRunner runner = new LocationAsyncRunner();
        runner.execute();
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},permsRequestCode);
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},permsRequestCode);
            }
        }
        else{
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES,mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION,mCurrentlocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING,mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     //* @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstance){
        if(savedInstance != null){
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if(savedInstance.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)){
                // Update the value of mCurrentLocation from the Bundle and update the UI to show the
                // correct latitude and longitude.
                mRequestingLocationUpdates = savedInstance.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }

            if(savedInstance.keySet().contains(KEY_LOCATION)){
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentlocation = savedInstance.getParcelable(KEY_LOCATION);
            }

            if (savedInstance.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                // Update the value of mLastUpdateTime from the Bundle and update the UI.
                mLastUpdateTime = savedInstance.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateLocationUI();
        }
    }
    protected double calculteDistance(double userLat, double userLang){
        double distance = 0;
        Location nearestCustomerCare = new Location("nearest Customer care");
        nearestCustomerCare.setLatitude(FIXED_LAT);
        nearestCustomerCare.setLongitude(FIXED_LANG);
        distance = nearestCustomerCare.distanceTo(mCurrentlocation);
        return distance;
    }

    public class LocationAsyncRunner extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this, "Patience", "Searching your location");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            checkLocationSettings();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            updateLocationUI();
        }
    }

}
