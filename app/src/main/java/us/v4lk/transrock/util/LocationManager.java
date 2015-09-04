package us.v4lk.transrock.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Wrapper for Play Services Location API. Mostly exists so I don't have
 * to clutter my activities with API callbacks.
 * This class will save the last location it was able to get from Google
 * and return that if it can't get a better location immediately.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient apiclient;
    private static Location latest;

    public LocationManager(Context context) {
        apiclient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

        apiclient.connect();
    }

    /* google api callbacks */
    @Override
    public void onConnectionSuspended(int i) {
        //TODO: i don't even know what goes here
    }
    /**
     * Called when the API client establishes a connection and can receive requests.
     * Here, the latest known location is immediately fetched and this class's static
     * cache is updated with it.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        latest = LocationServices.FusedLocationApi.getLastLocation(apiclient);
    }
    /**
     * Called when the API client disconnects.
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * Asks Google for latest location. Will return cached location if Google
     * says our location is null.
     * @return the latest location, or the cached location if we can't reach Google.
     */
    public Location getLocation() {
        Location l = LocationServices.FusedLocationApi.getLastLocation(apiclient);
        if (l != null)
            latest = l;

        return latest;
    }
    /**
     * @return whether or not the API client is currently connected
     */
    public boolean isConnected() {
        return apiclient.isConnected();
    }
}
