package us.v4lk.transrock.transloc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by qly on 5/31/15.
 */
public class Stop {

    public final String
        code,
        description,
        url,
        parent_station_id,
        station_id,
        location_type,
        stop_id,
        name;

    String[] agency_ids;
    String[] routes;
    Vector<Double> location;


    public Stop(JSONObject stopObject) throws JSONException {
        this.code = stopObject.getString("code");
        this.description = stopObject.getString("description");
        this.url = stopObject.getString("url");
        this.parent_station_id = stopObject.getString("parent_station_id");
        this.station_id = stopObject.getString("station_id");
        this.location_type = stopObject.getString("location_type");
        this.stop_id = stopObject.getString("stop_id");
        this.name = stopObject.getString("name");

        JSONArray aids = stopObject.getJSONArray("agency_ids");
        agency_ids = new String[aids.length()];
        for(int i = 0; i < aids.length(); i++)
            agency_ids[i] = aids.getString(i);

        JSONObject loc = stopObject.getJSONObject("location");
        location = new Vector<>(2);
        location.add(loc.getDouble("lat"));
        location.add(loc.getDouble("lng"));

        JSONArray rts = stopObject.getJSONArray("routes");
        routes = new String[rts.length()];
        for(int i = 0; i < rts.length(); i++)
            routes[i] = rts.getString(i);
    }
}
