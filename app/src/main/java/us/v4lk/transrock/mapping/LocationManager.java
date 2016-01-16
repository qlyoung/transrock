package us.v4lk.transrock.mapping;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Wrapper for Play Services Location API. Mostly exists so I don't have
 * to clutter my activities with API callbacks.
 * This object will update the current location every 5 seconds. Updates
 * may be subscribed to with addLocationListener(), or accessed via polling
 * by calling getLocation().
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener,
                                        LocationListener {

    /** play services api client */
    private GoogleApiClient apiclient;
    /** lastKnown location received */
    private Location lastKnown;
    /** defines settings for location updates */
    private LocationRequest locationRequest;
    /** list of listeners subscribed for location updates */
    private ArrayList<LocationListener> locationListeners;

    private static LocationManager instance;

    /**
     * Initializes a new LocationManager and initiates connection to
     * Play Services API.
     * @param appContext the application context
     */
    private LocationManager(Context appContext) {
        // setup api client
        apiclient = new GoogleApiClient.Builder(appContext)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

        // define some settings for location updates
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000)
                       .setFastestInterval(1000)
                       .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // initialize list of listeners, and add self
        locationListeners = new ArrayList<>();
        addLocationListener(this);

        apiclient.connect();
    }

    /**
     * Gets the LocationManager instance.
     * @param appContext application context
     * @return instance of LocationManager
     */
    public static LocationManager getInstance(Context appContext) {
        if (instance == null)
            instance = new LocationManager(appContext);

        return instance;
    }

    /* listener stuff */
    /**
     * Adds a listener that will receive callbacks on location updates
     * @param listener listener that should receive callbacks
     */
    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
        // if onConnected() has already been called, hook the listener up here
        if (apiclient.isConnected())
            LocationServices.FusedLocationApi.requestLocationUpdates(apiclient, locationRequest, listener);
    }
    /**
     * Removes a listener so that it no longer receives location updates
     * @param listener listener that should be removed
     */
    public void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }
    /**
     * @return the last known location
     */
    public Location getLocation() {
        return lastKnown;
    }

    /* google api callbacks */
    @Override
    public void onConnectionSuspended(int i) {
        //TODO: i don't even know what goes here
    }
    @Override
    public void onConnected(Bundle bundle) {
        // subscribe all listeners to receive location updates
        for (LocationListener listener : locationListeners)
            LocationServices.FusedLocationApi.requestLocationUpdates(apiclient, locationRequest, listener);
        // get an immediate update
        lastKnown = LocationServices.FusedLocationApi.getLastLocation(apiclient);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        for (LocationListener listener : locationListeners)
            LocationServices.FusedLocationApi.removeLocationUpdates(apiclient, listener);
    }
    @Override
    public void onLocationChanged(Location location) {
        lastKnown = location;
    }
}
