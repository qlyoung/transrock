package us.v4lk.transrock.util;

import android.location.Location;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

/**
 * Created by qly on 9/5/15.
 */
public class LocationProvider implements IMyLocationProvider {

    final LocationManager locationManager;

    public LocationProvider(LocationManager m) {
        locationManager = m;
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        return true;
    }
    @Override
    public Location getLastKnownLocation() {
        return locationManager.getLocation();
    }
    @Override
    public void stopLocationProvider() {

    }
}
