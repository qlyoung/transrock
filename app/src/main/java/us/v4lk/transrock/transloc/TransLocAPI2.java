package us.v4lk.transrock.transloc;

import com.android.volley.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import us.v4lk.transrock.model.Agency;
import us.v4lk.transrock.model.Route;
import us.v4lk.transrock.model.Segment;
import us.v4lk.transrock.model.Stop;
import us.v4lk.transrock.model.Vehicle;

/**
 * Endpoint for TransLoc API.
 */
public class TransLocAPI2 {

    private static final String
            ROOT = "https://transloc-api-1-2.p.mashape.com",
            AGENCY_PATH = "/agencies.json",
            ROUTE_PATH = "/routes.json",
            STOP_PATH = "/stops.json",
            SEGMENT_PATH = "/segments.json",
            VEHICLE_PATH = "/vehicles.json";
    private static final String DATA = "data";
    public static final String API_KEY_HEADER = "X-Mashape-Key";
    public static final String API_KEY = "HVy1utpe5Smsh8QVRRAES2GQu4pdp1Qx9gYjsnAoiFVQ0DZcXz";

    public static String apiRequest(String relativeUrl) {
        return ROOT + relativeUrl;
    }

    /**
     * Request builder to get all agencies
     * @param responseListener
     * @param errorListener
     * @return the request
     */
    public static String agencies(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        return apiRequest(AGENCY_PATH);
    }

    /**
     * Request builder to get all routes for the specified agencies.
     * @param responseListener
     * @param errorListener
     * @param agencyIds
     * @return the request
     */
    public static String routes(String... agencyIds) {
        StringBuilder builder = new StringBuilder();
        builder.append(ROUTE_PATH).append("?agencies=");
        for (String i : agencyIds)
            builder.append(i).append(',');
        builder.deleteCharAt(builder.length() - 1); // delete trailing comma
        String url = builder.toString();

        return apiRequest(url);
    }

    /**
     * Request builder to get all segments for the specified route
     * @param responseListener
     * @param errorListener
     * @param agencyId
     * @param routeId
     * @return the request
     */
    public static String segments(String agencyId, String routeId) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(SEGMENT_PATH)
                .append("?")
                .append("agencies=")
                .append(agencyId)
                .append("&")
                .append("routes=")
                .append(routeId);
        String url = builder.toString();

        return apiRequest(url);
    }

    /**
     * Request builder to get all stops for the specified agency
     * @param responseListener
     * @param errorListener
     * @param agencyId
     * @return the request
     */
    public static String stops(String agencyId) {
        // build request parameter
        StringBuilder builder = new StringBuilder();
        builder.append(STOP_PATH).append("?").append("agencies=").append(agencyId);
        String url = builder.toString();

        return apiRequest(url);
    }

    /**
     * Request builder to get all vehicles for the specified agency
     * @param responseListener
     * @param errorListener
     * @param agencyId
     * @param routeId
     * @return the request
     */
    public static String vehicles(String agencyId, String routeId) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(VEHICLE_PATH)
                .append("?")
                .append("agencies=").append(agencyId)
                .append("&")
                .append("routes=")
                .append(routeId);
        String url = builder.toString();

        return apiRequest(url);
    }

    /**
     * Builds an array of Agency objects from the API response.
     * @param response
     * @return the agencies
     * @throws JSONException
     */
    public static Agency[] buildAgencies(JSONObject response) throws JSONException {
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
     * Builds an array of Routes for each Agency in the API response.
     * @param response
     * @return a map from agencies requested to routes returned
     * @throws JSONException
     */
    public static Map<String, Route[]> buildRoutes(JSONObject response) throws JSONException {
        JSONObject data = response.getJSONObject(DATA);
        Iterator<String> keys = data.keys();

        Map<String, Route[]> result = new LinkedHashMap<>();

        while (keys.hasNext()) {
            String agencyId = keys.next();
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
     * Builds an array of Segment objects from the API response.
     * @param response
     * @return the segments
     * @throws JSONException
     */
    public static Segment[] buildSegments(JSONObject response) throws JSONException {
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
     * Builds an array of Stop objects from the API response.
     * @param response
     * @return the stops
     * @throws JSONException
     */
    public static Stop[] buildStops(JSONObject response) throws JSONException {
        JSONArray data = response.getJSONArray(DATA);

        ArrayList<Stop> stops = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject stop = data.getJSONObject(i);
            Stop model = new Stop();
            Stop.set(model, stop);
            stops.add(model);
        }

        return stops.toArray(new Stop[stops.size()]);
    }

    public static Vehicle[] buildVehicles(JSONObject response) throws JSONException {
        // extract data block from response
        JSONObject data = response.getJSONObject(DATA);
        JSONArray names = data.names();

        // extract json vehicle array if it exists
        JSONArray vehicleArray = null;
        if (names != null)
            vehicleArray = data.getJSONArray(data.names().getString(0));

        // array to hold vehicles, or empty array if there are none
        Vehicle[] vehicles = new Vehicle[vehicleArray == null ? 0 : vehicleArray.length()];

        // pull out the vehicles and put them in an array
        for (int i = 0; i < vehicles.length; i++)
            vehicles[i] = new Vehicle(vehicleArray.getJSONObject(i));

        return vehicles;
    }

}