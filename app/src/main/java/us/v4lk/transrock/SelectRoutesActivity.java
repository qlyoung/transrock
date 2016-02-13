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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;

import org.json.JSONException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.model.Agency;
import us.v4lk.transrock.model.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
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

    @Bind(R.id.addroute_bottomsheetlayout)
    BottomSheetLayout root;
    @Bind(R.id.addroute_agency_list)
    StickyListHeadersListView agencyList;
    @Bind(R.id.addroute_body_progressbar)
    ProgressBar bodyProgressBar;
    @Bind(R.id.addroute_toolbar_progressbar)
    ProgressBar toolbarProgressBar;
    @Bind(R.id.addroute_toolbar)
    Toolbar toolbar;
    LocationManager locationManager;

    Realm localRealm, globalRealm;

    public void onAgencyItemClicked(View v) {
        Agency agency = (Agency) v.getTag();
        showRouteBottomSheet(agency);
    }

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

        // initialize in-memory realm
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(this);
        RealmConfiguration config = builder.name("selectroutes.realm").inMemory().build();
        localRealm = Realm.getInstance(config);

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
        PopulateListTask populateListTask = new PopulateListTask();

        if (Util.isConnected(this))
            populateListTask.execute();
        else
            showBodyError(R.string.error_network_disconnected);

        super.onStart();
    }

    @Override
    protected void onPause() {
        // copy local changes to global realm
        globalRealm.beginTransaction();
        globalRealm.clear(Route.class);
        globalRealm.copyToRealmOrUpdate(localRealm.where(Route.class).equalTo("saved", true).findAll());
        globalRealm.commitTransaction();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        localRealm.close();
        globalRealm.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (root.isSheetShowing()) {
            root.dismissSheet();
            return;
        }

        super.onBackPressed();
    }

    /**
     * Shows a bottom sheet with the routes for the specified agency.
     * This overload will create an AsyncTask that fetches the routes and then calls the other
     * overload to display them.
     *
     * @param agency the agency whose routes should be displayed in the bottom sheet
     */
    private void showRouteBottomSheet(Agency agency) {
        // if sheet is already showing, do nothing
        if (root.isSheetShowing()) return;

        // this AsyncTask will fetch the routes and call the other overload when it finishes.
        AsyncTask<Agency, Integer, Route[]> fetchRoutes = new AsyncTask<Agency, Integer, Route[]>() {

            @Override
            protected void onPreExecute() {
                // before doing anything, check if we're connected
                if (!Util.isConnected(SelectRoutesActivity.this)) {
                    showPopupError(R.string.error_network_disconnected);
                    this.cancel(true);
                } else
                    toolbarProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Route[] doInBackground(Agency... params) {
                Agency agency = params[0];
                Route[] routes = null;

                try {
                    routes = TransLocAPI.getRoutes(agency.getAgencyId());
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

                return routes;
            }

            @Override
            protected void onPostExecute(Route[] routes) {
                // if no routes were returned, show an error
                if (routes.length == 0) {
                    showPopupError(R.string.error_no_routes);
                    return;
                }

                // copy fetched routes to local realm if they do not already exist
                localRealm.beginTransaction();
                for (int i = 0; i < routes.length; i++) {
                    boolean alreadyInRealm = localRealm.where(Route.class)
                                                        .equalTo("routeId", routes[i].getRouteId())
                                                        .count() > 0;

                    if (!alreadyInRealm)
                        routes[i] = localRealm.copyToRealm(routes[i]);
                }
                localRealm.commitTransaction();

                // fetch all matching routes from local realm & show sheet
                Route[] result = localRealm.where(Route.class)
                                                .equalTo("agencyId", routes[0].getAgencyId())
                                                .findAll()
                                                .toArray(new Route[0]);

                showRouteBottomSheet(result);
                toolbarProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                showPopupError(values[0]);
            }

            @Override
            protected void onCancelled(Route[] routes) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
            }
        };
        fetchRoutes.execute(agency);
    }

    /**
     * Shows a bottom sheet with a list of toggleable routes.
     *
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

        // set listview's adapter
        ListView routeList = (ListView) bottomSheet.findViewById(R.id.bottomsheet_list);
        RouteSwitchAdapter adapter = new RouteSwitchAdapter(
                SelectRoutesActivity.this,
                R.layout.route_switch_item,
                routes);
        routeList.setAdapter(adapter);

        // show bottom sheet
        localRealm.beginTransaction();
        root.showWithSheetView(bottomSheet);

        root.getSheetView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                localRealm.commitTransaction();
            }
        });
    }

    private void showBodyError(int errorStringResource) {
        // make sure spinner is hidden
        bodyProgressBar.setVisibility(View.GONE);
        // capture error message view
        TextView emv = (TextView) findViewById(R.id.addroute_error);
        emv.setText(errorStringResource);
        emv.setVisibility(View.VISIBLE);
    }

    /**
     * Shows a popup with the specified message.
     *
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

    /**
     * Asynchronous task run at activity start.
     * Pulls all agencies, organizes them by category, creates a custom list adapter
     * with the resultant data, and sets the adapter to back this.agencyList. Then
     * it dismisses the progress spinner and quits.
     * <p/>
     * Any errors are reported with a dynamically created snackbar on the UI thread.
     */
    private class PopulateListTask extends AsyncTask<Void, Integer, Agency[]> {
        int numActive, numLocal;

        @Override
        protected Agency[] doInBackground(Void... params) {

            // get last location, if known
            Location loc = locationManager.getLocation();
            // get instance of realm
            //Realm realm = null;

            Agency[] stored = null, local = null, all = null;

            try {
                //realm = Realm.getDefaultInstance();
                stored = new Agency[0]; //realm.where(AgencyModel.class).findAllSorted("longName").toArray(new AgencyModel[0]);
                all = TransLocAPI.getAgencies();
                if (loc != null)
                    local = TransLocAPI.getAgencies(loc, 10000);
                else
                    local = new Agency[0];

            } catch (SocketTimeoutException e) {
                publishProgress(R.string.error_network_timeout);
                this.cancel(true);
            } catch (NetworkErrorException e) {
                publishProgress(R.string.error_network_unknown);
                this.cancel(true);
            } catch (JSONException e) {
                publishProgress(R.string.error_bad_parse);
                this.cancel(true);
            } finally {
                //realm.close();
            }

            ArrayList<Agency> result = new ArrayList<>(stored.length + local.length + all.length);

            for (Agency agency : stored)
                if (!result.contains(agency))
                    result.add(agency);

            for (Agency agency : local)
                if (!result.contains(agency))
                    result.add(agency);

            for (Agency agency : all)
                if (!result.contains(agency))
                    result.add(agency);

            numActive = stored.length;
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
        protected void onProgressUpdate(Integer... messageStringResourceId) {
            showBodyError(messageStringResourceId[0]);
        }
    }

}