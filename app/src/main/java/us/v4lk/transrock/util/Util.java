package us.v4lk.transrock.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Miscellaneous static helper functions
 */
public class Util {

    public static final int GLOBAL_NETWORK_TIMEOUT = 3000;

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

}
