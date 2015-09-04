package us.v4lk.transrock.fragments;

import android.app.Fragment;
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

import us.v4lk.transrock.R;
import us.v4lk.transrock.util.LocationManager;

/**
 * Map fragment. Draws routes and lets the user move around the map.
 */
public class MapFragment extends Fragment {

    /**
     * map zoom level
     */
    final int MAP_ZOOM_LEVEL = 20;

    LocationManager locationManager;
    MapView map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // capture & setup map
        map = (MapView) root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setMultiTouchControls(true);

        // return whatever should be the root of this fragment's view hierarchy
        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initialize location manager
        locationManager = new LocationManager(getActivity());
        // center map on current location without animating
        centerAndZoomOnLocation(false);
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
                centerAndZoomOnLocation(true);
                break;
        }

        return true;
    }

    /**
     * Centers the map on the current location
     * @param animate whether the map should animate or jump directly
     * @return false if the location could not be accessed
     */
    public boolean centerAndZoomOnLocation(boolean animate) {
        Location l = locationManager.getLocation();

        if (l != null) {
            GeoPoint center = new GeoPoint(l.getLatitude(), l.getLongitude());
            map.getController().setZoom(MAP_ZOOM_LEVEL);
            if (animate)
                map.getController().animateTo(center);
            else
                map.getController().setCenter(center);

            return true;
        } else return false;
    }
}
