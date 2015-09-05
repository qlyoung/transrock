package us.v4lk.transrock.fragments;

import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import us.v4lk.transrock.MapWrap;
import us.v4lk.transrock.R;
import us.v4lk.transrock.util.LocationManager;

/**
 * Map fragment. Draws routes and lets the user move around the map.
 */
public class MapFragment extends Fragment {

    /** location manager */
    LocationManager locationManager;
    /** map wrapper */
    MapWrap mapWrap;
    /** root view */
    View root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
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
        mapWrap.setLocationMarkerOn(false);

        // initialize location manager
        locationManager = new LocationManager(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        Location l = locationManager.getLocation();
        mapWrap.centerAndZoomOnLocation(l, false);
        mapWrap.setLocationMarkerPosition(l);
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
                mapWrap.centerAndZoomOnLocation(locationManager.getLocation(), true);
                break;
        }

        return true;
    }

}
