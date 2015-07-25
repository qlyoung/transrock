package us.v4lk.transrock;

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

public class AddRoutesActivity extends AppCompatActivity {

    ListView agencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_add_routes);

        // set bottom sheet properties
        BottomSheetLayout layout = (BottomSheetLayout) findViewById(R.id.addroute_bottomsheetlayout);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.addroute_toolbar);
        setSupportActionBar(toolbar);

        // capture listview
        agencyList = (ListView) findViewById(R.id.addroute_agency_list);

        //set listener
        AdapterView.OnItemClickListener agencyClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Agency agency = (Agency) view.getTag();
                showRouteBottomSheet(agency);
            }
        };
        agencyList.setOnItemClickListener(agencyClickListener);

    }
    @Override
    protected void onStart() {
        FetchAgencies ga = new FetchAgencies();
        ga.execute();
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



    /**
     * Wraps TransLoc call for agencies, loads in result to agency list when
     * it returns
     */
    class FetchAgencies extends AsyncTask<Integer, Void, Agency[]> {

        @Override
        protected Agency[] doInBackground(Integer... params) {
            int[] ids = new int[params.length];
            for (int i = 0; i < params.length; i++) ids[i] = params[i];

            return TransLocAPI.getAgencies(ids);
        }

        @Override
        public void onPostExecute(Agency[] result) {
            AgencyAdapter adapter = new AgencyAdapter(
                    AddRoutesActivity.this,
                    R.layout.agency_list_item,
                    result);
            agencyList.setAdapter(adapter);
        }
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

            int[] ids = new int[params.length];
            for (int i = 0; i < params.length; i++) ids[i] = params[i];

            /*
            ProgressDialog dialog = ProgressDialog.show(
                    AddRoutesActivity.this,
                    null,
                    "Fetching routes...",
                    true);
                    */

            //TODO: make this actually use multiple ids
            Route[] result = TransLocAPI.getRoutes(ids[0]);

            // dialog.dismiss();

            return result;
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

}
