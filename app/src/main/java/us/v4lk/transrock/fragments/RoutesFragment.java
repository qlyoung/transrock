package us.v4lk.transrock.fragments;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import us.v4lk.transrock.MainActivity;
import us.v4lk.transrock.SelectRoutesActivity;
import us.v4lk.transrock.R;
import us.v4lk.transrock.adapters.TransrockRouteAdapter;
import us.v4lk.transrock.transloc.objects.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.transloc.objects.Stop;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.SmartViewPager;
import us.v4lk.transrock.util.TransrockRoute;

/**
 * Route list fragment.
 */
public class RoutesFragment extends Fragment implements ViewPager.OnPageChangeListener {

    @Bind(R.id.routelist)
    ListView routeList;
    @Bind(R.id.routelist_addroute_fab)
    FloatingActionButton fab;
    @Bind(R.id.routelist_noroutes_message)
    View noRoutesMessage;

    @OnClick(R.id.routelist_addroute_fab)
    void onFabClick() {
        // start add routes activity
        Intent intent = new Intent(getActivity(), SelectRoutesActivity.class);
        startActivityForResult(intent, SELECT_ROUTES_REQUESTCODE);
    }


    /**
     * request code for route selection activity
     */
    public static final int SELECT_ROUTES_REQUESTCODE = 0;

    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_routelist, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set empty adapter; will be filled later in onResume()
        routeList.setAdapter(new TransrockRouteAdapter(getActivity(), R.layout.route_list_item));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRoutelist();
    }

    @Override
    public void onPause() {
        super.onPause();
        persistRoutelist();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_routelist, menu);
    }

    /* pager callbacks */
    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case SmartViewPager.MAP_PAGE:
                persistRoutelist();
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { /* don't care */ }

    @Override
    public void onPageScrollStateChanged(int state) { /* don't care */ }

    /**
     * Loads in routes to routelist from persistence
     */
    private void updateRoutelist() {
        // get routes from db
        Collection<TransrockRoute> routes = RouteStorage.getMap().values();

        // clear adapter
        TransrockRouteAdapter adapter = (TransrockRouteAdapter) routeList.getAdapter();
        adapter.clear();

        // populate list with stored routes, or hide list if there are none
        if (routes.size() != 0) {
            adapter.addAll(routes);
            routeList.setVisibility(View.VISIBLE);
            noRoutesMessage.setVisibility(View.GONE);
        } else {
            routeList.setVisibility(View.GONE);
            noRoutesMessage.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Writes modified routes to persistence.
     * Each time a user turns a route on or off, the corresponding field
     * is set in the backing route object. These changes need to be saved to disk.
     * Instead of writing to disk each time a switch is flipped, it's more to batch persist
     * them. This method writes out the memory model to disk.
     * Typically called when the user navigates away from this fragment.
     */
    public void persistRoutelist() {
        TransrockRoute[] all = ((TransrockRouteAdapter) routeList.getAdapter()).getAll();
        Set<TransrockRoute> modifiedRoutes = new HashSet<>(all.length);

        for (TransrockRoute route : all)
            modifiedRoutes.add(route);

        RouteStorage.putRoute(modifiedRoutes);
    }

    /* called when we return from SelectRoutesActivity */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_ROUTES_REQUESTCODE:
                // pull and store any new routes
                HashMap<String, Route> routelist = (HashMap<String, Route>) data.getSerializableExtra(SelectRoutesActivity.RESULT_EXTRA_KEY);
                FetchRoutesTask fetchNewRoutes = new FetchRoutesTask();
                fetchNewRoutes.execute(routelist.values().toArray(new Route[0]));
        }
    }

    /* tasks */

    /**
     * Takes a list of routes, gets necessary data to build TransrockRoutes, sets
     * internal storage to the resulting list (not additive), and then updates
     * this routelist.
     * This is used after getting a list of raw Route API objects from the route selector.
     */
    class FetchRoutesTask extends AsyncTask<Route, Integer, Collection<TransrockRoute>> {

        Snackbar snackbar;
        Route[] routes;

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
        protected Collection<TransrockRoute> doInBackground(Route... params) {

            ArrayList<TransrockRoute> result = new ArrayList<>();
            routes = params;

            // pull segments from network, create a TransrockRoute, and put
            // it in internal storage
            for (Route route : params) {
                try {
                    // get segments for this route
                    Map<String, String> segments = TransLocAPI.getSegments(route);

                    // get stops for this route's agency
                    Map<String, Stop> stops = TransLocAPI.getStops(route.agency_id);

                    // get stops for just this route
                    ArrayList<Stop> routeStops = new ArrayList<>();
                    for (Stop stop : stops.values())
                        for (String route_id : stop.routes)
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
            Map<String, TransrockRoute> storageRoutes = RouteStorage.getMap();
            for (TransrockRoute route : result)
                if (storageRoutes.containsKey(route.route_id))
                    route.setActivated(storageRoutes.get(route.route_id).isActivated());

            // remove all previously stored routes
            RouteStorage.clear();
            // put all new routes
            RouteStorage.putRoute(result);
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
