package us.v4lk.transrock.util;

import android.os.AsyncTask;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for Hawk that offers a convenient way to access routes in storage.
 * <p/>
 * Hawk is backed by Android's SQLite interface which requires synchronous access. Hawk
 * doesn't make this explicit, so I've done so in this wrapper.
 */
public class RouteManager {

    private static final String ROUTES_STORAGE_KEY = "routes";

    private static Map<String, TransrockRoute> routes;

    public static synchronized Map<String, TransrockRoute> getMap() {
        return routes;
    }

    public static synchronized Collection<TransrockRoute> getAllRoutes() {
        return getMap().values();
    }

    public static synchronized TransrockRoute[] putRoute(Collection<TransrockRoute> routes) {
        TransrockRoute[] previous = new TransrockRoute[routes.size()];

        Map<String, TransrockRoute> map = getMap();
        int i = 0;
        for (TransrockRoute route : routes)
            previous[i++] = map.put(route.route_id, route);

        new PutRoutesTask().execute(map);

        return previous;
    }

    public static synchronized Collection<TransrockRoute> getActivatedRoutes() {
        Collection<TransrockRoute> routes = getAllRoutes();
        ArrayList<TransrockRoute> actives = new ArrayList<>();
        for (TransrockRoute route : routes)
            if (route.isActivated())
                actives.add(route);

        return actives;
    }

    public static synchronized TransrockRoute[] getRoutesByAgency(int agencyId) {
        ArrayList<TransrockRoute> result = new ArrayList<>();
        for (TransrockRoute route : getMap().values()) {
            if (route.agency_id == agencyId) result.add(route);
        }
        return result.toArray(new TransrockRoute[result.size()]);
    }

    public static synchronized void clear() {
        Hawk.remove(ROUTES_STORAGE_KEY);
        initialize();
    }

    /**
     * initialize empty storage if it does not already exist
     */
    public static synchronized void initialize() {
        if (Hawk.get(ROUTES_STORAGE_KEY) == null)
            Hawk.put(ROUTES_STORAGE_KEY, new HashMap<String, TransrockRoute>());

        routes = Hawk.get(ROUTES_STORAGE_KEY);
    }

    static class PutRoutesTask extends AsyncTask<Map<String, TransrockRoute>, Void, Void> {
        @Override
        protected Void doInBackground(Map<String, TransrockRoute>[] params) {
            Map<String, TransrockRoute> map = params[0];
            Hawk.put(ROUTES_STORAGE_KEY, map);
            return null;
        }
    }

}
