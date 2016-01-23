package us.v4lk.transrock.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.osmdroid.views.MapView;

import java.util.Collection;

import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.mapping.MapManager;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.SmartViewPager;
import us.v4lk.transrock.util.TransrockRoute;

/**
 * Map fragment.
 * Handles user interactions with the map, displaying routes & stops & vehicles,
 * monitors location & updates map accordingly.
 */
public class MapFragment extends Fragment implements LocationListener, ViewPager.OnPageChangeListener {

    /**
     * location manager
     */
    LocationManager locationManager;
    /**
     * map manager
     */
    MapManager mapManager;
    /**
     * root view
     */
    View root;

    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_map, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initialize manager
        MapView map = (MapView) root.findViewById(R.id.map);
        mapManager = new MapManager(map, getActivity(), root);

        // get a reference to the location manager
        locationManager = LocationManager.getInstance(this.getActivity().getApplicationContext());

        // get location updates
        locationManager.addLocationListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // do a proactive call to try to get location directly on resume
        Location loc = locationManager.getLocation();
        if (loc != null) mapManager.updateLocation(loc);

        // set the routes the map should draw
        Collection<TransrockRoute> activated = RouteStorage.getActivatedRoutes();
        mapManager.setRoutes(activated);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu_center_location:
                mapManager.setFollowMe(true);
                Location loc = locationManager.getLocation();
                if (loc != null) mapManager.updateLocation(loc);
                break;
            case R.id.map_menu_testing_button:
                mapManager.updateVehicles();
                break;
        }

        return true;
    }

    /* pager callbacks */
    @Override
    public void onPageSelected(int position) {

        switch (position) {
            case SmartViewPager.MAP_PAGE:
                mapManager.setRoutesFromStorage();
            default:
                break;
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /* play services location api callback */
    @Override
    public void onLocationChanged(Location location) {
        mapManager.updateLocation(location);
    }


}
