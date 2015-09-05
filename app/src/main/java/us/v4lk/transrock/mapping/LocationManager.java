package us.v4lk.transrock.mapping;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Wrapper for Play Services Location API. Mostly exists so I don't have
 * to clutter my activities with API callbacks.
 * This class will save the last location it was able to get from Google
 * and return that if it can't get a better location immediately.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener,
                                        LocationListener {

    private GoogleApiClient apiclient;
    private static Location latest;
    private LocationRequest locationRequest;

    public LocationManager(Context context) {
        apiclient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000)
                        .setFastestInterval(1000)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        apiclient.connect();
    }

    /* google api callbacks */
    @Override
    public void onConnectionSuspended(int i) {
        //TODO: i don't even know what goes here
    }
    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiclient, locationRequest, this);
        latest = LocationServices.FusedLocationApi.getLastLocation(apiclient);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiclient, this);
    }
    @Override
    public void onLocationChanged(Location location) {
        latest = location;
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

}
