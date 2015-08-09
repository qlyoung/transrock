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
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.orhanobut.hawk.Hawk;

import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.adapters.RouteAdapter;
import us.v4lk.transrock.adapters.RouteSwitchAdapter;
import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.Util;


public class RouteListActivity extends AppCompatActivity {

    ListView routeList;
    Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_routelist);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.routelist_toolbar);
        setSupportActionBar(toolbar);

        // add drawer
        drawer = new DrawerBuilder()
                        .withActivity(this)
                        .withToolbar(toolbar)
                        .build();

        // capture listview
        routeList = (ListView) findViewById(R.id.routelist);

        // setup empty adapter
        routeList.setAdapter(new RouteAdapter(this, R.layout.route_list_item));

        updateRoutelist();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        updateRoutelist();
        super.onResume();
    }

    public void addRoute(View v) {
        Intent intent = new Intent(this, AddRoutesActivity.class);
        startActivity(intent);
    }

    private void updateRoutelist() {
        // get routes from db
        Set<Route> routes = Hawk.get(Util.ROUTES_STORAGE_KEY, new HashSet<Route>());

        // clear adapter
        RouteAdapter adapter = (RouteAdapter) routeList.getAdapter();
        adapter.clear();

        // populate list with user's routes, or hide list if there are none
        if (routes.size() != 0) {
            adapter.addAll(routes);
            routeList.setVisibility(View.VISIBLE);
            findViewById(R.id.routelist_noroutes_message).setVisibility(View.GONE);
        }
        else {
            routeList.setVisibility(View.GONE);
            findViewById(R.id.routelist_noroutes_message).setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }
}
