package us.v4lk.transrock.util;

import us.v4lk.transrock.transloc.Route;

/**
 * Created by qly on 9/3/15.
 */
public class StoredRoute {

    private Route route;
    private boolean active = false;

    public StoredRoute(Route r, boolean active) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoredRoute that = (StoredRoute) o;

        return !(route != null ? !route.equals(that.route) : that.route != null);

    }
    @Override
    public int hashCode() {
        return route != null ? route.hashCode() : 0;
    }
}
