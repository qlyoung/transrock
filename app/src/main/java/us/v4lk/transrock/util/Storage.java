package us.v4lk.transrock.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;

/**
 * Provides interface for storing and retrieving items that need
 * to be persisted.
 */
public class Storage {

    /**
     * Commits the route and agency id's of the specified routes to preferences,
     * adding to any previous values
     * @param routes routes to add to preferences
     * @param c context
     */
    public static void appendRoutesToPrefs(Route[] routes, Context c) {
        // get preferences
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        // get existing entries
        Set<String> rs = prefs.getStringSet("routes", new HashSet<String>());

        // make new list of entries
        Set<String> routestrings = new HashSet<>();

        // copy old list to new list
        for (String s : rs)
                routestrings.add(s);

        // append any new items
        for (Route r : routes)
            routestrings.add(makeRouteString(r));

        // commit to preferences
        Editor e = prefs.edit();
        e.putStringSet("routes", routestrings);
        e.commit();
    }

    /**
     * Commits the route and agency id's of the specified routes to preferences,
     * overwriting to any previous values
     * @param routes routes to commit to preferences
     * @param c context
     */
    public static void commitRoutesToPrefs(Route[] routes, Context c) {
        // get preferences
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        // make new list of entries
        Set<String> routestrings = new HashSet<>();

        // append new items
        for (Route r : routes)
            routestrings.add(makeRouteString(r));

        // commit to preferences
        Editor e = prefs.edit();
        e.putStringSet("routes", routestrings);
        e.commit();
    }

    /**
     * Removes the route and agency id's of the specified routes from preferences
     * @param routes routes to remove
     * @param c context
     */
    public static void removeRoutesFromPrefs(Route[] routes, Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        // get existing lists
        Set<String> rs = prefs.getStringSet("routes", new HashSet<String>());

        // make new lists
        Set<String> routestrings = new HashSet<>();

        // copy old list items to new lists
        for (String s : rs)
            routestrings.add(s);

        // remove items
        for (Route r : routes) {
            routestrings.remove(makeRouteString(r));
        }

        // commit to preferences
        Editor e = prefs.edit();
        e.putStringSet("routes", routestrings);
        e.commit();
    }

    /**
     * Retrieves user's saved routes
     * @param c context
     * @return list of route id's
     */
    public static String[] retrieveSavedRoutes(Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        Set<String> rs = prefs.getStringSet("routes", new HashSet<String>());

        // convert strings to ints
        String[] routeIds = new String[rs.size()];
        int i = 0;
        for (String s : rs)
            routeIds[i++] = unmakeRouteString(s)[0];

        return routeIds;
    }

    /**
     * Retrieves id's of all agencies for which the user is tracking a route
     * @param c context
     * @return a list of agency id's
     */
    public static int[] retrieveSavedAgencies(Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        Set<String> as = prefs.getStringSet("routes", new HashSet<String>());

        // grab list of agencies
        ArrayList<Integer> ids = new ArrayList<>();
        for (String s : as) {
            String[] s2 = unmakeRouteString(s);
            int id = Integer.valueOf(s2[1]);
            // don't add duplicates
            if (!ids.contains(id))
                ids.add(id);
        }

        Integer[] intermediate = ids.toArray(new Integer[0]);
        int[] result = new int[intermediate.length];
        int i = 0;
        for (Integer id : intermediate)
            result[i++] = id;

        return result;
    }

    public static boolean contains(Route r, Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        Set<String> rs = prefs.getStringSet("routes", new HashSet<String>());

        return rs.contains(makeRouteString(r));
    }

    public static boolean contains(Agency a, Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        Set<String> rs = prefs.getStringSet("routes", new HashSet<String>());
        for (String s : rs)
            if (unmakeRouteString(s)[1].equals(String.valueOf(a.agency_id))) return true;

        return false;
    }

    private static String makeRouteString(Route r) {
        return r.route_id + "|" + String.valueOf(r.agency_id);
    }
    private static String[] unmakeRouteString(String s) {
        return s.split("\\|");
    }
}
