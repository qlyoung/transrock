package us.v4lk.transrock;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import us.v4lk.transrock.fragments.MapFragment;
import us.v4lk.transrock.fragments.RoutesFragment;

/**
 * Main activity. Switches between content fragments using a nav drawer.
 */
public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_toolbar) Toolbar toolbar;
    /** the current fragment being displayed */
    Fragment current;
    /** mapFragment fragment */
    Fragment mapFragment;
    /** route fragment */
    Fragment routeFragment;
    /** the nav drawer */
    Drawer drawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mapFragment = new MapFragment();
        routeFragment = new RoutesFragment();

        // build drawer
        drawer = buildDrawer(R.id.drawer_container);

        // add mapFragment fragment
        FragmentTransaction tx = getFragmentManager().beginTransaction();
        tx.add(R.id.frame_container, mapFragment).commit();
        current = mapFragment;
        toolbar.setTitle(R.string.title_activity_map);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Convenience method that switches the current fragment for the given fragment.
     *
     * @param fragment  the fragment to switch to
     * @param title     the new title of the activity in the toolbar
     */
    private void setContentFragment(Fragment fragment, int title) {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        toolbar.setTitle(title);
    }

    private Drawer buildDrawer(int rootView) {
        // make drawer builder
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withRootView(rootView)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withHeader(R.layout.drawer_header)
                .withTranslucentStatusBar(false);

        // make drawer items
        SecondaryDrawerItem mapItem = new SecondaryDrawerItem();
        mapItem.withIcon(R.drawable.ic_map_white_24dp);
        mapItem.withName(R.string.main_menu_transit_map);
        SecondaryDrawerItem routesItem = new SecondaryDrawerItem();
        routesItem.withIcon(R.drawable.ic_directions_bus_white_24dp);
        routesItem.withName(R.string.main_menu_routes);

        // make item click listener
        Drawer.OnDrawerItemClickListener listener = new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                switch (i) {
                    case 0:
                        setContentFragment(mapFragment, R.string.title_activity_map);
                        break;
                    case 1:
                        setContentFragment(routeFragment, R.string.title_activity_routelist);
                        break;
                }
                return false;
            }
        };

        // add items and listener to builder
        builder.addDrawerItems(mapItem, routesItem);
        builder.withOnDrawerItemClickListener(listener);

        return builder.build();
    }
}
