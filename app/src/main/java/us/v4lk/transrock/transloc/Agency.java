package us.v4lk.transrock.transloc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * A transit provider.
 */
public class Agency {

    class BoundingBox {
        public final Vector<Double> topLeft;
        public final Vector<Double> bottomRight;

        public BoundingBox(Vector<Double> topLeft, Vector<Double> bottomRight) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
        }
    }

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

    public Agency(JSONObject providerObject) throws JSONException {
        this.long_name = providerObject.getString("long_name");
        this.language = providerObject.getString("language");
        this.name = providerObject.getString("name");
        this.short_name = providerObject.getString("short_name");
        this.phone = providerObject.getString("phone");
        this.url = providerObject.getString("url");
        this.timezone = providerObject.getString("timezone");
        this.agency_id = Integer.valueOf(providerObject.getString("agency_id"));

        JSONObject position = providerObject.getJSONObject("position");
        this.position = new Vector<>(2);
        this.position.add(position.getDouble("lat"));
        this.position.add(position.getDouble("lng"));

        JSONArray bounding_box = providerObject.getJSONArray("bounding_box");
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
}
