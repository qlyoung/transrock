package us.v4lk.transrock.mapping;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.objects.Stop;
import us.v4lk.transrock.transloc.objects.Vehicle;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.TransrockRoute;

/**
 * Convenience wrapper for MapView that encapsulates many common
 * tasks into easy-to-use methods. Also tracks different classes
 * and groups of overlays for easy addition and removal.
 */
public class MapWrap {

    /** map zoom level */
    final int MAP_ZOOM_LEVEL;
    /** context */
    final Context context;
    /** map **/
    final MapView map;

    /** default marker drawable */
    Drawable defaultMarkerDrawable;
    /** location marker drawable */
    Drawable locationMarkerDrawable;

    /** location marker */
    final ItemizedIconOverlay<OverlayItem> locationMarkerOverlay;
    /** scale bar overlay */
    final ScaleBarOverlay scaleBarOverlay;
    /** route polyline overlays */
    final Map<String, Collection<Polyline>> routeOverlays;
    /** combined stop marker overlays */
    ItemizedIconOverlay stopOverlay;
    /** vehicle overlays */
    final ItemizedIconOverlay<OverlayItem> vehicleOverlay;

    /**
     * @param c context
     * @param mapView the mapview to wrap
     */
    public MapWrap(Context c, MapView mapView) {
        this(c, mapView, 17);
    }
    public MapWrap(Context c, MapView mapView, int defaultZoomLevel) {
        // context & map
        context = c;
        map = mapView;
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setMultiTouchControls(true);

        // no idea what this does but i need it for bitmaps
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(c);

        // drawables
        defaultMarkerDrawable = resourceProxy.getDrawable(bitmap.marker_default);
        locationMarkerDrawable = defaultMarkerDrawable;

        // scale bar
        scaleBarOverlay = new ScaleBarOverlay(context);
        scaleBarOverlay.setEnabled(true);
        map.getOverlayManager().add(scaleBarOverlay);

        // routes
        routeOverlays = new HashMap<>();

        // vehicles
        vehicleOverlay = new ItemizedIconOverlay<OverlayItem>(new ArrayList(), defaultMarkerDrawable, null, resourceProxy);
        map.getOverlayManager().add(vehicleOverlay);

        // location marker
        locationMarkerOverlay = new ItemizedIconOverlay<>(new ArrayList<OverlayItem>(), locationMarkerDrawable, null, resourceProxy);
        setLocationMarkerPosition(new GeoPoint(0, 0));
        map.getOverlayManager().add(locationMarkerOverlay);

        MAP_ZOOM_LEVEL = defaultZoomLevel;
    }

    /**
     * Converts a location to a GeoPoint
     * @param l location to convert
     * @return GeoPoint at same lat/lng as the passed location
     */
    public static GeoPoint toGeoPoint(Location l) {
        return new GeoPoint(l.getLatitude(), l.getLongitude());
    }

    /**
     * Enables / disables scale bar
     * @param on whether the bar should be enabled or disabled
     */
    public void setScaleBar(boolean on) {
        scaleBarOverlay.setEnabled(on);
    }

    /**
     * Enables / disables location marker
     * @param on whether the location marker should be enabled or disabled
     */
    public void setLocationMarkerOn(boolean on) {
        locationMarkerOverlay.setEnabled(on);
    }

    /**
     * Sets the position of the location marker
     * @param p the position
     */
    public void setLocationMarkerPosition(GeoPoint p) {
        locationMarkerOverlay.removeAllItems();
        OverlayItem locationMarker = makeMarker(p, locationMarkerDrawable);
        locationMarkerOverlay.addItem(locationMarker);
    }

    /**
     * Sets the position of the location marker
     * @param l the position
     */
    public void setLocationMarkerPosition(Location l) {
        setLocationMarkerPosition(toGeoPoint(l));
    }

    /**
     * Sets the drawable used by the location marker
     * @param d the drawable
     */
    public void setLocationMarkerDrawable(Drawable d) {
        locationMarkerDrawable = d;
        locationMarkerOverlay.getItem(0).setMarker(locationMarkerDrawable);
    }

    /**
     * Sets the default marker for miscellaneous markers
     * @param d the drawable
     */
    public void setDefaultMarkerDrawable(Drawable d) {
        defaultMarkerDrawable = d;
    }

    /**
     * Centers the map on the given geopoint
     * @param center the geopoint to center on
     * @param animate whether map should animate to center
     */
    public void centerOnPosition(GeoPoint center, boolean animate) {
        if (animate)
            map.getController().animateTo(center);
        else
            map.getController().setCenter(center);
    }
    public void centerOnPosition(Location l, boolean animate) {
        centerOnPosition(toGeoPoint(l), animate);
    }
    public void centerAndZoomOnPosition(GeoPoint center, boolean animate) {
        map.getController().setZoom(MAP_ZOOM_LEVEL);
        if (animate)
            map.getController().animateTo(center);
        else
            map.getController().setCenter(center);
    }
    public void centerAndZoomOnPosition(Location l, boolean animate) {
        centerAndZoomOnPosition(toGeoPoint(l), animate);
    }
    public void invalidate() { map.invalidate(); }
    public MapView getMapView() {
        return map;
    }
    public Context getContext() { return context; }

    /* overlays */

    /**
     * Adds overlay(s) to the map. You must keep a reference to each overlay
     * if you wish to remove it later.
     * @param overlays overlays to add
     */
    private void addOverlay(Overlay... overlays) {
        OverlayManager overlayManager = map.getOverlayManager();
        for (Overlay overlay : overlays)
            overlayManager.add(overlay);
    }
    /**
     * Adds a collection of overlays to the map. You must keep a reference
     * to each overlay if you wish to remove them later.
     * @param overlays overlays to add
     */
    private void addOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlayManager().addAll(overlays);
    }
    /**
     * Removes overlay(s) from the map. Removes any overlays that .equals()
     * the overlays passed.
     * @param overlays overlays to remove
     */
    private void removeOverlay(Overlay... overlays) {
        OverlayManager overlayManager = map.getOverlayManager();
        for (Overlay overlay : overlays)
            overlayManager.remove(overlay);
    }
    private void removeOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlayManager().removeAll(overlays);
    }
    /**
     * Adds a collection of segments that comprise a single route path.
     * MapWrap maintains an internal list of these overlays keyed by route id
     * and provides convenience methods for removing and updating them.
     * @param route_id route these segments are associated with
     * @param segmentOverlays collection of segments that should be drawn together for this route
     */
    public void addRouteOverlay(String route_id, Collection<Polyline> segmentOverlays) {
        routeOverlays.put(route_id, segmentOverlays);
        addOverlay(segmentOverlays);
    }
    /**
     * Sets the combined overlay for stops.
     * @param stopOverlay ItemizedIconOverlay with all markers for all stops for all routes.
     */
    public void setStopsOverlay(ItemizedIconOverlay stopOverlay) {
        removeOverlay(this.stopOverlay);
        this.stopOverlay = stopOverlay;
        addOverlay(stopOverlay);
    }
    /**
     * Sets routes that should be drawn to the map.
     * Build path and stop overlays and adds them.
     * Should call invalidate() afterwards.
     * @param routes routes to add
     */
    public void setRoutes(Collection<TransrockRoute> routes) {

        // todo: run this on a non-ui thread

        // remove old
        for (Collection<Polyline> path : routeOverlays.values())
            removeOverlay(path);

        /* ------------ SEGMENTS --------------- */

        // calculate how many times each segment is reused across all routes
        Map<String, Integer> totalCount = new LinkedHashMap<>(routes.size());
        for (TransrockRoute route : routes)
            for (String segment : route.segments) {
                int prevCount = totalCount.get(segment) == null ? 0 : totalCount.get(segment);
                totalCount.put(segment, prevCount + 1);
            }

        // build overlays
        Map<String, Integer> visitedCount = new LinkedHashMap<>(totalCount.size());
        int basePolylineSize = 10;
        float dashScale = 100;

        for (TransrockRoute route : routes) {
            ArrayList<Polyline> segments = new ArrayList<>();

            for (String segment : route.segments) {
                int timesVisited = visitedCount.get(segment) == null ? 0 : visitedCount.get(segment);

                // get the polyline
                Polyline p = Polylines.encodedPolylineToOverlay(segment, context);
                p.setWidth(basePolylineSize);
                p.setColor(Color.parseColor("#" + route.color));

                // calculate the dash effect
                float split = 1f / totalCount.get(segment);             // ratio of this line : all other lines
                float dashOn = split * dashScale;                       // magnitude of 'on' segments
                float dashOff = dashOn * (totalCount.get(segment) - 1); // magnitude of 'off' segments, which should equal split * scale * lines remaining
                DashPathEffect dashFect = new DashPathEffect(new float[] {dashOn, dashOff}, timesVisited * dashOn);

                // set effect
                p.getPaint().setPathEffect(dashFect);

                // increment segment's visited count
                visitedCount.put(segment, timesVisited + 1);

                // add polyline to list
                segments.add(p);
            }

            // add polylines to thingy
            addRouteOverlay(route.route_id, segments);
        }

        /* ------------- STOPS ----------------- */

        // map each stop to the routes containing it
        Map<Stop, Collection<TransrockRoute>> stopsToRoutes = new LinkedHashMap<>();
        for (TransrockRoute route : routes) {
            for (Stop stop : route.stops) {
                Collection<TransrockRoute> existing = stopsToRoutes.get(stop);
                if (existing == null)
                    existing = new HashSet<>();
                existing.add(route);
                stopsToRoutes.put(stop, existing);
            }
        }

        ItemizedIconOverlay<OverlayItem> stopoverlay = new ItemizedIconOverlay<>(context, new ArrayList<OverlayItem>(), null);

        for (Stop stop : stopsToRoutes.keySet()) {
            Collection<TransrockRoute> stopRoutes = stopsToRoutes.get(stop);
            int numRoutes = stopRoutes.size();

            // build drawable for this stop
            Drawable[] drawables = new Drawable[numRoutes + 1];
            float sweep = 360f / numRoutes;
            int i = 0;
            for (TransrockRoute route : stopRoutes) {
                // create arc section for this route
                ShapeDrawable arcDrawable = new ShapeDrawable(new ArcShape(sweep * i, sweep));
                arcDrawable.setIntrinsicWidth(30);
                arcDrawable.setIntrinsicHeight(30);
                arcDrawable.getPaint().setColor(Color.parseColor("#" + route.color));
                // add to composite drawable list
                drawables[i] = arcDrawable;
                i++;
            }
            drawables[numRoutes] = context.getResources().getDrawable(R.drawable.stop_marker); // border
            LayerDrawable stopMarkerDrawable = new LayerDrawable(drawables);

            // make new OverlayItem for this stop
            GeoPoint stopMarkerLocation = new GeoPoint(stop.location.get(0), stop.location.get(1));
            OverlayItem item = new OverlayItem(stop.name, stop.description, stopMarkerLocation);
            item.setMarker(stopMarkerDrawable);

            // add it to the overlay
            stopoverlay.addItem(item);
        }

        setStopsOverlay(stopoverlay);

    }
    /**
     * Sets the vehicles should be drawn to the map. Replaces any previous vehicles
     * that were being drawn.
     * @param vehicles vehicles to add
     */
    public void setVehicles(Collection<Vehicle> vehicles) {
        this.vehicleOverlay.removeAllItems();

        for (Vehicle vehicle : vehicles) {
            GeoPoint vehicleLocation = new GeoPoint(vehicle.location.firstElement(), vehicle.location.lastElement());
            OverlayItem item = new OverlayItem(vehicle.call_name, "", vehicleLocation);
            this.vehicleOverlay.addItem(item);
        }

    }


    /* marker convenience */
    public OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable, String title, String snippet) {
        OverlayItem marker = new OverlayItem(title, snippet, p);
        if (markerDrawable == null) markerDrawable = defaultMarkerDrawable;
        marker.setMarker(markerDrawable);
        return marker;
    }
    public OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable) {
        return makeMarker(p, markerDrawable, "", "");
    }

}