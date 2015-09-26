package us.v4lk.transrock.util;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface to saved routes
 */
public class RouteStorage {

    private static final String ROUTES_STORAGE_KEY = "routes";

    public static Map<String, TransrockRoute> getMap() {
        return Hawk.get(ROUTES_STORAGE_KEY);
    }
    public static Collection<TransrockRoute> getAllRoutes() {
        return getMap().values();
    }

    public static TransrockRoute getRoute(String id) {
        return getMap().get(id);
    }

    public static TransrockRoute putRoute(TransrockRoute route) {
        Map<String, TransrockRoute> map = getMap();
        TransrockRoute previous = map.put(route.route_id, route);
        Hawk.put(ROUTES_STORAGE_KEY, map);

        return previous;
    }
    public static TransrockRoute[] putRoute(Collection<TransrockRoute> routes) {
        TransrockRoute[] previous = new TransrockRoute[routes.size()];

        Map<String, TransrockRoute> map = getMap();
        int i = 0;
        for (TransrockRoute route : routes)
            previous[i++] = map.put(route.route_id, route);
        Hawk.put(ROUTES_STORAGE_KEY, map);

        return previous;
    }

    public static TransrockRoute removeRoute(String id) {
        Map<String, TransrockRoute> map = getMap();
        TransrockRoute removed = map.remove(id);
        Hawk.put(ROUTES_STORAGE_KEY, map);

        return removed;
    }
    public static TransrockRoute removeRoute(TransrockRoute route) {
        return removeRoute(route.route_id);
    }
    public static TransrockRoute[] removeRoute(Collection<TransrockRoute> routes) {
        TransrockRoute[] removed = new TransrockRoute[routes.size()];

        Map<String, TransrockRoute> map = getMap();
        int i = 0;
        for (TransrockRoute route : routes)
            removed[i++] = map.remove(route.route_id);
        Hawk.put(ROUTES_STORAGE_KEY, map);

        return removed;
    }

    public static Collection<TransrockRoute> getActiveRoutes() {
        Collection<TransrockRoute> routes = getAllRoutes();
        ArrayList<TransrockRoute> actives = new ArrayList<>();
        for (TransrockRoute route : routes)
            if (route.isActive())
                actives.add(route);

        return actives;
    }

    public static boolean contains(TransrockRoute route) {
        return contains(route.route_id);
    }
    public static boolean contains(String id) {
        return getMap().containsKey(id);
    }

    /**
     * initialize empty storage if it does not already exist
     */
    public static void initialize() {
        if (Hawk.get(ROUTES_STORAGE_KEY) == null)
            Hawk.put(ROUTES_STORAGE_KEY, new HashMap<String, TransrockRoute>());
    }

}
