package us.v4lk.transrock;

import android.content.Intent;
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

import us.v4lk.transrock.adapters.RouteAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.transloc.TransLocAPI;


public class RouteListActivity extends AppCompatActivity {

    ListView routeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_routelist);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.routelist_toolbar);
        setSupportActionBar(toolbar);

        // capture listview
        routeList = (ListView) findViewById(R.id.routelist);

        // TODO: query db to retrieve user's routes here
        Route[] routes = null;

        // populate list with user's routes, or hide list if there are none
        if (routes != null) {
            // populate list

            routeList.setAdapter(new RouteAdapter(this, R.layout.route_list_item, routes));
        } else
            routeList.setVisibility(View.GONE);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_routelist, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onResume() {
        //TODO: reload (potentially) updated routelist from db
        super.onResume();
    }

    public void addRoute(View v) {
        Intent intent = new Intent(this, AddRoutesActivity.class);
        startActivity(intent);
    }
}
