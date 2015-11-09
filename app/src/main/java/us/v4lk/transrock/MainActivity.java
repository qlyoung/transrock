package us.v4lk.transrock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnPageChange;
import us.v4lk.transrock.fragments.MapFragment;
import us.v4lk.transrock.fragments.RoutesFragment;
import us.v4lk.transrock.util.SmartViewPager;

/**
 * Main activity. Switches between content fragments using a nav drawer.
 */
public class MainActivity extends AppCompatActivity {

    @Bind(R.id.main_toolbar) Toolbar toolbar;
    @Bind(R.id.fragment_pager) SmartViewPager pager;
    @OnPageChange(R.id.fragment_pager) void onPageSelected(int position) {
        switch (position) {
            case SmartViewPager.MAP_PAGE:
                pager.setAllowSwiping(false);
                drawer.setSelection(position);
                break;
            case SmartViewPager.ROUTE_PAGE:
                pager.setAllowSwiping(true);
                drawer.setSelection(position);
                break;
        }
    }

    /** fragments this activity hosts */
    MapFragment mapFragment;
    RoutesFragment routeFragment;

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

        // setup ViewPager's fragment adapter
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
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
        pager.addOnPageChangeListener(routeFragment);
        pager.addOnPageChangeListener(mapFragment);

        // build drawer
        drawer = buildDrawer(R.id.drawer_container);
        // set initial page and disable swiping for initial use
        pager.setCurrentItem(SmartViewPager.MAP_PAGE);
        pager.setAllowSwiping(false);

    }

    private Drawer buildDrawer(int rootView) {
        // make drawer builder
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withRootView(rootView)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
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
                        pager.setCurrentItem(SmartViewPager.MAP_PAGE);
                        break;
                    case 1:
                        pager.setCurrentItem(SmartViewPager.ROUTE_PAGE);
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
