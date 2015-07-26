package us.v4lk.transrock.transloc;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

/**
 * Encapsulates the TransLoc API.
 * TODO: implement the rest of the API object classes and call functions
 */
public class TransLocAPI {

    /**
     * Get specified agencies. If no agency id's are specified,
     * all agencies will be returned.
     *
     * @param ids agency ids to retrieve
     * @return the specified agencies, or all if none were specified
     */
    public static Agency[] getAgencies(int... ids) {
        // if cache size == 0, cache all agencies
        if (Cache.getAgencies().size() == 0) {
            JSONObject response = callApi("/agencies.json");

            try {
                JSONArray data = response.getJSONArray("data");
                ArrayList<Agency> agencies = new ArrayList<>();
                for (int i = 0; i < data.length(); i++)
                    agencies.add(new Agency(data.getJSONObject(i)));

                Cache.cacheAgencies(agencies.toArray(new Agency[agencies.size()]));

            } catch (Exception e) {
                Log.e("TransRock", e.getMessage());
            }
        }

        /* since the cache holds either all or no agencies, we can assume the cache hit rate
         * will be 100% and just default to cached values */
        LinkedHashMap<Integer, Agency> cachedAgencies = Cache.getAgencies(ids);

        if (ids.length == 0)
            return cachedAgencies.values().toArray(new Agency[cachedAgencies.size()]);
        else {
            Agency[] result  = new Agency[ids.length];
            for (int i = 0; i < ids.length; i++)
                result[i] = cachedAgencies.get(ids[i]);
            return result;
        }

    }

    /**
     * Get agencies within the defined geoarea. Point-radius form.
     *
     * @param center center of the circle
     * @param radius radius of circle in meters
     * @return
     */
    public static Agency[] getAgencies(Location center, float radius) {

        Agency[] agencies = getAgencies();
        ArrayList<Agency> agenciesInArea = new ArrayList<>();

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
     *
     * @param id id of the agency to retrieve routes for
     * @return array of routes for the agency
     */
    public static Route[] getRoutes(int id) {
        // encapsulate the single param in an array and call the more general overload
        LinkedHashMap<Integer, Route[]> res = getRoutes(new int[] {id});
        return res.get(id);
    }
    /**
     * Get the routes for a given agency.
     *
     * @param ids id's of agencies to retrieve routes for
     * @return linkedhashmap with <agency, route[]>
     */
    public static LinkedHashMap<Integer, Route[]> getRoutes(int... ids) {
        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder.append("/routes.json?agencies=");
        for (int i : ids)
            builder.append(i).append(',');
        builder.deleteCharAt(builder.length() - 1); // delete trailing comma
        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);

        LinkedHashMap<Integer, Route[]> result = new LinkedHashMap<>(ids.length);

        try {
            // get response datablock
            JSONObject data = response.getJSONObject("data");

            // extract routes for each agency block
            for (int i : ids) {
                JSONArray routelist = data.getJSONArray(String.valueOf(i));

                Route[] routes = new Route[routelist.length()];
                for (int j = 0; j < routelist.length(); j++)
                    routes[j] = new Route(routelist.getJSONObject(j));

                result.put(i, routes);
            }

        } catch (JSONException e) {
            Log.d("TransRock", e.getMessage());
        }

        return result;
    }
    /**
     * Get a list of stops served by a given agency.
     * @param ids The id's of the agencies whose stops to get
     * @return An array of Stops.
     */
    public static Stop[] getStops(int... ids) {
        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder.append("/stops.json?agencies=");
        for (int i : ids)
            builder.append(i + ",");
        builder.deleteCharAt(builder.length() - 1); // delete trailing comma
        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);

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
     * Calls the api endpoint with the specified path.
     *
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

}
