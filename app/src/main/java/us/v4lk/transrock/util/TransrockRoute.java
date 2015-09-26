package us.v4lk.transrock.util;

import java.util.Collection;

import us.v4lk.transrock.transloc.Route;

/**
 * Route with application metadata. Wraps the raw Route object from the TransLoc API.
 */
public class TransrockRoute {

    // properties of this route
    public final String
            description,
            short_name,
            route_id,
            color,
            text_color,
            long_name,
            url,
            type;
    public final int agency_id;
    public final String[] stopIds;
    public final String[] segmentIds;

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
        description = r.description;
        short_name = r.short_name;
        route_id = r.route_id;
        color = r.color;
        text_color = r.text_color;
        long_name = r.long_name;
        url = r.url;
        type = r.type;
        agency_id = r.agency_id;
        stopIds = r.stops;
        segmentIds = new String[r.segments.length];
        for (int i = 0; i < segmentIds.length; i++)
            segmentIds[i] = r.segments[i].id;
        this.active = active;
    }

    /**
     * @return whether this route is active
     */
    public boolean isActive() { return active; }
    /**
     * Sets whether this route is active or not
     * @param active whether the route is active or not
     */
    public void setActive(boolean active) { this.active = active; }

    /**
     * @return polyline encoded segments
     */
    public String[] getSegmentIds() { return segmentIds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransrockRoute route = (TransrockRoute) o;

        return route_id.equals(route.route_id);

    }
    @Override
    public int hashCode() {
        return route_id.hashCode();
    }
}
