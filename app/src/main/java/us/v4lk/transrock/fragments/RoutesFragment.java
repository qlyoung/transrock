package us.v4lk.transrock.fragments;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import us.v4lk.transrock.SelectRoutesActivity;
import us.v4lk.transrock.R;
import us.v4lk.transrock.adapters.TransrockRouteAdapter;
import us.v4lk.transrock.transloc.objects.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.TransrockRoute;

/** Route list fragment. */
public class RoutesFragment extends Fragment {

    /** ListView holding all route items */
    ListView routeList;
    /** request code for route selection activity */
    final int SELECT_ROUTES_REQUESTCODE = 0;

    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View root = inflater.inflate(R.layout.fragment_routelist, container, false);

        // capture list
        routeList = (ListView) root.findViewById(R.id.routelist);

        // capture & setup fab
        FloatingActionButton addRoutesFab = (FloatingActionButton) root.findViewById(R.id.routelist_addroute_fab);
        addRoutesFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start add routes activity
                Intent intent = new Intent(getActivity(), SelectRoutesActivity.class);
                startActivityForResult(intent, SELECT_ROUTES_REQUESTCODE);
            }
        });

        // return whatever should be the root of this fragment's view hierarchy
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
        updateRoutelist();
        super.onResume();
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

    /** Loads in routes to routelist from persistence */
    private void updateRoutelist() {
        // get routes from db
        Collection<TransrockRoute> routes = RouteStorage.getAllRoutes();

        // clear adapter
        TransrockRouteAdapter adapter = (TransrockRouteAdapter) routeList.getAdapter();
        adapter.clear();

        // populate list with stored routes, or hide list if there are none
        if (routes.size() != 0) {
            adapter.addAll(routes);
            routeList.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.routelist_noroutes_message).setVisibility(View.GONE);
        }
        else {
            routeList.setVisibility(View.GONE);
            getView().findViewById(R.id.routelist_noroutes_message).setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }
    /**
     * Writes modified routes to persistence.
     *
     * Each time a user turns a route on or off, the corresponding field
     * is set in the backing route object. These changes need to be saved to disk.
     * Instead of writing to disk each time a switch is flipped, it's more
     * efficient to
     */
    private void persistRoutelist() {
        TransrockRoute[] all = ((TransrockRouteAdapter) routeList.getAdapter()).getAll();
        Set<TransrockRoute> modifiedRoutes = new HashSet<>(all.length);
        for (int i = 0; i < all.length; i++)
            modifiedRoutes.add(all[i]);

        RouteStorage.putRoute(modifiedRoutes);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_ROUTES_REQUESTCODE:
                // pull and store any new routes
                HashMap<String, Route> routelist = (HashMap<String, Route>) data.getSerializableExtra("routelist");
                FetchRoutesTask fetchNewRoutes = new FetchRoutesTask();
                fetchNewRoutes.execute(routelist.values().toArray(new Route[0]));
        }
    }

    /**
     * Takes a list of routes, gets necessary data to build TransrockRoutes, sets
     * internal storage to the resulting list (not additive), and then updates
     * this routelist.
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
                    Map<String, String> segments = TransLocAPI.getSegments(route);
                    TransrockRoute trr = new TransrockRoute(route, segments.values().toArray(new String[0]));
                    trr.setActivated(true);
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
            RouteStorage.clear();
            RouteStorage.putRoute(result);
            updateRoutelist();
            snackbar.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // show snackbar with error message
            int messageResId = values[0];
            Snackbar errorSnackbar = Snackbar.make(RoutesFragment.this.getView(), messageResId, Snackbar.LENGTH_LONG);
            // allow user to retry
            errorSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FetchRoutesTask.this.execute(routes);
                }
            });
        }
    }
}
