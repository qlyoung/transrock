package us.v4lk.transrock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.orhanobut.hawk.Hawk;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class MapActivity extends AppCompatActivity {

    Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the layout content
        setContentView(R.layout.activity_map);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        // add drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .build();


        // setup map
        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
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
            case R.id.map_menu_manage_routes:
                startRouteManager();
                break;
            case R.id.map_menu_center_location:
                //TODO: add map code
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startRouteManager() {
        Intent intent = new Intent(this, RouteListActivity.class);
        startActivity(intent);
    }
}
