package us.v4lk.transrock.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.MapActivity;
import us.v4lk.transrock.R;
import us.v4lk.transrock.RouteListActivity;
import us.v4lk.transrock.transloc.Route;

/**
 * Miscellaneous static helper functions & global vars
 */
public class Util {

    public static final int GLOBAL_NETWORK_TIMEOUT = 3000;
    public static final String ROUTES_STORAGE_KEY = "routes";

    /**
     * Checks to see if we are connected to some form of network.
     * @param c context
     * @return whether or not this device is connected to a network
     */
    public static boolean isConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    /**
     * Checks to see if we can hit google. Accesses network; run in AsyncTask.
     * @param timeout timeout
     * @return whether google is reachable
     * @throws IllegalArgumentException if timeout < 0
     */
    public static boolean isOnline(int timeout) throws IllegalArgumentException {
        try {
            return InetAddress.getByName("http://www.google.com/").isReachable(timeout);
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    /**
     * Returns a list of the agencies that the specified routes belong to.
     * @return list of unique integer agency ids
     */
    public static int[] getAgencyIds(Route[] routes) {
        // get unique ids
        Set<Integer> ids = new HashSet<>();
        for (Route r : routes)
            ids.add(r.agency_id);

        // convert to int[]
        int[] result = new int[ids.size()];
        int i = 0;
        for (Integer id : ids)
            result[i++] = id;

        return result;
    }
    /**
     * Converts a color to a bitmap of given size.
     * @param color color int
     * @param width da width of da bitmap
     * @param height da height of da bitmap
     * @return da bitmap
     */

    public static Bitmap colorToBitmap(int color, int width, int height) {
        ColorDrawable cd = new ColorDrawable(color);
        cd.setBounds(0, 0, width, height);

        Bitmap colorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorBitmap);
        cd.setBounds(0, 0, width, height);
        cd.draw(canvas);

        return colorBitmap;
    }
    public static Drawer buildMainMenu(final Activity activity, Toolbar toolbar) {
        // add drawer
        Drawer drawer = new DrawerBuilder()
                .withActivity(activity)
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
                .build();

        drawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                Intent intent = null;
                switch(i) {
                    case 0:
                        intent = new Intent(activity, MapActivity.class);
                        activity.startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(activity, RouteListActivity.class);
                        break;
                }
                activity.startActivity(intent);

                return false;
            }
        });

        return drawer;
    }
}
