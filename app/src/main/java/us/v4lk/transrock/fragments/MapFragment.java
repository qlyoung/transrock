package us.v4lk.transrock.fragments;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import butterknife.BindDrawable;
import butterknife.ButterKnife;
import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.mapping.MapWrap;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.transloc.objects.Vehicle;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.SmartViewPager;
import us.v4lk.transrock.util.TransrockRoute;

/**
 * Map fragment. Draws routes and lets the user move around the map.
 */
public class MapFragment extends Fragment implements LocationListener, ViewPager.OnPageChangeListener {

    @BindDrawable(R.drawable.location_marker) Drawable location_marker;
    @BindDrawable(R.drawable.stop_marker) Drawable stop_marker;

    /** location manager */
    LocationManager locationManager;
    /** map wrapper */
    MapWrap mapWrap;
    /** root view */
    View root;
    /** whether to center the map on the user's location each time we get a location update */
    boolean followMe = true;

    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, root);
        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // capture & setup map
        mapWrap = new MapWrap(getActivity(), (MapView) getView().findViewById(R.id.map));
        mapWrap.setLocationMarkerDrawable(location_marker);
        mapWrap.setDefaultMarkerDrawable(stop_marker);
        mapWrap.setScaleBar(true);

        // add follow-me watchdog overlay
        TouchOverlay touchOverlay = new TouchOverlay(mapWrap.getContext());
        touchOverlay.setEnabled(true);
        mapWrap.getMapView().getOverlays().add(touchOverlay);

        // get a reference to the location manager
        locationManager = LocationManager.getInstance(this.getActivity().getApplicationContext());

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

        // update routes
        mapWrap.setRoutes( RouteStorage.getActivatedRoutes() );
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
            // if location button selected, animate & zoom to current location
            case R.id.map_menu_center_location:
                // enable follow-me user on location updates && simulate location update
                followMe = true;
                mapWrap.centerAndZoomOnPosition(locationManager.getLocation(), true);
                break;
            // if test button selected do test stuff
            case R.id.map_menu_testing_button:
                new UpdateVehicles().execute();
                break;
        }

        return true;
    }

    /* pager callbacks */
    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case SmartViewPager.MAP_PAGE:
                mapWrap.setRoutes( RouteStorage.getActivatedRoutes() );
                break;
            case SmartViewPager.ROUTE_PAGE:
            default:
                break;
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override
    public void onPageScrollStateChanged(int state) { }

    /* play services location api callback */
    @Override
    public void onLocationChanged(Location location) {
        mapWrap.setLocationMarkerPosition(location);
        if (followMe)
            mapWrap.centerOnPosition(location, true);
    }

    /* overlays */
    /**
     * Hacky workaround for all of MapView's touch callbacks being hosed.
     * This overlay covers the whole map and breaks follow-me when touched.
     */
    class TouchOverlay extends Overlay {
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
    /**
     * Fetches vehicles and updates map
     */
    class UpdateVehicles extends AsyncTask<Void, Void, Collection<Vehicle>> {

        Collection<TransrockRoute> routes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            routes = RouteStorage.getActivatedRoutes();
        }

        @Override
        protected Collection<Vehicle> doInBackground(Void... params) {
            // fetch vehicles
            ArrayList<Vehicle> vehicles = new ArrayList<>();
            try {
                for (TransrockRoute route : routes)
                    vehicles.addAll(TransLocAPI.getVehicles(route.agency_id, route.route_id));
            }
            catch (NetworkErrorException e) {

            }
            catch (SocketTimeoutException e) {

            }
            catch (JSONException e) {

            }

            return vehicles;
        }

        @Override
        protected void onPostExecute(Collection<Vehicle> vehicles) {
            super.onPostExecute(vehicles);
            mapWrap.setVehicles(vehicles);
            mapWrap.invalidate();
        }
    }

}
