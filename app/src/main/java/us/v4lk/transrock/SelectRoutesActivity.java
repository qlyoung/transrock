package us.v4lk.transrock;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.model.Agency;
import us.v4lk.transrock.model.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.transloc.TransLocAPI2;
import us.v4lk.transrock.util.Util;

/**
 * Allows the user to select which routes they want to track.
 *
 * The user makes changes to a list of routes, selecting and deselecting
 * specific routes as desired. When the activity exits, the resultant list
 * is written out to storage, updating the application's internal list.
 *
 */
public class SelectRoutesActivity extends AppCompatActivity {

    // view bindings for this activity's layout
    @Bind(R.id.addroute_bottomsheetlayout)
    BottomSheetLayout root;
    @Bind(R.id.addroute_toolbar_progressbar)
    ProgressBar toolbarProgressBar;
    @Bind(R.id.addroute_toolbar)
    Toolbar toolbar;

    @Bind(R.id.addroute_agency_pane)
    RelativeLayout agencyPane;
    @Bind(R.id.addroute_agency_list)
    StickyListHeadersListView agencyList;
    @Bind(R.id.addroute_agency_progressbar)
    ProgressBar agencyProgressBar;

    @Bind(R.id.addroute_error_pane)
    RelativeLayout errorPane;
    @Bind(R.id.addroute_error_textview)
    TextView errorTextView;
    @Bind(R.id.addroute_error_button)
    Button errorButton;

    LocationManager locationManager;

    // the two realms we will use. the local realm keeps track of changes that are made
    // when the user leaves the activity, these changes are saved back to the global realm
    Realm localRealm, globalRealm;
    // the configuration for the local realm, needed to instantiate the realm in different threads
    RealmConfiguration localconfig;

    /**
     * When an item in the agency list is clicked, show a bottom sheet with its
     * routes so the user can select which ones they want to use.
     * @param v the view that was clicked
     */
    public void onAgencyItemClicked(View v) {
        // list items representing agencies should have the corresponding Agency as their tag
        Agency agency = (Agency) v.getTag();
        new ShowRouteListTask(v).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // restore instance state, if any, and initialize this activity
        super.onCreate(savedInstanceState);

        // set the root layout and bind views
        setContentView(R.layout.activity_select_routes);
        ButterKnife.bind(this);

        // set the click action of each item in the list of agencies
        agencyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAgencyItemClicked(view);
            }
        });

        // bind our toolbar view to serve as the action bar for this activity, and set some properties
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // build location manager
        locationManager = LocationManager.getInstance(this.getApplicationContext());

        // initialize the local realm
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(this);
        localconfig = builder.name("selectroutes.realm").inMemory().build();
        localRealm = Realm.getInstance(localconfig);

        // get a reference to global realm
        globalRealm = Realm.getDefaultInstance();
        // copy routes from global realm to local realm
        List<Route> fromRealm = globalRealm.copyFromRealm(globalRealm.allObjects(Route.class));
        localRealm.beginTransaction();
        localRealm.copyToRealmOrUpdate(fromRealm);
        localRealm.commitTransaction();
    }

    @Override
    protected void onStart() {
        // make a task to fetch agencies from TransLoc and populate the list
        PopulateAgencyListTask populateListTask = new PopulateAgencyListTask();
        populateListTask.execute();

        super.onStart();
    }

    @Override
    protected void onPause() {

        // get all saved routes
        RealmResults<Route> savedRoutes = localRealm.where(Route.class).equalTo("saved", true).findAll();

        // get the agencies they belong to
        Set<Agency> savedAgencies = new HashSet<>();
        for (Route route : savedRoutes) {
            Agency agency = localRealm.where(Agency.class).equalTo("agencyId", route.getAgencyId()).findFirst();
            if (agency != null)
                savedAgencies.add(agency);
        }

        // activate those agencies in the local realm
        localRealm.beginTransaction();
        for (Agency agency : savedAgencies) agency.setActive(true);
        localRealm.commitTransaction();

        // copy local changes to global realm
        globalRealm.beginTransaction();
        globalRealm.clear(Route.class);
        globalRealm.clear(Agency.class);
        globalRealm.copyToRealmOrUpdate(savedRoutes);
        globalRealm.copyToRealmOrUpdate(savedAgencies);
        globalRealm.commitTransaction();

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Todo: save local realm & restore in onCreate
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // close our realm instances to free their resources
        localRealm.close();
        globalRealm.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // hook our options menu to this activity
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
     * Dismiss route sheet if the back button press while it is showing, otherwise
     * fall back to the superclass
     * */
    @Override
    public void onBackPressed() {
        if (root.isSheetShowing()) {
            root.dismissSheet();
            return;
        }

        super.onBackPressed();
    }

    /**
     * Changes the background of the activity to the supplied error message,
     * hiding the progress spinner.
     *
     * @param errorStringResource The id of the string resource defining the message to display.
     */
    private void showErrorPane(int errorStringResource, View.OnClickListener buttonListener) {
        // make sure spinner is hidden
        agencyPane.setVisibility(View.GONE);

        // set the error message, bind button click listener, show error pane
        errorTextView.setText(errorStringResource);
        errorButton.setOnClickListener(buttonListener);
        errorPane.setVisibility(View.VISIBLE);
    }

    /**
     * Shows a popup with the specified message.
     *
     * @param errorStringResource The id of the string resource defining the message to display.
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

    /**
     * Asynchronous task run at activity start.
     * This task is responsible for populating the StickyListHeadersListView passed to it
     * with Agency items.
     *
     * Fresh records of each transit agency are pulled from TransLoc's API and stored in the local
     * realm. Then records stored in the persisted realm on disk are merged into the local realm.
     * The agency list is populated using these records.
     *
     * If an error occurs, the body progress spinner is dismissed and the background of the
     * activity is set to a message describing the error. Any errors will result in the cancellation
     * of the task and the list will not be populated.
     */
    private class PopulateAgencyListTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            // hide the error pane if it is visible
            errorPane.setVisibility(View.GONE);

            // unhide the agency pane and set it to the spinner
            agencyPane.setVisibility(View.VISIBLE);
            agencyList.setVisibility(View.GONE);
            agencyProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // get last location, if known
            Location loc = locationManager.getLocation();

            // get instances of the realms we need for this thread
            Realm local = Realm.getInstance(localconfig);
            Realm global = Realm.getDefaultInstance();

            // refresh the realm to ensure synchronization with other threads
            global.refresh();
            try {
                // get all agency records from the TransLoc API
                String url = TransLocAPI2.agencies();
                JsonObject response = Ion.with(SelectRoutesActivity.this).load(url).setHeader(TransLocAPI2.API_KEY_HEADER, TransLocAPI2.API_KEY).asJsonObject().get();
                JSONObject re = new JSONObject(response.toString());
                ArrayList<Agency> agencies = new ArrayList<Agency>(Arrays.asList(TransLocAPI2.buildAgencies(re)));

                // add them to our local realm, and then update these records to reflect the state
                // of any corresponding records we have on disk
                local.beginTransaction();
                local.copyToRealmOrUpdate(agencies);
                local.copyToRealmOrUpdate(global.where(Agency.class).equalTo("active", true).findAll());
                local.commitTransaction();
            }
            catch (ExecutionException e) {
                publishProgress(R.string.error_network_unknown);
                this.cancel(true);
                return null;
            }
            catch (InterruptedException e) {
                publishProgress(R.string.error_network_unknown);
                this.cancel(true);
                return null;
            }
            catch (JSONException e) {
                publishProgress(R.string.error_bad_parse);
                this.cancel(true);
                return null;
            }
            finally {
                // will execute even if we returned in a catch block
                local.close();
                global.close();
            }

            return null;
        }

        @Override
        public void onPostExecute(Void result) {
            // sort agencies by whether we have them on disk, then by lexicographic order
            Agency[] agencies = localRealm.allObjectsSorted(Agency.class,
                                                            "active", Sort.DESCENDING,
                                                            "longName", Sort.ASCENDING)
                                                            .toArray(new Agency[0]);
            // count the number of records we have on disk
            int numActive = (int) localRealm.where(Agency.class).equalTo("active", true).count();
            int numLocal = 0;

            // load agencies into the StickyListHeadersListView adapter
            AgencyAdapter adapter = new AgencyAdapter(
                    SelectRoutesActivity.this,
                    R.layout.agency_list_item,
                    agencies,
                    numActive,
                    numLocal,
                    localRealm
            );

            // bind this adapter to the view
            agencyList.setAdapter(adapter);

            // hide spinner and show list
            agencyProgressBar.setVisibility(View.GONE);
            agencyList.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... messageStringResourceId) {
            View.OnClickListener retryListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PopulateAgencyListTask().execute();
                }
            };

            showErrorPane(messageStringResourceId[0], retryListener);
        }

    }

    /**
     * Shows a bottom sheet with the routes for the specified agency.
     *
     * This AsyncTask fetches route records for the specified agency from the TransLoc API. After
     * the records are fetched, it will use the showBottomSheet() method to display them.
     *
     * While fetching is in progress, the activity's indeterminate progress bar is shown.
     *
     * If fetching the routes fails, a popup will be displayed with a message describing the error
     * and the progress bar will be hidden.
     *
     */
    private class ShowRouteListTask extends AsyncTask<Void, Integer, Void> {
        private String agencyId;
        private View agencyItemView;

        public ShowRouteListTask(View agencyItemView) {
            this.agencyItemView = agencyItemView;
            this.agencyId = ((Agency) agencyItemView.getTag()).getAgencyId();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (root.isSheetShowing()) return;

            // before doing anything, check if we're connected
            if (!Util.isConnected(SelectRoutesActivity.this)) {
                showPopupError(R.string.error_network_disconnected);
                this.cancel(true);
                return;
            } else
                toolbarProgressBar.setVisibility(View.VISIBLE);

        }

        /**
         * @param params the id of the agency to show routes for
         * @return nothing
         */
        @Override
        protected Void doInBackground(Void... params) {
            Route[] routes;
            Realm local = Realm.getInstance(localconfig);

            try {
                // try to fetch all routes from the TransLoc API
                String url = TransLocAPI2.routes(agencyId);
                JsonObject response = Ion.with(SelectRoutesActivity.this).load(url).setHeader(TransLocAPI2.API_KEY_HEADER, TransLocAPI2.API_KEY).asJsonObject().get();
                JSONObject re = new JSONObject(response.toString());
                routes = TransLocAPI2.buildRoutes(re).get(agencyId);
            }
            catch (ExecutionException e) {
                publishProgress(R.string.error_network_unknown);
                this.cancel(true);
                return null;
            }
            catch (InterruptedException e) {
                publishProgress(R.string.error_network_unknown);
                this.cancel(true);
                return null;
            }
            catch (JSONException e) {
                publishProgress(R.string.error_bad_parse);
                this.cancel(true);
                return null;
            }


            // if no routes were returned, say so
            if (routes == null || routes.length == 0) {
                publishProgress(R.string.error_no_routes);
                this.cancel(true);
                return null;
            }

            // copy fetched routes to local realm if they do not already exist
            local.beginTransaction();
            for (int i = 0; i < routes.length; i++) {
                boolean alreadyInRealm = local
                        .where(Route.class)
                        .equalTo("routeId", routes[i].getRouteId())
                        .count() > 0;

                if (!alreadyInRealm)
                    routes[i] = local.copyToRealm(routes[i]);
            }
            local.commitTransaction();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // fetch all matching routes from local realm & show sheet
            Route[] result = localRealm
                    .where(Route.class)
                    .equalTo("agencyId", agencyId)
                    .findAll()
                    .toArray(new Route[0]);

            // if sheet is already showing, do nothing
            if (root.isSheetShowing()) return;

            // inflate bottom sheet content view
            View bottomSheet = getLayoutInflater().inflate(R.layout.bottomsheet_list, root, false);

            // capture & set the title
            TextView v = (TextView) bottomSheet.findViewById(R.id.bottomsheet_title);
            v.setText(R.string.routes);

            // bind given routes to the ListView's adapter and bind the adapter to the view.
            ListView routeList = (ListView) bottomSheet.findViewById(R.id.bottomsheet_list);
            RouteSwitchAdapter adapter = new RouteSwitchAdapter(SelectRoutesActivity.this,
                                                                R.layout.route_switch_item,
                                                                result,
                                                                localRealm);
            routeList.setAdapter(adapter);

            // show bottom sheet
            root.showWithSheetView(bottomSheet);
            root.getSheetView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) { }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    // update badge route counter on agency view on sheet dismissal
                    TextView badgeCounter = (TextView) agencyItemView.findViewById(R.id.agency_list_item_badge_text);
                    int numActiveRoutes = (int) localRealm
                                                    .where(Route.class)
                                                    .equalTo("agencyId", agencyId)
                                                    .equalTo("saved", true).count();

                    badgeCounter.setText(String.valueOf(numActiveRoutes));
                }
            });

            toolbarProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            showPopupError(values[0]);
        }

        @Override
        protected void onCancelled(Void aVoid) {
            // hide the toolbar progress bar
            toolbarProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}