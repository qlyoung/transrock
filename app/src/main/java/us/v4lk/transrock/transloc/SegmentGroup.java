package us.v4lk.transrock.transloc;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by qly on 9/5/15.
 */
public class SegmentGroup {

    private final ArrayList<String> segments;

    public SegmentGroup(Collection<String> segments) {
        this.segments = new ArrayList<>();
        this.segments.addAll(segments);
    }
    public SegmentGroup(String... segments) {
        this.segments = new ArrayList<>();
        for (String s : segments)
            this.segments.add(s);
    }
    public SegmentGroup(JSONObject segments) throws JSONException {
        this.segments = new ArrayList<>();
        Iterator<String> keys = segments.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            this.segments.add(segments.getString(key));
        }
    }
}
