package us.v4lk.transrock.transloc;

import android.location.Location;

import java.util.LinkedHashMap;

import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.transloc.Stop;

/**
 * TransRock master cache
 */
public class Cache {

    // cache of agencies <agency_id, agency>
    private static LinkedHashMap<Integer, Agency> agencyCache = new LinkedHashMap<>();
    // cache of stops <code, stop>
    private static LinkedHashMap<String, Stop> stopCache = new LinkedHashMap<>();
    // cache of routes <agency_id, routes>
    private static LinkedHashMap<Integer, Route[]> routeCache = new LinkedHashMap<>();

    public static void cacheAgencies(Agency... agencies) {
        for (Agency a : agencies)
            agencyCache.put(a.agency_id, a);
    }
    public static void cacheStops(Stop... stops) {
        for (Stop s : stops)
            stopCache.put(s.code, s);
    }
    public static void cacheRoutes(int id, Route... routes) {
        //TODO: implement additive caching
        routeCache.put(id, routes);
    }

    public static LinkedHashMap<Integer, Agency> getAgencies(int... ids) {
        if (ids.length == 0)
            return agencyCache;

        LinkedHashMap<Integer, Agency> result = new LinkedHashMap<>(ids.length);
        for (int i : ids)
            result.put(i, agencyCache.get(i));

        return result;
    }
    public static LinkedHashMap<String, Stop> getStops(String... codes){
        if (codes.length == 0)
            return stopCache;

        LinkedHashMap<String, Stop> result = new LinkedHashMap<>(codes.length);
        for (String s : codes)
            result.put(s, stopCache.get(s));

        return result;
    }
    public static LinkedHashMap<Integer, Route[]> getRoutes(int... ids) {
        if (ids.length == 0)
            return routeCache;

        LinkedHashMap<Integer, Route[]> result = new LinkedHashMap<>(ids.length);
        for (int i : ids)
            result.put(i, routeCache.get(i));

        return result;
    }

}
