package us.v4lk.transrock.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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

import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.mapping.MapManager;
import us.v4lk.transrock.util.SmartViewPager;

/**
 * Map fragment.
 * Handles user interactions with the map, displaying routes & stops & vehicles,
 * monitors location & updates map accordingly.
 */
public class MapFragment extends Fragment implements LocationListener, ViewPager.OnPageChangeListener {

    LocationManager locationManager;
    MapManager mapManager;
    View root;

    /** Used to post tasks to be executed in the future.. */
    Handler handler;

    /** Time between vehicles updates, in milliseconds */
    int UPDATE_VEHICLES_INTERVAL = 3000;

    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        handler = new Handler();
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
        locationManager = LocationManager.getInstance(getActivity().getApplicationContext());

        // get location updates
        locationManager.addLocationListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // do a proactive call to try to get location directly on resume
        Location loc = locationManager.getLocation();
        if (loc != null) {
            mapManager.setMapPosition(loc);
            mapManager.updateLocation(loc);
        }

        // build and draw routes
        mapManager.buildAndDraw();

        // set a recurring task on the handler to update vehicles
        Runnable updateVehiclesRunnable = new Runnable() {
            @Override
            public void run() {
                mapManager.updateVehicles();
                handler.postDelayed(this, UPDATE_VEHICLES_INTERVAL);
            }
        };
        handler.post(updateVehiclesRunnable);
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
        }

        return true;
    }

    /* pager callbacks */
    @Override
    public void onPageSelected(int position) {

        switch (position) {
            case SmartViewPager.MAP_PAGE:
                mapManager.buildAndDraw();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }
}
