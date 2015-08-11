package us.v4lk.transrock;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import us.v4lk.transrock.util.LocationManager;
import us.v4lk.transrock.util.Util;

public class MapActivity extends AppCompatActivity {

    Drawer drawer;
    LocationManager locationManager;
    MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the layout content
        setContentView(R.layout.activity_map);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        // make drawer
        drawer = Util.buildMainMenu(this, toolbar);

        // setup map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // location manager
        locationManager = new LocationManager(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu_center_location:
                centerAndZoomOnLocation();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void startRouteManager() {

    }
    public void centerAndZoomOnLocation() {
        Location l = locationManager.getLocation();
        if (l != null) {
            GeoPoint center = new GeoPoint(l.getLatitude(), l.getLongitude());
            map.getController().setCenter(center);
            map.getController().setZoom(20);
        } else {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage("hi");
            b.show();
        }
    }
}
