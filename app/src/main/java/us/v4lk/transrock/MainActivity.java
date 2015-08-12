package us.v4lk.transrock;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import us.v4lk.transrock.fragments.MapFragment;
import us.v4lk.transrock.fragments.RoutesFragment;
import us.v4lk.transrock.util.Util;

public class MainActivity extends AppCompatActivity {

    Drawer drawer;
    Toolbar toolbar;
    Fragment current;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content view
        setContentView(R.layout.activity_main);

        // set toolbar as action bar
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // make drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new SecondaryDrawerItem()
                                .withIcon(R.drawable.ic_map_white_24dp)
                                .withName(R.string.main_menu_transit_map),
                        new SecondaryDrawerItem()
                                .withIcon(R.drawable.ic_directions_bus_white_24dp)
                                .withName(R.string.main_menu_routes)
                )
                .withTranslucentStatusBar(false)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                        switch(i) {
                            case 0:
                                setContentFragment(new MapFragment(), R.string.title_activity_map);
                                break;
                            case 1:
                                setContentFragment(new RoutesFragment(), R.string.title_activity_routelist);
                                break;
                        }
                        return false;
                    }
                })
                .build();

        setContentFragment(new MapFragment(), R.string.title_activity_map);
    }

    private void setContentFragment(Fragment content, int newActivityTitle) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_root, content);
        transaction.commit();
        current = content;

        toolbar.setTitle(newActivityTitle);
    }

}
