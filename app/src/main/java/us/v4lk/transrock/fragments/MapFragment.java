package us.v4lk.transrock.fragments;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.views.MapView;

import us.v4lk.transrock.mapping.MapWrap;
import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;

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
        //mapWrap.setLocationMarkerDrawable(getResources().getDrawable(R.drawable.location_marker));

        // initialize location manager
        locationManager = new LocationManager(getActivity());
    }
    @Override
    public void onResume() {
        super.onResume();

        Location l = locationManager.getLocation();
        mapWrap.centerAndZoomOnPosition(l, false);
        mapWrap.setLocationMarkerPosition(l);
        mapWrap.setLocationMarkerOn(true);
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
                mapWrap.centerAndZoomOnPosition(locationManager.getLocation(), true);
                mapWrap.setLocationMarkerPosition(locationManager.getLocation());
                break;
        }

        return true;
    }

}
