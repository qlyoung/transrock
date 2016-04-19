package us.v4lk.transrock.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Miscellaneous static helper functions & global vars
 */
public class Util {

    /**
     * Global network timeout.
     */
    public static final int GLOBAL_NETWORK_TIMEOUT = 3000;

    /**
     * Checks to see if we are connected to some form of network.
     *
     * @param c context
     * @return whether or not this device is connected to a network
     */
    public static boolean isConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Checks to see if we can hit google. Accesses network; run in AsyncTask.
     *
     * @param timeout timeout
     * @return whether google is reachable
     * @throws IllegalArgumentException if timeout < 0
     */
    public static boolean isOnline(int timeout) throws IllegalArgumentException {
        try {
            return InetAddress.getByName("8.8.8.8").isReachable(timeout);
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Converts a color to a bitmap of given size.
     *
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

    /**
     * default precision for polylines
     */
    public static final double DEFAULT_POLYLINE_PRECISION = 1e5;

    /**
     * Polyline decoder.
     * https://github.com/scoutant/polyline-decoder/blob/master/src/main/java/org/scoutant/polyline/PolylineDecoder.java
     *
     * @param encoded the encoded polyline string
     * @param precision precision to decode to (should be ~ 1e5 or 1e6)
     * @return a list of geopoints corresponding to the encoded polyline
     */
    public static ArrayList<GeoPoint> decodePolyline(String encoded, double precision) {
        ArrayList<GeoPoint> track = new ArrayList<>();
        int index = 0;
        int lat = 0, lng = 0;

        while (index < encoded.length()) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            GeoPoint point = new GeoPoint((double) lat / precision, (double) lng / precision);
            track.add(point);
        }

        return track;
    }

    /**
     * Converts list of GeoPoints to a single Polyline overlay.
     *
     * @param waypoints the points defining the line
     * @param context the context
     * @return a Polyline overlay
     */
    public static Polyline pointsToOverlay(ArrayList<GeoPoint> waypoints, Context context) {
        Road road = new Road(waypoints);
        road.buildLegs(waypoints);
        Polyline overlay = RoadManager.buildRoadOverlay(road, context);
        return overlay;
    }

    /**
     * Converts an encoded polyline string to a Polyline overlay.
     *
     * @param encodedPolyline encoded polyline string
     * @param context the context
     * @return a Polyline overlay
     */
    public static Polyline encodedPolylineToOverlay(String encodedPolyline, Context context) {
        // decode encoded polyline to list of geopoints
        ArrayList<GeoPoint> segmentGeoPoints = decodePolyline(encodedPolyline, DEFAULT_POLYLINE_PRECISION);
        // convert geopoints to a Polyline overlay
        Polyline segmentPolyline = pointsToOverlay(segmentGeoPoints, context);
        // return polyline
        return segmentPolyline;
    }

    /** Converts Google Location object to GeoPoint */
    public static GeoPoint toGeoPoint(Location l) {
        return new GeoPoint(l.getLatitude(), l.getLongitude());
    }

}
