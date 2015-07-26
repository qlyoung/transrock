package us.v4lk.transrock.transloc;

import java.util.Vector;

/**
 * Created by qly on 7/25/15.
 */
class BoundingBox {
    public final Vector<Double> topLeft;
    public final Vector<Double> bottomRight;

    public BoundingBox(Vector<Double> topLeft, Vector<Double> bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }
}
