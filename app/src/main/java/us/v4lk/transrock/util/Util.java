package us.v4lk.transrock.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.orhanobut.hawk.Hawk;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

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

}
