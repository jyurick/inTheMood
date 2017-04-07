package com.example.austin.inthemood;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

/**
 * A Controller class that attempts to allow for simple handling of getting locations.
 * Usage:
 *          Check for FINE_LOCATION_PERMISSION
 *          Request FINE_LOCATION_PERMISSION
 *
 *          if the request is successful the controller will will check and request location settings
 */
public class LocationController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {


    protected static final String TAG = LocationController.class.getSimpleName();
    public static final int REQUEST_CHECK_SETTINGS = 2;
    public static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSION = 3;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected GoogleApiClient mGoogleApiClient;
    protected Activity activity;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private boolean canGetLocation;

    /**
     * Instantiates a new Location controller.
     *
     * @param mGoogleApiClient the m google api client
     * @param activity         the activity
     */
    public LocationController(GoogleApiClient mGoogleApiClient, Activity activity) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.activity = activity;

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    /**
     * Is can get location boolean.
     *
     * @return the boolean
     */
    public boolean isCanGetLocation() {
        return canGetLocation;
    }

    /**
     * Sets can get location.
     *
     * @param canGetLocation the can get location
     */
    public void setCanGetLocation(boolean canGetLocation) {
        this.canGetLocation = canGetLocation;
    }

    /**
     * Gets google api client.
     *
     * @return the google api client
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Gets current location.
     * this might still be null becuase it doesn't seem to work right.
     * @return the current location
     */
    public Location getCurrentLocation() {
        if (mCurrentLocation == null && checkLocationPermission()) {
//            Toast.makeText(activity, LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).toString(), Toast.LENGTH_SHORT).show();
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
//        Toast.makeText(activity, mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
        return mCurrentLocation;
    }

    /**
     * Build google api client.
     */
    protected void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Create location request.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Build location settings request.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check location permission boolean.
     *
     * @return the boolean
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


    /**
     * Request location permission.
     */
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION_PERMISSION);
    }

    /**
     * ask the user if they want to change the location settings
     * based on http://blog.teamtreehouse.com/beginners-guide-location-android
     * accessed on March 27, 2017
     */
    public void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient, mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }


    /**
     * On request permissions result.
     * check the result from requesting the location permission
     * from https://stackoverflow.com/questions/33865445/gps-location-provider-requires-access-fine-location-permission-for-android-6-0
     * accessed on March 27, 2017
     *
     * @param requestCode  the request code
     * @param permissions  the permissions
     * @param grantResults the grant results
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // check location settings
                    checkLocationSettings();
                } else {
                    canGetLocation = false;
                }
                return;
            }
        }
    }

    /**
     * Connect the GoogleAPI. This should be called in an activities onStart() method.
     */
    public void connectGoogleApiClient() {
        mGoogleApiClient.connect();
    }

    /**
     * Disconnect the GoogleAPI. This should be called in an activities onStop() method.
     */
    public void disconnectGoogleApiClient() {
        mGoogleApiClient.disconnect();
    }

    /**
     * Check if the Google API is connected.
     * @return true if Google API is connected.
     */
    public boolean googleApiClientConnected() {
        return mGoogleApiClient.isConnected();
    }

    /**
     * Start location updates.
     */
    public void startLocationUpdates() {
        if (checkLocationPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mRequestingLocationUpdates = true;
                }
            });
        }
    }

    /**
     * Stop requesting location updates.
     */
    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    /**
     * Convert a location object to a LatLng for Google Maps
     * @param location
     * @return a LatLng object corresponding to the coordinates of the passed location.
     */
    public static LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


    /**
     * Called from a PendingResult object. In this case its used to check location settings.
     * @param locationSettingsResult
     */
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                // TODO add check to make sure location is high accuracy and not just on
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // might be null if there is no last location so poll for a new one.
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }
}
