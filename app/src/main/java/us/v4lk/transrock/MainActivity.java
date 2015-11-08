package us.v4lk.transrock;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
    @Bind(R.id.fragment_pager) ViewPager pager;

    /** the nav drawer */
    Drawer drawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            Fragment mapFragment = new MapFragment();
            Fragment routeFragment = new RoutesFragment();

            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return mapFragment;
                    case 1:
                        return routeFragment;
                    default:
                        return mapFragment;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        pager.setAdapter(adapter);

        // build drawer
        drawer = buildDrawer(R.id.drawer_container);
    }
    @Override
    protected void onStop() {
        super.onStop();
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
                        pager.setCurrentItem(0);
                        break;
                    case 1:
                        pager.setCurrentItem(1);
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
