package us.v4lk.transrock.transloc;

import android.accounts.NetworkErrorException;
import android.location.Location;

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

import us.v4lk.transrock.model.Agency;
import us.v4lk.transrock.model.Route;
import us.v4lk.transrock.model.Segment;
import us.v4lk.transrock.model.Stop;
import us.v4lk.transrock.model.Vehicle;
import us.v4lk.transrock.util.Util;

/**
 * Encapsulates the TransLoc API. Implements transparent caching.
 * TODO: implement the rest of the API object classes and call functions
 */
public class TransLocAPI {

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
     *
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

        } catch (IOException e) {
            throw new NetworkErrorException("Unknown issue with network.");
        }

        return response;
    }

    /**
     * Get all agencies.
     *
     * @return All agencies known to TransLoc API.
     */
    public static Agency[] getAgencies()
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        // get agencies from api
        JSONObject response = callApi(AGENCY_PATH);
        JSONArray data = response.getJSONArray(DATA);

        // transform into model classes
        ArrayList<Agency> agencies = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject agency = data.getJSONObject(i);
            Agency model = new Agency();
            Agency.set(model, agency);
            agencies.add(model);
        }

        return agencies.toArray(new Agency[agencies.size()]);
    }

    /**
     * Get agencies within the defined geoarea. Point-radius form. TransLoc
     * has an API parameter for this but since we've got the agencies locally
     * it's faster to do the measurement ourselves.
     *
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
            float[] result = new float[1];
            Location.distanceBetween(center.getLatitude(),
                    center.getLongitude(),
                    a.getLatitude(),
                    a.getLongitude(), result);

            float distanceMeters = result[0];
            if (distanceMeters < radius)
                agenciesInArea.add(a);
        }

        return agenciesInArea.toArray(new Agency[agenciesInArea.size()]);
    }

    /**
     * Get the routes for the given agency.
     *
     * @param agencyId id of the agency to retrieve routes for
     * @return array of routes for the agency
     */
    public static Route[] getRoutes(String agencyId)
            throws NetworkErrorException, SocketTimeoutException, JSONException {
        // encapsulate the single param in an array and call the more general overload
        return getRoutes(new String[] { agencyId }).get(agencyId);
    }

    /**
     * Get the routes for a given agency.
     *
     * @param agencyIds id's of agencies to retrieve routes for
     * @return hash map with agency ids as keys and corresponding route arrays as values
     */
    public static Map<String, Route[]> getRoutes(String... agencyIds)
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder.append(ROUTE_PATH).append("?agencies=");
        for (String i : agencyIds)
            builder.append(i).append(',');
        builder.deleteCharAt(builder.length() - 1); // delete trailing comma
        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);
        JSONObject data = response.getJSONObject(DATA);

        Map<String, Route[]> result = new LinkedHashMap<>(agencyIds.length);
        for (String agencyId : agencyIds) {
            JSONArray routelist = data.getJSONArray(agencyId);

            Route[] routes = new Route[routelist.length()];
            for (int j = 0; j < routelist.length(); j++) {
                JSONObject route = routelist.getJSONObject(j);
                Route model = new Route();
                Route.set(model, route);
                routes[j] = model;
            }

            result.put(agencyId, routes);
        }

        return result;
    }

    /**
     * Returns a list of encoded polylines representing the segments for the given route
     * along with their segment id's.
     *
     * @param r route to fetch segments for
     * @return list of encoded polylines keyed by id
     */
    public static Segment[] getSegments(Route route)
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        StringBuilder builder = new StringBuilder();
        builder
                .append(SEGMENT_PATH)
                .append("?")
                .append("agencies=")
                .append(route.getAgencyId())
                .append("&")
                .append("routes=")
                .append(route.getRouteId());
        String request = builder.toString();

        JSONObject response = callApi(request);
        JSONObject data = response.getJSONObject(DATA);

        ArrayList<Segment> segments = new ArrayList<>();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Segment model = new Segment();
            Segment.set(model, key, data.getString(key));
            segments.add(model);
        }

        return segments.toArray(new Segment[segments.size()]);
    }

    /**
     * Get a list of stops served by a given agency.
     *
     * @param agencyId The id of the agency to get stops for
     * @return An array of Stops.
     */
    public static Stop[] getStops(String agencyId)
            throws NetworkErrorException, SocketTimeoutException, JSONException {

        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder.append(STOP_PATH).append("?").append("agencies=").append(agencyId);
        String request = builder.toString();

        // call api
        JSONObject response = callApi(request);
        JSONArray data = response.getJSONArray(DATA);

        Stop[] stops = new Stop[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject stop = data.getJSONObject(i);
            Stop model = new Stop();
            Stop.set(model, stop);
            stops[i] = model;
        }

        return stops;
    }

    /**
     * Gets latest vehicles for the given agency and routes
     *
     * @param agencyId agency
     * @param routeId  route ids to retrieve vehicles for; should be limited to agency specified
     * @return list of vehicles currently running on the given route. If there are none currently
     * running, an empty list is returned.
     */
    public static List<Vehicle> getVehicles(String agencyId, String routeId)
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
            vehicleArray = response.getJSONObject(DATA).getJSONArray(agencyId);
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

}