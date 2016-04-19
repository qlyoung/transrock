package us.v4lk.transrock.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by qly on 2/6/16.
 */
public class Segment extends RealmObject {

    @PrimaryKey
    private String segmentId;
    private String segment;

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public static void set(Segment model, String segmentId, String segment) {
        model.setSegmentId(segmentId);
        model.setSegment(segment);
    }
}