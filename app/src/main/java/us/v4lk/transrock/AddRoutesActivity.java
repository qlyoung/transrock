package us.v4lk.transrock;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
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

import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.util.LocationManager;
import us.v4lk.transrock.util.Util;

/**
 * Allows the user to select which routes they want to track.
 * Routes are organized by agency.
 */
public class AddRoutesActivity extends AppCompatActivity {

    ListView agencyList;
    LocationManager locationManager;
    ProgressBar agencyLoadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_add_routes);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.addroute_toolbar);
        setSupportActionBar(toolbar);

        // capture listview
        agencyList = (ListView) findViewById(R.id.addroute_agency_list);

        // capture progressbar
        agencyLoadingSpinner = (ProgressBar) findViewById(R.id.addroute_progressbar);

        // set listener
        AdapterView.OnItemClickListener agencyClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Agency agency = (Agency) view.getTag();
                showRouteBottomSheet(agency);
            }
        };
        agencyList.setOnItemClickListener(agencyClickListener);

        // build location manager
        locationManager = new LocationManager(this);
    }
    @Override
    protected void onStart() {
        if (Util.isConnected(this)) {
            FetchAgencies fa = new FetchAgencies();
            fa.execute();
        }
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
        return false;
    }

    private void showRouteBottomSheet(Agency agency) {
        // this AsyncTask will fetch the routes and call the other overload when it finishes.
        AsyncTask<Integer, Integer, Route[]> fetchRoutes = new AsyncTask<Integer, Integer, Route[]>() {
            @Override
            protected Route[] doInBackground(Integer... params) {
                int id = params[0];

                Route[] routes = null;
                try {
                    routes = TransLocAPI.getRoutes(id);
                }
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
                Message message = new Message();
                message.obj = routes;
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
        };
        fetchRoutes.execute(agency.agency_id);
    }
    private void showRouteBottomSheet(Route[] routes) {
        // capture root view
        BottomSheetLayout root = (BottomSheetLayout) findViewById(R.id.addroute_bottomsheetlayout);

        // inflate bottom sheet
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottomsheet_list, root, false);

        // capture & set the title
        TextView v = (TextView) bottomSheet.findViewById(R.id.bottomsheet_title);
        v.setText(R.string.routes);

        // capture list & set adapter
        ListView routeList = (ListView) bottomSheet.findViewById(R.id.bottomsheet_list);
        RouteSwitchAdapter adapter = new RouteSwitchAdapter(
                AddRoutesActivity.this,
                R.layout.route_switch_item,
                routes);
        routeList.setAdapter(adapter);

        // show bottom sheet
        root.showWithSheetView(bottomSheet);
    }
    private void showError(int errorStringResource) {
        // make sure spinner is hidden
        agencyLoadingSpinner.setVisibility(View.GONE);

        // capture error message view
        TextView emv = (TextView) findViewById(R.id.addroute_error);
        emv.setText(errorStringResource);
        emv.setVisibility(View.VISIBLE);
    }
    private void showPopupError(int errorStringResource) {
        AlertDialog.Builder dgb = new AlertDialog.Builder(this);

        dgb.setMessage(getResources().getString(errorStringResource))
           .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   dialog.dismiss();
               }
           });

        dgb.show();
    }

    /**
     * Wraps TransLoc call for agencies, loads in result to agency list when
     * it returns
     */
    class FetchAgencies extends AsyncTask<Void, Integer, Agency[]> {

        int numActive, numLocal;

        @Override
        protected Agency[] doInBackground(Void... params) {
            // TODO: fix this. seriously.
            // wait a few seconds for api to connect
            try { Thread.sleep(2000); } catch (Exception e) {}
            Location loc = locationManager.getLocation();

            Agency[] active = null,
                     local = null,
                     all = null;
            try {
                active = new Agency[0]; //TODO: load in active routes from persistence
                local = loc == null ? TransLocAPI.getAgencies(loc, 10000) : new Agency[0];
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

            // concatenate all agencies into single array
            Agency[][] cumulative = new Agency[][] { active, local, all };
            Agency[] sum = new Agency[active.length + local.length + all.length];
            int i = 0;
            for (Agency[] al : cumulative)
                for (Agency a : al)
                    sum[i++] = a;

            return sum;
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

            agencyLoadingSpinner.setVisibility(View.GONE);
        }

        /**
         * Since we never actually need to update progress, i'm hijacking this
         * method and using it to display any potential error messages.
         * Pretend the method is called "displayError".
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            showError(values[0]);
        }
    }
}
