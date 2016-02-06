package us.v4lk.transrock;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;

import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.transloc.objects.Agency;
import us.v4lk.transrock.transloc.objects.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.util.RouteManager;
import us.v4lk.transrock.util.TransrockRoute;
import us.v4lk.transrock.util.Util;

/**
 * Allows the user to select which routes they want to track.
 * Routes are organized by agency.
 *
 * This activity will fetch and display all routes available for
 * selection. Its display reflects currently saved selections as well.
 *
 * The user makes changes to this list, selecting and deselecting items
 * as desired. When the activity exits, the resultant list is written
 * out to storage, updating the application's internal list.
 *
 * When the activity exits, a copy of the list is put as a Serializable
 * extra in the result intent. The receiving activity should use this list
 * to update the on-disk list, and then update its own view to reflect
 * the new values on disk.
 *
 * The key for the returned serializable is "routeFragment" and the type
 * is HashMap<String, Route>.
 * e.g. data.getSerializableExtra("routeFragment");
 */
public class SelectRoutesActivity extends AppCompatActivity {

    public static String RESULT_EXTRA_KEY = "routelist";

    @Bind(R.id.addroute_bottomsheetlayout) BottomSheetLayout root;
    @Bind(R.id.addroute_agency_list) StickyListHeadersListView agencyList;
    @Bind(R.id.addroute_body_progressbar) ProgressBar bodyProgressBar;
    @Bind(R.id.addroute_toolbar_progressbar) ProgressBar toolbarProgressBar;
    @Bind(R.id.addroute_toolbar) Toolbar toolbar;
    LocationManager locationManager;

    public void onAgencyItemClicked(View v) {
        Agency agency = (Agency) v.getTag();
        showRouteBottomSheet(agency);
    }
    public void onBottomSheetDismissed(View v) {
        ListView bottomSheetView = (ListView) v.findViewById(R.id.bottomsheet_list);
        RouteSwitchAdapter adapter = (RouteSwitchAdapter) bottomSheetView.getAdapter();

        // get changes made
        Map<Route, Boolean> result = adapter.getData();

        // apply changes
        for (Route r : result.keySet())
            if (result.get(r))
                routelist.put(r.route_id, r);
            else
                routelist.remove(r.route_id);
    }

    /**
     * List of routes that the user wishes to store.
     * This is loaded from storage at the beginning of this activity's
     * lifecycle, and returned as an activity result at the end.
     *
     * keys:   route id's
     * values: route api objects
     */
    HashMap<String, Route> routelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_routes);
        ButterKnife.bind(this);

        agencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAgencyItemClicked(view);
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // build location manager
        locationManager = LocationManager.getInstance(this.getApplicationContext());

        // build mapFragment
        routelist = new HashMap<>();
        for (TransrockRoute route : RouteManager.getAllRoutes())
            routelist.put(route.route_id, route.getRoute());
    }
    @Override
    protected void onStart() {
        PopulateListTask populateListTask = new PopulateListTask();
        if (Util.isConnected(this)) populateListTask.execute();
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
    /** Dismiss sheet on back button press if it is showing, otherwise do the normal thing */
    @Override
    public void onBackPressed() {
        if (root.isSheetShowing())
            root.dismissSheet();
        else {
            // set result
            Intent result = new Intent();
            result.putExtra(RESULT_EXTRA_KEY, routelist);
            setResult(RESULT_OK, result);
            finish();
        }
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
                if (!Util.isConnected(SelectRoutesActivity.this)) {
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
    private void showRouteBottomSheet(Route[] routes)  {
        // if sheet is already showing, do nothing
        if (root.isSheetShowing()) return;

        // inflate bottom sheet content view
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottomsheet_list, root, false);

        // capture & set the title
        TextView v = (TextView) bottomSheet.findViewById(R.id.bottomsheet_title);
        v.setText(R.string.routes);

        // setup adapter & listview
        ListView routeList = (ListView) bottomSheet.findViewById(R.id.bottomsheet_list);

        // cross-reference against storageRoutes to build a mapFragment of route -> checked value
        Map<Route, Boolean> routeSwitchMap = new HashMap<>();
        for (Route r : routes) {
            routeSwitchMap.put(r, routelist.containsKey(r.route_id));
        }

        RouteSwitchAdapter adapter = new RouteSwitchAdapter(
                SelectRoutesActivity.this,
                R.layout.route_switch_item,
                routeSwitchMap);

        routeList.setAdapter(adapter);

        // show bottom sheet
        root.showWithSheetView(bottomSheet);

        // modify routeFragment on dismissal
        root.getSheetView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) { }
            @Override
            public void onViewDetachedFromWindow(View v) {
                onBottomSheetDismissed(v);
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

    /** Pulls all agencies from the network and populates the list. */
    private class PopulateListTask extends AsyncTask<Void, Integer, Agency[]> {
        int numActive, numLocal;

        @Override
        protected Agency[] doInBackground(Void... params) {
            // get last location;
            Location loc = locationManager.getLocation();

            // get agency ids of stored routes
            Collection<TransrockRoute> storedRoutes = RouteManager.getAllRoutes();
            int[] storedAgencyIds = Util.getAgencyIds(storedRoutes.toArray(new TransrockRoute[storedRoutes.size()]));

            Agency[] active = null, local = null, all = null;

            try {

                // fetch the various routes from the various sources
                active = storedAgencyIds.length != 0 ?
                        TransLocAPI.getAgencies(storedAgencyIds) :
                        new Agency[0];

                // if we couldn't get the location, don't display that section
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

            // sort alphabetically
            Comparator alphabetical = new Comparator<Agency>() {
                @Override
                public int compare(Agency lhs, Agency rhs) {
                    return lhs.long_name.compareTo(rhs.long_name);
                }
            };
            Arrays.sort(active, alphabetical);
            Arrays.sort(local, alphabetical);
            Arrays.sort(all, alphabetical);

            // convenient holder
            Agency[][] sections = new Agency[][] { active, local, all };

            // flatten into an (ordered) arraylist
            ArrayList<Agency> result = new ArrayList<>(active.length + local.length + all.length);
            for (Agency[] agencyList : sections)
                for (Agency a : agencyList)
                    if (!result.contains(a)) // don't add items twice
                        result.add(a);

            numActive = active.length;
            numLocal = local.length;

            return result.toArray(new Agency[result.size()]);
        }

        @Override
        public void onPostExecute(Agency[] result) {
            // populate agency list
            AgencyAdapter adapter = new AgencyAdapter(
                    SelectRoutesActivity.this,
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
    }

}