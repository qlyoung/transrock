package us.v4lk.transrock.transloc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * A transit provider.
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

    /* additional data */
    public final boolean local;

    /**
     * Agency
     * local = false
     * @param ao agency object
     * @throws JSONException
     */
    public Agency(JSONObject ao) throws JSONException {
        this(ao, false);
    }

    /**
     * Agency
     * @param ao agency object
     * @param local whether this agency is local to the current location
     * @throws JSONException
     */
    public Agency(JSONObject ao, boolean local) throws JSONException {
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

        // set additional data
        this.local = local;
    }

    /**
     * Does equality comparison based on agency_id.
     * @param o object to compare
     * @return whether this agency is the same as the passed agency
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Agency))
            return false;
        Agency other = (Agency) o;
        return other.agency_id == agency_id;
    }
}
