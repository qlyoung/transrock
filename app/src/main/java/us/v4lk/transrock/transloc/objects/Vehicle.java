package us.v4lk.transrock.transloc.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/**
 * Vehicle
 */
public class Vehicle {

    class ArrivalEstimate {
        public String route_id;
        public Date arrival_at;
        public String stop_id;

        public ArrivalEstimate(String route_id, String arrival_at, String stop_id) throws ParseException {
            this.route_id = route_id;
            this.stop_id = stop_id;

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"){
                public Date parse(String source, ParsePosition pos) {
                    return super.parse(source.replaceFirst(":(?=[0-9]{2}$)",""),pos);
                }
            };
            this.arrival_at = df.parse(arrival_at);
        }
    }

    public String
            description,
            call_name,
            vehicle_id,
            segment_id,
            route_id;
    public int
            passenger_load,
            standing_capacity,
            seating_capacity,
            speed,
            heading;

    public ArrayList<ArrivalEstimate> arrival_estimates;
    public Vector<Double> location;
    public Date last_updated_on;
    public boolean tracking_status;

    public Vehicle(JSONObject vo) throws JSONException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
            public Date parse(String source, ParsePosition pos) {
                return super.parse(source.replaceFirst(":(?=[0-9]{2}$)",""),pos);
            }
        };

        this.description = vo.getString("description");
        this.passenger_load = vo.getInt("passenger_load");
        this.standing_capacity = vo.getInt("standing_capacity");
        this.seating_capacity = vo.getInt("seating_capacity");
        try { this.last_updated_on = df.parse(vo.getString("last_updated_on")); }
        catch (ParseException p) { throw new JSONException(p.getMessage()); }
        this.call_name = vo.getString("call_name");
        this.speed = vo.getInt("speed");
        this.vehicle_id = vo.getString("vehicle_id");
        this.segment_id = vo.getString("segment_id");
        this.route_id = vo.getString("route_id");
        this.tracking_status = vo.getString("tracking_status").equals("up");
        this.heading = vo.getInt("heading");

        JSONObject loc = vo.getJSONObject("location");
        this.location = new Vector<>(2);
        this.location.add(loc.getDouble("lat"));
        this.location.add(loc.getDouble("lng"));

        this.arrival_estimates = new ArrayList<>();
        JSONArray estimates = vo.getJSONArray("arrival_estimates");
        for (int i = 0; i < estimates.length(); i++) {
            JSONObject arrivalEstimate = estimates.getJSONObject(i);
            try {
                ArrivalEstimate a = new ArrivalEstimate(arrivalEstimate.getString("route_id"),
                        arrivalEstimate.getString("arrival_at"),
                        arrivalEstimate.getString("stop_id"));
                this.arrival_estimates.add(a);
            } catch (ParseException p) { throw new JSONException(p.getMessage()); }
        }
    }
}
