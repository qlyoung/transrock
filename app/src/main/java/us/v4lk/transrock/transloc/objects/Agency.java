package us.v4lk.transrock.transloc.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * A transit agency.
 */
public class Agency {

    public final Vector<Double> position;
    public final BoundingBox boundingBox;
    public final String
        long_name,
        language,
        name,
        short_name,
        phone,
        url,
        timezone;
    public final int agency_id;

    /**
     * @param ao JSON object from TransLoc API representing Agency.
     * @throws JSONException
     */
    public Agency(JSONObject ao) throws JSONException {
        // unpack returned object
        this.long_name = ao.getString("long_name");
        this.language = ao.getString("language");
        this.name = ao.getString("name");
        this.short_name = ao.getString("short_name");
        this.phone = ao.getString("phone");
        this.url = ao.getString("url");
        this.timezone = ao.getString("timezone");
        this.agency_id = Integer.valueOf(ao.getString("agency_id"));

        JSONObject position = ao.getJSONObject("position");
        this.position = new Vector<>(2);
        this.position.add(position.getDouble("lat"));
        this.position.add(position.getDouble("lng"));

        JSONArray bounding_box = ao.getJSONArray("bounding_box");
        JSONObject tl = bounding_box.getJSONObject(0);
        JSONObject br = bounding_box.getJSONObject(1);
        Vector<Double> topLeft = new Vector<>(2);
        Vector<Double> bottomRight = new Vector<>(2);
        topLeft.add(tl.getDouble("lat"));
        topLeft.add(tl.getDouble("lng"));
        bottomRight.add(br.getDouble("lat"));
        bottomRight.add(br.getDouble("lng"));
        this.boundingBox = new BoundingBox(topLeft, bottomRight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Agency agency = (Agency) o;

        return agency_id == agency.agency_id;

    }

    @Override
    public int hashCode() {
        return agency_id;
    }
}
