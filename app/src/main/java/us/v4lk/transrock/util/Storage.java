package us.v4lk.transrock.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.transloc.Route;

/**
 * Provides interface for storing and retrieving items that need
 * to be persisted.
 */
public class Storage {

    /**
     * Commits the routes specified to preferences, adding to any previous values
     * @param routes routes to commit to preferences
     * @param c context
     */
    public static void commitRoutesToPrefs(Route[] routes, Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);

        // get existing lists
        Set<String> as = prefs.getStringSet("agencies", new HashSet<String>());
        Set<String> rs = prefs.getStringSet("routes", new HashSet<String>());

        // make new lists
        Set<String> agencystrings = new HashSet<>();
        Set<String> routestrings = new HashSet<>();

        // populate new lists with existing list items
        for (String s : as)
                agencystrings.add(s);
        for (String s : rs)
                routestrings.add(s);

        // append any new agencies and ids
        for (Route r : routes)
            agencystrings.add(String.valueOf(r.agency_id));
        for (Route r : routes)
            routestrings.add(r.route_id);

        // commit to preferences
        Editor e = prefs.edit();
        e.putStringSet("agencies", agencystrings);
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
        String[] routeIds = rs.toArray(new String[rs.size()]);

        return routeIds;
    }

    /**
     * Retrieves id's of all agencies for which the user is tracking a route
     * @param c context
     * @return a list of agency id's
     */
    public static int[] retrieveSavedAgencies(Context c) {
        SharedPreferences prefs = c.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        Set<String> as = prefs.getStringSet("agencies", new HashSet<String>());

        int[] agencyIds = new int[as.size()];
        int i = 0;
        for (String s : as) {
            agencyIds[i] = Integer.valueOf(s);
            i++;
        }

        return agencyIds;
    }



}
