package us.v4lk.transrock;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.orhanobut.hawk.Hawk;

import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.TransrockRoute;
import us.v4lk.transrock.util.Util;

/**
 * Allows the user to select which routes they want to track.
 * Routes are organized by agency.
 */
public class AddRoutesActivity extends AppCompatActivity {

    /** Root layout */
    BottomSheetLayout root;
    /** List of agencies */
    StickyListHeadersListView agencyList;
    /** body and toolbar progress bars */
    ProgressBar bodyProgressBar, toolbarProgressBar;
    /** location manager */
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_add_routes);

        // capture content
        root = (BottomSheetLayout) findViewById(R.id.addroute_bottomsheetlayout);

        // set toolbar as action bar, enable back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.addroute_toolbar);
        setSupportActionBar(toolbar);

        // enable home button
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // capture views
        agencyList = (StickyListHeadersListView) findViewById(R.id.addroute_agency_list);
        bodyProgressBar = (ProgressBar) findViewById(R.id.addroute_body_progressbar);
        toolbarProgressBar = (ProgressBar) findViewById(R.id.addroute_toolbar_progressbar);

        // set listener
        AdapterView.OnItemClickListener agencyClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Agency a = (Agency) view.getTag();
                showRouteBottomSheet(a);
            }
        };
        agencyList.setOnItemClickListener(agencyClickListener);

        // build location manager
        locationManager = new LocationManager(this);

    }
    @Override
    protected void onStart() {
        /** asynctask to fetch all agencies & populate list */
        AsyncTask<Void, Integer, Agency[]> fetchAgencies = new AsyncTask<Void, Integer, Agency[]>() {

            int numActive, numLocal;

            @Override
            protected Agency[] doInBackground(Void... params) {
                // get last location;
                Location loc = locationManager.getLocation();

                // get agency ids of stored routes
                Collection<TransrockRoute> storedRoutes = RouteStorage.getAllRoutes();
                int[] storedAgencyIds = Util.getAgencyIds(storedRoutes.toArray(new TransrockRoute[storedRoutes.size()]));

                Agency[] active = null,
                        local = null,
                        all = null;

                try {
                    // fetch the various routes from the various sources
                    active = storedAgencyIds.length != 0 ?
                            TransLocAPI.getAgencies(storedAgencyIds) :
                            new Agency[0];
                    local = loc != null ? TransLocAPI.getAgencies(loc, 10000) : new Agency[0];
                    all = TransLocAPI.getAgencies();
                } catch (SocketTimeoutException e) {
                    publishProgress(R.string.error_network_timeout);
                    this.cancel(true);
                }
                catch (NetworkErrorException e) {
                    publishProgress(R.string.error_network_unknown);
                    this.cancel(true);
                }
                catch (JSONException e) {
                    publishProgress(R.string.error_bad_parse);
                    this.cancel(true);
                }

                // concatenate all agencies into single ordered array
                // the order of cumulative is important for correct categorical sorting
                Agency[][] cumulative = new Agency[][] { active, local, all };
                ArrayList<Agency> sum = new ArrayList<>(active.length + local.length + all.length);
                for (Agency[] al : cumulative)
                    for (Agency a : al)
                        if (!sum.contains(a)) // don't add items twice
                            sum.add(a);

                numActive = active.length;
                numLocal = local.length;

                return sum.toArray(new Agency[sum.size()]);
            }
            @Override
            public void onPostExecute(Agency[] result) {
                // populate agency list
                AgencyAdapter adapter = new AgencyAdapter(
                        AddRoutesActivity.this,
                        R.layout.agency_list_item,
                        result,
                        numActive,
                        numLocal);
                agencyList.setAdapter(adapter);

                // hide body progress spinner
                bodyProgressBar.setVisibility(View.GONE);
            }
            @Override
            protected void onProgressUpdate(Integer... values) {
                showError(values[0]);
            }
        };
        if (Util.isConnected(this)) fetchAgencies.execute();
        else showError(R.string.error_network_disconnected);

        super.onStart();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_route, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // capture back button press
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * Dismiss sheet on back button press if it is showing, otherwise do the normal thing
     */
    @Override
    public void onBackPressed() {
        if (root.isSheetShowing())
            root.dismissSheet();
        else
            super.onBackPressed();
    }

    /**
     * Shows a bottom sheet with the routes for the specified agency.
     * This overload will create an AsyncTask that fetches the routes and then calls the other
     * overload to display them.
     * @param agency the agency whose routes should be displayed in the bottom sheet
     */
    private void showRouteBottomSheet(Agency agency) {
        // this AsyncTask will fetch the routes and call the other overload when it finishes.
        AsyncTask<Integer, Integer, Route[]> fetchRoutes = new AsyncTask<Integer, Integer, Route[]>() {

            @Override
            protected void onPreExecute() {
                // before doing anything, check if we're connected
                if (!Util.isConnected(AddRoutesActivity.this)) {
                    showPopupError(R.string.error_network_disconnected);
                    this.cancel(true);
                }
                else
                    toolbarProgressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Route[] doInBackground(Integer... params) {
                int id = params[0];

                Route[] routes = null;

                try { routes = TransLocAPI.getRoutes(id); }

                catch (SocketTimeoutException e) {
                    publishProgress(R.string.error_network_timeout);
                    this.cancel(true);
                }
                catch (NetworkErrorException e) {
                    publishProgress(R.string.error_network_unknown);
                    this.cancel(true);
                }
                catch (JSONException e) {
                    publishProgress(R.string.error_bad_parse);
                    this.cancel(true);
                }

                return routes;
            }
            @Override
            protected void onPostExecute(Route[] routes) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                showRouteBottomSheet(routes);
            }

            /**
             * Since we never actually need to update progress, i'm hijacking this
             * method and using it to display any potential error messages.
             * Pretend the method is called "displayError".
             */
            @Override
            protected void onProgressUpdate(Integer... values) {
                showPopupError(values[0]);
            }

            @Override
            protected void onCancelled(Route[] routes) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
            }
        };
        fetchRoutes.execute(agency.agency_id);
    }
    /**
     * Shows a bottom sheet with a list of toggleable routes.
     * @param routes the routes to show
     */
    private void showRouteBottomSheet(Route[] routes) {
        // if sheet is already showing, do nothing
        if (root.isSheetShowing()) return;

        // inflate bottom sheet content view
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottomsheet_list, root, false);

        // capture & set the title
        TextView v = (TextView) bottomSheet.findViewById(R.id.bottomsheet_title);
        v.setText(R.string.routes);

        // capture list & set adapter
        ListView routeList = (ListView) bottomSheet.findViewById(R.id.bottomsheet_list);
        RouteSwitchAdapter adapter = new RouteSwitchAdapter( AddRoutesActivity.this, R.layout.route_switch_item, routes);
        routeList.setAdapter(adapter);

        // show bottom sheet
        root.showWithSheetView(bottomSheet);

        // persist route changes on sheet dismissal
        root.getSheetView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                ListView rl = (ListView) v.findViewById(R.id.bottomsheet_list);
                RouteSwitchAdapter adapter = (RouteSwitchAdapter) rl.getAdapter();

                Map<Route, Boolean> deltas = adapter.getDeltas();

                // remove all routes
                for (Route route : deltas.keySet())
                    RouteStorage.removeRoute(route.route_id);

                final ArrayList<Route> needToBeAdded = new ArrayList<Route>();
                for (Route route : deltas.keySet()) {
                    if (adapter.getDeltas().get(route))
                        needToBeAdded.add(route);
                }

                // add routes to storage
                for (Route route : needToBeAdded) {
                    TransrockRoute tRoute = new TransrockRoute(route);
                    RouteStorage.putRoute(tRoute);
                }

                // fire off an asynctask to cache some data we might want later
                AsyncTask<Route, Void, Void> downloadAndAddRoutes = new AsyncTask<Route, Void, Void>() {
                    @Override
                    protected Void doInBackground(Route... routes) {
                        for (Route route : routes)
                            try { TransLocAPI.getSegments(route).values(); } catch (Exception e) { }
                        return null;
                    }
                };
                downloadAndAddRoutes.execute(needToBeAdded.toArray(new Route[needToBeAdded.size()]));
            }
        });
    }
    /**
     * Shows the body error view with the specified message.
     * @param errorStringResource resource id of string error message
     */
    private void showError(int errorStringResource) {
        // make sure spinner is hidden
        bodyProgressBar.setVisibility(View.GONE);

        // capture error message view
        TextView emv = (TextView) findViewById(R.id.addroute_error);
        emv.setText(errorStringResource);
        emv.setVisibility(View.VISIBLE);
    }
    /**
     * Shows a popup with the specified message.
     * @param errorStringResource resource id of string error message
     */
    private void showPopupError(int errorStringResource) {
        AlertDialog.Builder popup = new AlertDialog.Builder(this);

        popup.setMessage(getResources().getString(errorStringResource))
           .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   dialog.dismiss();
               }
           });

        popup.show();
    }

}