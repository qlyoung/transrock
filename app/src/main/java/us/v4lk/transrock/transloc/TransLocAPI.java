package us.v4lk.transrock.transloc;

import android.accounts.NetworkErrorException;
import android.location.Location;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import us.v4lk.transrock.transloc.objects.Agency;
import us.v4lk.transrock.transloc.objects.Route;
import us.v4lk.transrock.transloc.objects.Segment;
import us.v4lk.transrock.transloc.objects.Stop;
import us.v4lk.transrock.transloc.objects.Vehicle;
import us.v4lk.transrock.util.Util;

/**
 * Encapsulates the TransLoc API. Implements transparent caching.
 * TODO: implement the rest of the API object classes and call functions
 */
public class TransLocAPI {

    /**  TransLocAPI memcache */
    static class Cache {
        /** Structures cache data for serialization onto disk */
        private static final String API_AGENCY_CACHE_KEY = "API_CACHE_AGENCY",
                                    API_STOP_CACHE_KEY = "API_CACHE_STOP",
                                    API_ROUTE_CACHE_KEY = "API_CACHE_ROUTE",
                                    API_SEGMENT_CACHE_KEY = "API_CACHE_SEGMENT";

        private static Map<Integer, Agency> agencyCache;
        private static Map<String, Stop> stopCache;
        private static Map<String, Route> routeCache;
        private static Map<String, Segment> segmentCache;

        private static boolean initialized = false;

        /**
         * Caches agencies, overwriting any previous entries for the same data.
         * @param agencies agencies to cache
         */
        public static void cacheAgencies(Agency... agencies) {
            for (Agency agency : agencies)
                agencyCache.put(agency.agency_id, agency);
        }
        /**
         * Caches stops, overwriting any previous entries for the same data.
         * @param stops stops to cache
         */
        public static void cacheStops(Stop... stops) {
            for (Stop stop : stops)
                stopCache.put(stop.stop_id, stop);
        }
        /**
         * Caches routes, overwriting any previous entries for the same data.
         * @param routes routes to cache
         */
        public static void cacheRoutes(Route... routes) {
            for (Route route : routes)
                routeCache.put(route.route_id, route);
        }
        /**
         * Caches segments, overwriting any previous entries for the same data.
         * @param segments segments to cache
         */
        public static void cacheSegments(Segment... segments) {
            for (Segment segment : segments)
                segmentCache.put(segment.segmentId, segment);
        }

        /**
         * Gets the agency cache. Please don't write to it!
         * @return agency cache
         */
        public static Map<Integer, Agency> getAgencyCache() {
            return agencyCache;
        }
        /**
         * Gets the stop cache. Please don't write to it!
         * @return stop cache
         */
        public static Map<String, Stop> getStopCache() {
            return stopCache;
        }
        /**
         * Gets the route cache. Please don't write to it!
         * @return route cache
         */
        public static Map<String, Route> getRouteCache() {
            return routeCache;
        }
        /**
         * Gets the segment cache. Please don't write to it!
         * @return segment cache
         */
        public static Map<String, Segment> getSegmentCache() {
            return segmentCache;
        }

        /** loads on-disk cache to mem */
        public static void initialize() {
            if (!initialized) {
                agencyCache = Hawk.get(API_AGENCY_CACHE_KEY, new LinkedHashMap<Integer, Agency>());
                stopCache = Hawk.get(API_STOP_CACHE_KEY, new LinkedHashMap<String, Stop>());
                routeCache = Hawk.get(API_ROUTE_CACHE_KEY, new LinkedHashMap<String, Route>());
                segmentCache = Hawk.get(API_SEGMENT_CACHE_KEY, new LinkedHashMap<String, Segment>());
            }
            initialized = true;
        }
        /** commits in-mem cache to disk */
        public static void commit() {
            Hawk.chain()
                    .put(API_AGENCY_CACHE_KEY, agencyCache)
                    .put(API_STOP_CACHE_KEY, stopCache)
                    .put(API_ROUTE_CACHE_KEY, routeCache)
                    .put(API_SEGMENT_CACHE_KEY, segmentCache)
                    .commit();
        }
    }

    private static final String
            AGENCY_PATH = "/agencies.json",
            ROUTE_PATH = "/routes.json",
            STOP_PATH = "/stops.json",
            SEGMENT_PATH = "/segments.json",
            VEHICLE_PATH = "/vehicles.json";
    private static final String DATA = "data";

    /* API call methods */
    /**
     * Calls the api endpoint with the specified path.
     * @param relativePath the path to call the server with.
     * @return A JSONObject with the response. Includes API metadata.
     */
    private static JSONObject callApi(String relativePath)
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        JSONObject response;
        try {
            // setup the request
            URL url = new URL("https://transloc-api-1-2.p.mashape.com" + relativePath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("X-Mashape-Key", "HVy1utpe5Smsh8QVRRAES2GQu4pdp1Qx9gYjsnAoiFVQ0DZcXz");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(Util.GLOBAL_NETWORK_TIMEOUT);

            // make the request and convert the response to a JSONObject
            conn.connect();
            InputStream response_body = conn.getInputStream();
            String responseBody = (new java.util.Scanner(response_body).useDelimiter("\\A")).next();
            response = new JSONObject(responseBody);

        } catch (IOException e) { throw new NetworkErrorException("Unknown issue with network."); }

        return response;
    }

    /**
     * Get specified agencies. If no agency id's are specified, all agencies will be returned.
     * This method will attempt to read agencies from local cache. If the cache is empty, it
     * will sidetrack and cache all agencies. Therefore this method should be called on
     * application startup to accelerate performance later on.
     * @param agencyIds agency ids to retrieve
     * @return the specified agencies, or all if none were specified
     */
    public static Agency[] getAgencies(int... agencyIds)
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        // if cache size == 0, cache all agencies
        if (Cache.getAgencyCache().size() == 0) {
            JSONObject response = callApi(AGENCY_PATH);

            JSONArray data = response.getJSONArray(DATA);
            ArrayList<Agency> agencies = new ArrayList<>();
            for (int i = 0; i < data.length(); i++)
                agencies.add(new Agency(data.getJSONObject(i)));

            Cache.cacheAgencies(agencies.toArray(new Agency[agencies.size()]));
        }

        // since the cache holds either all or no agencies, we can assume the cache hit rate
        // will be 100% and just default to cached values
        // TODO: scary assumption, fix this
        Map<Integer, Agency> cachedAgencies = Cache.getAgencyCache();

        if (agencyIds.length == 0)
            return cachedAgencies.values().toArray(new Agency[cachedAgencies.size()]);
        else {
            Agency[] result  = new Agency[agencyIds.length];
            for (int i = 0; i < agencyIds.length; i++)
                result[i] = cachedAgencies.get(agencyIds[i]);
            return result;
        }
    }
    /**
     * Get agencies within the defined geoarea. Point-radius form. TransLoc
     * has an API parameter for this but since we've got the agencies locally
     * it's faster to do the measurement ourselves.
     * @param center center of the circle
     * @param radius radius of circle in meters
     * @return all agencies in the specified geoarea
     */
    public static Agency[] getAgencies(Location center, float radius)
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        // get all agencies
        Agency[] agencies = getAgencies();
        ArrayList<Agency> agenciesInArea = new ArrayList<>();

        // for each agency measure whether distance from location to agency is < radius
        for (Agency a : agencies) {
            double agencyLat = a.position.get(0);
            double agencyLong = a.position.get(1);
            float[] result = new float[1];
            Location.distanceBetween(center.getLatitude(),
                                     center.getLongitude(),
                                     agencyLat,
                                     agencyLong, result);

            float distanceMeters = result[0];
            if (distanceMeters < radius)
                agenciesInArea.add(a);
        }

        Agency[] result = new Agency[agenciesInArea.size()];
        agenciesInArea.toArray(result);

        return result;
    }
    /**
     * Get the routes for the given agency.
     * TODO: implement caching
     * @param agencyId id of the agency to retrieve routes for
     * @return array of routes for the agency
     */
    public static Route[] getRoutes(int agencyId)
        throws NetworkErrorException, SocketTimeoutException, JSONException {
        // encapsulate the single param in an array and call the more general overload
        Map<Integer, Route[]> res = getRoutes(new int[]{agencyId});
        return res.get(agencyId);
    }
    /**
     * Get the routes for a given agency.
     * * TODO: implement caching
     * @param agencyIds id's of agencies to retrieve routes for
     * @return hash map with agency ids as keys and corresponding route arrays as values
     */
    public static Map<Integer, Route[]> getRoutes(int... agencyIds)
        throws NetworkErrorException, SocketTimeoutException, JSONException {

        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder
                .append(ROUTE_PATH)
                .append("?agencies=");
        for (int i : agencyIds)
            builder.append(i).append(',');
        builder.deleteCharAt(builder.length() - 1); // delete trailing comma
        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);

        LinkedHashMap<Integer, Route[]> result = new LinkedHashMap<>(agencyIds.length);

        try {
            // get response datablock
            JSONObject data = response.getJSONObject(DATA);

            // extract routes for each agency block
            for (int i : agencyIds) {
                JSONArray routelist = data.getJSONArray(String.valueOf(i));

                Route[] routes = new Route[routelist.length()];
                for (int j = 0; j < routelist.length(); j++) {
                    routes[j] = new Route(routelist.getJSONObject(j));
                }

                result.put(i, routes);
            }

        } catch (JSONException e) {
            Log.d("TransRock", e.getMessage());
        }

        return result;
    }
    /**
     * Returns a list of encoded polylines representing the segments for the given route
     * along with their segment id's.
     * TODO: implement caching
     * @param r route to fetch segments for
     * @return list of encoded polylines keyed by id
     */
    public static Map<String, String> getSegments(Route r)
            throws NetworkErrorException, SocketTimeoutException, JSONException {
        return getSegments(r.agency_id, r.route_id);
    }
    /**
     * Returns a list of encoded polylines representing the segments for the given route
     * along with their segment id's.
     * TODO: implement caching
     * @param routeId id of route to fetch segments for
     * @param agencyId id of agency the route belongs to
     * @return list of encoded polylines keyed by id
     */
    public static Map<String, String> getSegments(int agencyId, String routeId)
            throws NetworkErrorException, SocketTimeoutException, JSONException {
        StringBuilder builder = new StringBuilder();
        builder
                .append(SEGMENT_PATH)
                .append("?")
                .append("agencies=")
                .append(agencyId)
                .append("&")
                .append("routes=")
                .append(routeId);
        String request = builder.toString();

        JSONObject response = callApi(request);

        JSONObject data = response.getJSONObject(DATA);

        LinkedHashMap<String, String> segments = new LinkedHashMap<>();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            segments.put(key, data.getString(key));
        }

        return segments;
    }
    /**
     * Get a list of stops served by a given agency.
     * TODO: implement caching
     * @param agencyIds The id's of the agencies whose stops to get
     * @return An array of Stops.
     */
    public static Map<String, Stop> getStops(int... agencyIds)
        throws NetworkErrorException, SocketTimeoutException, JSONException {
        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder
                .append(STOP_PATH)
                .append("?")
                .append("agencies=");
        for (int i : agencyIds)
            builder.append(i).append(",");
        builder.deleteCharAt(builder.length() - 1); // delete trailing comma
        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);

        // build map
        Map<String, Stop> stops = new LinkedHashMap<>();
        JSONArray data = response.getJSONArray(DATA);
        for (int i = 0; i < data.length(); i++) {
            Stop s = new Stop(data.getJSONObject(i));
            stops.put(s.stop_id, s);
        }

        return stops;
    }
    /**
     * Gets latest vehicles for the given agency and routes
     * @param agencyId agency
     * @param routeId route ids to retrieve vehicles for; should be limited to agency specified
     * @return list of vehicles currently running on the given route. If there are none currently
     *         running, an empty list is returned.
     */
    public static List<Vehicle> getVehicles(int agencyId, String routeId)
            throws NetworkErrorException, SocketTimeoutException, JSONException {
        StringBuilder builder = new StringBuilder();
        builder
                .append(VEHICLE_PATH)
                .append("?")
                .append("agencies=").append(agencyId)
                .append("&")
                .append("routes=")
                .append(routeId);

        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);

        //Currently this throws an exception; it should return a list of size 0.
        ArrayList<Vehicle> result = new ArrayList<>();
        JSONArray vehicleArray;

        try {
            // get array of vehicles organized by route & agency if it exists
            vehicleArray = response.getJSONObject(DATA).getJSONArray(String.valueOf(agencyId));
        } catch (JSONException e) {
            // if there is no entry corresponding to the agency id, then the agency has no vehicles
            // running; return empty list
            return new ArrayList<>();
        }
        // otherwise pull out the vehicles and put them in an array
        for (int i = 0; i < vehicleArray.length(); i++)
            result.add(new Vehicle(vehicleArray.getJSONObject(i)));

        return result;
    }

    /* caching and init methods */
    /**
     * Initializes API object. Loads cache from disk to memory.
     */
    public static void initialize() {
        Cache.initialize();
    }
    /**
     * Commits memory cache to disk. May take a second or two; consider
     * calling in an AsyncTask.
     */
    public static void commitCache() {
        Cache.commit();
    }
}