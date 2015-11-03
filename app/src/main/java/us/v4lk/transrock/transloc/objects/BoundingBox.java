package us.v4lk.transrock.transloc.objects;

import java.util.Vector;

/**
 * Simple class with two double-precision vectors of magnitude 2.
 * Defines a rectangular geoarea in terms of latitude/longitude.
 */
class BoundingBox {
    public final Vector<Double> topLeft;
    public final Vector<Double> bottomRight;

    public BoundingBox(Vector<Double> topLeft, Vector<Double> bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }
}
