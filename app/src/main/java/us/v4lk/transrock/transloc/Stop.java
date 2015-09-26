package us.v4lk.transrock.transloc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * A stop.
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

    public String[] agency_ids;
    public String[] routes;
    public Vector<Double> location;


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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stop stop = (Stop) o;

        return stop_id.equals(stop.stop_id);

    }

    @Override
    public int hashCode() {
        return stop_id.hashCode();
    }
}
