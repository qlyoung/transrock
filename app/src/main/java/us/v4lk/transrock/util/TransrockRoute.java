package us.v4lk.transrock.util;

import us.v4lk.transrock.transloc.objects.Route;

/**
 * Full Route object for use on client side.
 *
 * This class defines routes that the user has saved and is used as
 * TransRock's internal representation of a route. The information
 * it contains is not time-dependent and should only need to be fetched
 * at time of creation. Information that is time-dependent is only keyed
 * here and should be accessed using said keys in conjunction with the
 * TransLoc API.
 */
public class TransrockRoute {

    /* properties of this route */
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
    public final String[] segments;
    public final String[] stopIds;

    private Route route;

    /* metadata for application use */
    private boolean activated = false;

    /**
     * Constructs a new TransRock route with deactivated status.
     * @param r the route
     */
    public TransrockRoute(Route r, String[] segments) {
        this(r, false, segments);
    }

    /**
     * Constructs a new TransRock route with the specified status.
     * @param r the route
     * @param activated whether this route should be displayed on the map
     */
    public TransrockRoute(Route r, boolean activated, String[] segments) {
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
        this.segments = segments;
        this.activated = activated;

        this.route = r;
    }

    /**
     * @return whether this route is activated
     */
    public boolean isActivated() { return activated; }
    /**
     * Sets whether this route is activated or not
     * @param activated whether the route is activated or not
     */
    public void setActivated(boolean activated) { this.activated = activated; }
    /** returns the route used to create this object; useful at times */
    public Route getRoute() {
        return route;
    }

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
