package us.v4lk.transrock.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import us.v4lk.transrock.mapping.MapWrap;
import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;

/**
 * Map fragment. Draws routes and lets the user move around the map.
 */
public class MapFragment extends Fragment implements LocationListener {

    /** location manager */
    LocationManager locationManager;
    /** map wrapper */
    MapWrap mapWrap;
    /** root view */
    View root;
    /** whether to center the map on the user's location */
    boolean followMe = true;

    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        root = inflater.inflate(R.layout.fragment_map, container, false);

        // return whatever should be the root of this fragment's view hierarchy
        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // capture & setup map
        mapWrap = new MapWrap(getActivity(), (MapView) root.findViewById(R.id.map));
        mapWrap.setScaleBar(true);
        mapWrap.setLocationMarkerDrawable(getResources().getDrawable(R.drawable.location_marker));
        TouchOverlay touchOverlay = new TouchOverlay(mapWrap.getContext());
        touchOverlay.setEnabled(true);
        mapWrap.getMapView().getOverlays().add(touchOverlay);

        // initialize location manager
        locationManager = new LocationManager(getActivity());

        // get location updates
        locationManager.addLocationListener(this);
    }
    @Override
    public void onResume() {
        super.onResume();

        // do a proactive call to try to get location directly on resume
        Location l = locationManager.getLocation();
        if (l != null) {
            mapWrap.centerAndZoomOnPosition(l, false);
            mapWrap.setLocationMarkerPosition(l);
            mapWrap.setLocationMarkerOn(true);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // if location button selected, animate & zoom to current location
            case R.id.map_menu_center_location:
                // enable follow-me user on location updates && simulate location update
                followMe = true;
                onLocationChanged(locationManager.getLocation());
                break;
        }

        return true;
    }

    /* play services location api callback */
    @Override
    public void onLocationChanged(Location location) {
        mapWrap.setLocationMarkerPosition(location);
        if (followMe)
            mapWrap.centerOnPosition(location, true);
    }

    /**
     * Hacky workaround for all of MapView's touch callbacks
     * being hosed. Overlay whose sole purpose is to do something
     * when it gets touched.
     */
    private class TouchOverlay extends Overlay {
        Context context;

        public TouchOverlay(Context c) {
            super(c);
            context = c;
        }

        @Override
        protected void draw(Canvas c, MapView osmv, boolean shadow) { }
        @Override
        public boolean onDown(MotionEvent e, MapView mapView) {
            // disable follow-me; user goes to manual mode
            followMe = false;
            return super.onDown(e, mapView);
        }
    }
}
