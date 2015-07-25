package us.v4lk.transrock.transloc;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Encapsulates the TransLoc API.
 * TODO: implement the rest of the API object classes and call functions
 */
public class TransLocAPI {

    // cache of agencies
    private static LinkedHashMap<Integer, Agency> agencyCache;
    // cache of stops
    private static LinkedHashMap<Integer, Stop[]> stopCache;
    // cache of routes
    private static LinkedHashMap<Integer, Route[]> routeCache;

    /**
     * Get the routes for a given agency.
     * @param agencyid The agency id.
     * @return An array of routes.
     */
    public static Route[] getRoutes(int agencyid) {
        JSONObject response = callApi("/routes.json?agencies=" + agencyid);

        // parse that shit
        Route[] routes = null;
        try {
            JSONArray routeList = response.getJSONObject("data").getJSONArray(String.valueOf(agencyid));
            routes = new Route[routeList.length()];
            for (int i = 0; i < routeList.length(); i++)
                routes[i] = new Route(routeList.getJSONObject(i));
        } catch (JSONException e) {
            Log.d("TransRock", e.getMessage());
        }

        return routes;
    }
    /**
     * Get a list of stops served by a given agency.
     * @param agencyid The id of the agency.
     * @return An array of Stops.
     */
    public static Stop[] getStops(int agencyid) {
        JSONObject response = callApi("/stops.json?agencies=" + agencyid);

        // parse that shit
        Stop[] stops = null;
        try {
            JSONArray data = response.getJSONArray("data");
            stops = new Stop[data.length()];
            for (int i = 0; i < data.length(); i++)
                stops[i] = new Stop(data.getJSONObject(i));

        } catch (Exception e) { Log.e("TransRock", e.getMessage()); }

        return stops;
    }
    /**
     * Get specified agencies. If no agency id's are specified,
     * all agencies will be returned.
     * @param ids agency ids to retrieve
     */
    public static Agency[] getAgencies(int... ids) {
        if (ids.length == 0) // return all agencies
            return agencyCache.values().toArray(new Agency[agencyCache.size()]);
        else {
            Agency[] result = new Agency[ids.length];
            for (int i = 0; i < ids.length; i++)
                result[i] = agencyCache.get(i);
            return result;
        }
    }
    /**
     * Calls the api endpoint with the specified path.
     * @param relativePath the path to call the server with.
     * @return A JSONObject with the response. Includes API metadata.
     */
    private static JSONObject callApi(String relativePath) {
        // Fuck all the JSON parsers and shitty REST libraries, they can go fuck themselves.
        // Fuck writing shit introspecting dynamic generic converter adapter classes for fucked
        // up stupid JSON to object parses. I'll just parse this shit myself if you're going
        // to make me trudge through your shitty docs.

        JSONObject response = null;
        try {
            // setup the request
            URL url = new URL("https://transloc-api-1-2.p.mashape.com" + relativePath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("X-Mashape-Key", "HVy1utpe5Smsh8QVRRAES2GQu4pdp1Qx9gYjsnAoiFVQ0DZcXz");
            conn.setRequestProperty("Accept", "application/json");

            // make the request and convert the response to a JSONObject
            conn.connect();
            InputStream response_body = conn.getInputStream();
            String responseBody = (new java.util.Scanner(response_body).useDelimiter("\\A")).next();
            response = new JSONObject(responseBody);
        } catch (Exception e) {  Log.e("TransRock", e.getMessage()); }

        return response;
    }
    /**
     * Pulls and caches all agencies. The cache is always used
     * to get agency data. This should be acceptable since it
     * is rather unlikely that TransLoc, Inc. will gain a new
     * client between API calls.
     */
    public static void initialize() {
        JSONObject response = callApi("/agencies.json");

        try {
            JSONArray data = response.getJSONArray("data");
            agencyCache = new LinkedHashMap<>(data.length());
            for (int i = 0; i < data.length(); i++) {
                Agency a = new Agency(data.getJSONObject(i));
                agencyCache.put(a.agency_id, a);
            }

        } catch (Exception e) {
            Log.e("TransRock", e.getMessage());
        }
        Log.d("TransRock", "faux breakpoint");
    }
}
