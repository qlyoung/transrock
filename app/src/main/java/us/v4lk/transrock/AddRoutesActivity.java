package us.v4lk.transrock;

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
import android.widget.TextView;

import com.flipboard.bottomsheet.BottomSheetLayout;

import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.adapters.AgencyAdapter;
import us.v4lk.transrock.util.LocationManager;

/**
 * Allows the user to select which routes they want to track.
 * Routes are organized by agency.
 */
public class AddRoutesActivity extends AppCompatActivity {

    ListView agencyList;
    LocationManager locationManager;

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
        FetchAgencies fa = new FetchAgencies();
        fa.execute();
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
        // capture root layout
        BottomSheetLayout root = (BottomSheetLayout) findViewById(R.id.addroute_bottomsheetlayout);
        ShowRoutes fr = new ShowRoutes(root);
        fr.execute(agency.agency_id);
    }

    /**
     *
     */
    class ShowRoutes extends AsyncTask<Integer, Void, Route[]> {

        BottomSheetLayout layout;

        public ShowRoutes(BottomSheetLayout layout){
            super();
            this.layout = layout;
        }

        @Override
        protected Route[] doInBackground(Integer... params) {
            int id = params[0];
            return TransLocAPI.getRoutes(id);
        }
        @Override
        protected void onPostExecute(Route[] routes) {
            // inflate bottom sheet
            View bottomSheet = getLayoutInflater().inflate(R.layout.bottomsheet_list, layout, false);

            // capture & set title
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
            layout.showWithSheetView(bottomSheet);
        }
    }
    /**
     * Wraps TransLoc call for agencies, loads in result to agency list when
     * it returns
     */
    class FetchAgencies extends AsyncTask<Void, Void, Agency[][]> {

        @Override
        protected Agency[][] doInBackground(Void... params) {
            // block for location
            while(!locationManager.isConnected());
            Location loc = locationManager.getLocation();

            Agency[] active = null; //TODO: load in active routes from persistence
            Agency[] local = TransLocAPI.getAgencies(loc, 10000); // get all agencies within 10 km
            Agency[] all = TransLocAPI.getAgencies();

            return new Agency[][] { local, all };
        }

        @Override
        public void onPostExecute(Agency[][] result) {
            int active = 0, //TODO: add in active
                local  = result[0].length,
                all    = result[1].length;

            Agency[] cumulative = new Agency[active + local + all];
            int i = 0;
            for (Agency[] al : result) {
                for (Agency a : al)
                    cumulative[i++] = a;
            }

            AgencyAdapter adapter = new AgencyAdapter(
                    AddRoutesActivity.this,
                    R.layout.agency_list_item,
                    cumulative,
                    active,
                    local);
            agencyList.setAdapter(adapter);
        }
    }
}
