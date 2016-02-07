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

import butterknife.Bind;
import butterknife.ButterKnife;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.model.AgencyModel;
import us.v4lk.transrock.model.RouteModel;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.Util;

/**
 * Allows the user to select which routes they want to track.
 * Routes are organized by agency.
 * <p/>
 * This activity will fetch and display all routes available for
 * selection. Its display reflects currently saved selections as well.
 * <p/>
 * The user makes changes to this list, selecting and deselecting items
 * as desired. When the activity exits, the resultant list is written
 * out to storage, updating the application's internal list.
 * <p/>
 * When the activity exits, a copy of the list is put as a Serializable
 * extra in the result intent. The receiving activity should use this list
 * to update the on-disk list, and then update its own view to reflect
 * the new values on disk.
 * <p/>
 * The key for the returned serializable is "routeFragment" and the type
 * is HashMap<String, Route>.
 * e.g. data.getSerializableExtra("routeFragment");
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

    public void onAgencyItemClicked(View v) {
        AgencyModel agency = (AgencyModel) v.getTag();
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
    }

    /**
     * Shows a bottom sheet with the routes for the specified agency.
     * This overload will create an AsyncTask that fetches the routes and then calls the other
     * overload to display them.
     *
     * @param agency the agency whose routes should be displayed in the bottom sheet
     */
    private void showRouteBottomSheet(AgencyModel agency) {
        // this AsyncTask will fetch the routes and call the other overload when it finishes.
        AsyncTask fetchRoutes = new AsyncTask<AgencyModel, Integer, RouteModel[]>() {

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
            protected RouteModel[] doInBackground(AgencyModel... params) {
                AgencyModel agency = params[0];

                RouteModel[] routes = null;

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
            protected void onPostExecute(RouteModel[] routes) {
                toolbarProgressBar.setVisibility(View.INVISIBLE);
                showRouteBottomSheet(routes);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                showPopupError(values[0]);
            }

            @Override
            protected void onCancelled(RouteModel[] routes) {
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
    private void showRouteBottomSheet(RouteModel[] routes) {
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
        root.showWithSheetView(bottomSheet);
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
    private class PopulateListTask extends AsyncTask<Void, Integer, AgencyModel[]> {
        int numActive, numLocal;

        @Override
        protected AgencyModel[] doInBackground(Void... params) {

            // get last location, if known
            Location loc = locationManager.getLocation();

            AgencyModel[] stored = null, local = null, all = null;

            try {
                stored = Util.realm.where(AgencyModel.class).findAllSorted("longName").toArray(new AgencyModel[0]);
                all = TransLocAPI.getAgencies();
                if (loc != null)
                    local = TransLocAPI.getAgencies(loc, 10000);
                else
                    local = new AgencyModel[0];

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

            ArrayList<AgencyModel> result = new ArrayList<>(stored.length + local.length + all.length);

            for (AgencyModel agency : stored)
                if (!result.contains(agency))
                    result.add(agency);

            for (AgencyModel agency : local)
                if (!result.contains(agency))
                    result.add(agency);

            for (AgencyModel agency : all)
                if (!result.contains(agency))
                    result.add(agency);

            numActive = stored.length;
            numLocal = local.length;

            return result.toArray(new AgencyModel[result.size()]);
        }

        @Override
        public void onPostExecute(AgencyModel[] result) {
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