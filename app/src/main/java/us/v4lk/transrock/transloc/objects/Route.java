package us.v4lk.transrock.transloc.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * A route.
 */
public class Route implements Serializable {

    /* direct route data from json */
    enum Direction { FORWARD, BACKWARD }
    public class Segment implements Serializable {
        public final String id;
        public final Direction direction;
        public Segment(String id, Direction direction) {
            this.id = id;
            this.direction = direction;
        }
    }

    public final String
        description,
        short_name,
        route_id,
        color,
        text_color,
        long_name,
        url,
        type;
    public final Segment[] segments;
    public final int agency_id;
    public final boolean is_active, is_hidden;
    public final String[] stops;

    /**
     * @param ro JSON object from TransLoc API representing Route.
     * @throws JSONException
     */
    public Route(JSONObject ro) throws JSONException {
        this.description = ro.getString("description");
        this.short_name = ro.getString("short_name");
        this.route_id = ro.getString("route_id");
        this.color = ro.getString("color");
        this.is_active = ro.getBoolean("is_active");
        this.agency_id = ro.getInt("agency_id");
        this.text_color = ro.getString("text_color");
        this.long_name = ro.getString("long_name");
        this.url = ro.getString("url");
        this.is_hidden = ro.getBoolean("is_hidden");
        this.type = ro.getString("type");

        JSONArray segs = ro.getJSONArray("segments");
        segments = new Segment[segs.length()];
        for (int i = 0; i < segs.length(); i++) {
            JSONArray seg = segs.getJSONArray(i);
            String id = seg.getString(0);
            Direction direction = seg.getString(1).equals("forward") ?
                    Direction.FORWARD :
                    Direction.BACKWARD;
            segments[i] = new Segment(id, direction);
        }

        JSONArray stps = ro.getJSONArray("stops");
        stops = new String[stps.length()];
        for (int i = 0; i < stps.length(); i++)
            stops[i] = stps.getString(i);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route route = (Route) o;

        return route_id.equals(route.route_id);

    }
    @Override
    public int hashCode() {
        return route_id.hashCode();
    }
}