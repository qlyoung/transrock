package us.v4lk.transrock.mapping;

import android.content.Context;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Various convenience methods for working with polylines
 */
public class Polylines {

    /** default precision for polylines */
    public static final double DEFAULT_POLYLINE_PRECISION = 1e5;
    /**
     * Polyline decoder.
     * https://github.com/scoutant/polyline-decoder/blob/master/src/main/java/org/scoutant/polyline/PolylineDecoder.java
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
     * @param encodedPolyline encoded polyline string
     * @param context the context
     * @return a Polyline overlay
     */
    public static Polyline encodedPolylineToOverlay(String encodedPolyline, Context context) {
        // decode encoded polyline to list of geopoints
        ArrayList<GeoPoint> segmentGeoPoints = Polylines.decodePolyline(encodedPolyline, Polylines.DEFAULT_POLYLINE_PRECISION);
        // convert geopoints to a Polyline overlay
        Polyline segmentPolyline = pointsToOverlay(segmentGeoPoints, context);
        // return polyline
        return segmentPolyline;
    }

}
