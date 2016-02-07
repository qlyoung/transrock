package us.v4lk.transrock.fragments;

import android.accounts.NetworkErrorException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.osmdroid.views.MapView;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.mapping.MapManager;
import us.v4lk.transrock.model.RouteModel;
import us.v4lk.transrock.model.SegmentModel;
import us.v4lk.transrock.model.StopModel;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.RouteManager;
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
    /** Handles messages & tasks on this thread's queue. */
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
        locationManager = LocationManager.getInstance(this.getActivity().getApplicationContext());

        // get location updates
        locationManager.addLocationListener(this);
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

        // set the routes the map should draw
        mapManager.setRoutes(RouteManager.getActivatedRoutes());

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
                mapManager.setRoutes(RouteManager.getActivatedRoutes());
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

    /**
     * Takes a list of routes and updates the map with relevant markers for these routes. If relevant
     * data is in realm, that is used, otherwise data is fetched from the network and added to realm
     * for next time.
     */
    class FetchRoutesTask extends AsyncTask<RouteModel, Integer, Collection<TransrockRoute>> {

        Snackbar snackbar;
        RouteModel[] routes;

        @Override
        protected void onPreExecute() {
            // add currently stored routes to listview before pulling new ones
            // avoids empty routelist while waiting for this task to finish
            updateRoutelist();
            // show snackbar
            snackbar = Snackbar.make(
                    RoutesFragment.this.getView(),
                    R.string.updating_routes,
                    Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.dismiss, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
            super.onPreExecute();
        }

        @Override
        protected Collection<TransrockRoute> doInBackground(RouteModel... params) {

            ArrayList<TransrockRoute> result = new ArrayList<>();
            routes = params;

            // pull segments from network, create a TransrockRoute, and put
            // it in internal storage
            for (RouteModel route : params) {
                try {

                    route.getStops();
                    // get segments for this route
                    SegmentModel[] segments = TransLocAPI.getSegments(route);

                    // get stops for this route's agency
                    StopModel[] stops = TransLocAPI.getStops(route.getAgencyId());

                    // get stops for just this route
                    ArrayList<StopModel> routeStops = new ArrayList<>();
                    for (StopModel stop : stops)
                        for (String route_id : s)
                            if (route_id.equals(route.route_id))
                                routeStops.add(stop);

                    // build a new TransrockRoute
                    TransrockRoute trr = new TransrockRoute(route,
                            segments.values().toArray(new String[0]),
                            routeStops.toArray(new Stop[0]));

                    // set it to active
                    trr.setActivated(true);

                    // add it it to the resultant
                    result.add(trr);

                } catch (SocketTimeoutException e) {
                    publishProgress(R.string.error_network_timeout);
                    this.cancel(true);
                } catch (NetworkErrorException e) {
                    publishProgress(R.string.error_network_unknown);
                    this.cancel(true);
                } catch (JSONException e) {
                    publishProgress(R.string.error_bad_parse);
                    this.cancel(true);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Collection<TransrockRoute> result) {
            // for the new routes, keep the activated status if it was previously set
            Map<String, TransrockRoute> storageRoutes = RouteManager.getMap();
            for (TransrockRoute route : result)
                if (storageRoutes.containsKey(route.route_id))
                    route.setActivated(storageRoutes.get(route.route_id).isActivated());

            // remove all previously stored routes
            RouteManager.clear();
            // put all new routes
            RouteManager.putRoute(result);
            // update views
            updateRoutelist();
            // dismiss snackbar
            snackbar.dismiss();

            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // show snackbar with error message
            int messageResId = values[0];
            if (RoutesFragment.this.getView() != null) {
                Snackbar errorSnackbar = Snackbar.make(RoutesFragment.this.getView(), messageResId, Snackbar.LENGTH_LONG);

                // allow user to retry this task in case of error
                errorSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // re-run this task
                        FetchRoutesTask newTask = new FetchRoutesTask();
                        newTask.execute(routes);
                    }
                });
                errorSnackbar.show();
            }
        }
    }


}
