package us.v4lk.transrock.util;

import java.util.Collection;
import java.util.Set;

import us.v4lk.transrock.transloc.Route;

/**
 * Route with application metadata. Wraps the raw Route object from the TransLoc API.
 */
public class TransrockRoute {

    private Collection<String> segments;

    /**
     * The route.
     */
    private Route route;
    /**
     * Whether this route should be shown on the map.
     */
    private boolean active = false;

    /**
     * Constructs a new TransRock route with deactivated status.
     * @param r the route
     */
    public TransrockRoute(Route r) {
        this(r, false);
    }

    /**
     * Constructs a new TransRock route with the specified status.
     * @param r the route
     * @param active whether this route should be displayed on the map
     */
    public TransrockRoute(Route r, boolean active) {
        route = r;
        this.active = active;
    }

    /**
     * @return the corresponding Route object
     */
    public Route getRoute() { return route; }
    /**
     * @return whether this route is active
     */
    public boolean isActive() { return active; }
    /**
     * Sets whether this route is active or not
     * @param active whether the route is active or not
     */
    public void setActive(boolean active) { this.active = active; }

    public void setSegments(Collection<String> segments) {
        this.segments = segments;
    }
    public Collection<String> getSegments() { return segments; }

    /* these equivalency methods are crucial to other pieces of the code, do not change them */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransrockRoute that = (TransrockRoute) o;

        return !(route != null ? !route.equals(that.route) : that.route != null);
    }
    @Override
    public int hashCode() {
        return route != null ? route.hashCode() : 0;
    }
}
