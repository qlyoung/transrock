package us.v4lk.transrock.transloc;

import java.util.ArrayList;

/**
 * Created by qly on 6/1/15.
 */
public class TransitSystem {

    private Agency agency;
    private Route[] routes;
    private Stop[] stops;


    public TransitSystem(Agency agency, Route[] routes, Stop[] stops) {
        this.agency = agency;
        this.stops = stops;
        this.routes = routes;
    }

    public Agency getAgency() { return agency; }
    public Route[] getRoutes() { return routes; }
    public Stop[] getStops() { return stops; }
}
