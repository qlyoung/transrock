package us.v4lk.transrock.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by qly on 7/25/15.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient apiclient;
    private boolean connected = false;

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
    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
        latest = LocationServices.FusedLocationApi.getLastLocation(apiclient);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connected = false;
    }

    /**
     * Asks Google for latest location. Will return cached location if Google
     * says our location is null.
     *
     * @return the latest location, or the cached location if we can't reach Google.
     */
    public Location getLocation() {
        Location l = LocationServices.FusedLocationApi.getLastLocation(apiclient);
        if (l != null)
            latest = l;

        return latest;
    }
    public boolean isConnected() {
        return connected;
    }
}
