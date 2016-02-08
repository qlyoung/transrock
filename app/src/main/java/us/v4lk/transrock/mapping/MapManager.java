package us.v4lk.transrock.mapping;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.ButterKnife;
import us.v4lk.transrock.R;
import us.v4lk.transrock.model.RouteModel;
import us.v4lk.transrock.model.SegmentModel;
import us.v4lk.transrock.model.StopModel;
import us.v4lk.transrock.model.Vehicle;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.Util;

/**
 * Takes care of asynchronously updating the map.
 */
public class MapManager {

    Map map;
    ArrayList<RouteModel> routes;
    Context context;

    /** whether to center the map on the user's location each time we get a location update */
    boolean followMe = true;

    @BindDrawable(R.drawable.default_marker) Drawable location_marker;
    @BindDrawable(R.drawable.stop_marker) Drawable stop_marker;

    public MapManager(MapView map, Context context, View root) {
        this.context = context;
        this.routes = new ArrayList<>();

        ButterKnife.bind(this, root);

        // setup map
        this.map = new Map(context, map);
        this.map.setLocationMarkerDrawable(location_marker);
        this.map.setDefaultMarkerDrawable(stop_marker);
        this.map.setScaleBar(true);

        // add follow-me watchdog overlay
        TouchOverlay touchOverlay = new TouchOverlay(map.getContext());
        touchOverlay.setEnabled(true);
        map.getOverlayManager().add(touchOverlay);
    }

    /**
     * Takes a list of TransrockRoutes and sets up the map to display
     * all relevant information related to those routes.
     * Executes asynchronously.
     * @param routes
     */
    public void setRoutes(Collection<RouteModel> routes) {
        routes.clear();
        routes.addAll(routes);

        SetRoutesTask srt = new SetRoutesTask();
        srt.execute();
    }

    /**
     * Updates position markers for vehicles on all routes.
     */
    public void updateVehicles() {
        UpdateVehiclesTask uvt = new UpdateVehiclesTask();
        uvt.execute(routes);
    }
    /**
     * Handles a change in user location
     * @param loc the new location
     */
    public void updateLocation(Location loc) {
        GeoPoint location = Util.toGeoPoint(loc);
        map.setLocationMarkerPosition(location);
        map.setLocationMarkerOn(true);
        if (followMe)
            map.centerAndZoomOnPosition(location, true);
    }

    /**
     * Sets the location the map should be initially centered on.
     * Does not update location marker. Does not animate.
     */
    public void setMapPosition(Location loc) {
        map.centerAndZoomOnPosition(Util.toGeoPoint(loc), false);
    }

    public void setFollowMe(boolean followMe) {
        this.followMe = followMe;
    }

    class SetRoutesTask extends AsyncTask<Void, Void, Void> {

        private HashMap<String, Collection<Polyline>> routelines;
        private ItemizedIconOverlay stopoverlay;

        @Override
        protected Void doInBackground(Void... params) {

            // first, fetch segments if we don't have them already
            for (RouteModel route : routes) {
                if (route.getSegments() == null) {
                    // fetch segments and add to realm
                }
            }

            // calculate how many times each segment is reused across all routes
            LinkedHashMap<SegmentModel, Integer> totalCount = new LinkedHashMap<>(routes.size());
            for (RouteModel route : routes)
                for (SegmentModel segment : route.getSegments()) {
                    int prevCount = totalCount.get(segment) == null ? 0 : totalCount.get(segment);
                    totalCount.put(segment, prevCount + 1);
                }

            /* ------------ SEGMENTS --------------- */

            // build overlays
            LinkedHashMap<SegmentModel, Integer> visitedCount = new LinkedHashMap<>(totalCount.size());
            int basePolylineSize = 10;
            float dashScale = 100;
            routelines = new HashMap<>();

            for (RouteModel route : routes) {
                ArrayList<Polyline> segments = new ArrayList<>();

                for (SegmentModel segment : route.getSegments()) {
                    int timesVisited = visitedCount.get(segment) == null ? 0 : visitedCount.get(segment);

                    // get the polyline
                    Polyline p = Util.encodedPolylineToOverlay(segment.getSegment(), context);

                    p.setWidth(basePolylineSize);
                    p.setColor(Color.parseColor("#" + route.getColor()));

                    // calculate the dash effect
                    float split = 1f / totalCount.get(segment);             // ratio of this line : all other lines
                    float dashOn = split * dashScale;                       // magnitude of 'on' segments
                    float dashOff = dashOn * (totalCount.get(segment) - 1); // magnitude of 'off' segments, which should equal split * scale * lines remaining
                    DashPathEffect dashFect = new DashPathEffect(new float[]{dashOn, dashOff}, timesVisited * dashOn);

                    // set effect
                    p.getPaint().setPathEffect(dashFect);

                    // increment segment's visited count
                    visitedCount.put(segment, timesVisited + 1);

                    // add polyline to list
                    segments.add(p);
                }

                routelines.put(route.getRouteId(), segments);
            }


            /* ------------ STOPS ----------------- */

            stopoverlay = new ItemizedIconOverlay<OverlayItem>(context, new ArrayList<OverlayItem>(), null);

            // map each stop to the routes containing it
            HashMap<StopModel, Collection<RouteModel>> stopsToRoutes = new LinkedHashMap<>();
            for (RouteModel route : routes) {
                for (StopModel stop : route.getStops()) {
                    Collection<RouteModel> existing = stopsToRoutes.get(stop);
                    if (existing == null)
                        existing = new HashSet<>();
                    existing.add(route);
                    stopsToRoutes.put(stop, existing);
                }
            }

            // calculate & place marker items
            for (StopModel stop : stopsToRoutes.keySet()) {
                // get the routes for this stop
                Collection<RouteModel> stopRoutes = stopsToRoutes.get(stop);
                int numRoutes = stopRoutes.size();

                // get base marker drawable
                LayerDrawable stopmarker = (LayerDrawable) context.getResources().getDrawable(R.drawable.stop_marker);

                // add colored arcs, one for each route, that together form an evenly partitioned disk
                Drawable[] arcs = new Drawable[numRoutes];
                float sweep = 360f / numRoutes;
                int i = 0;
                for (RouteModel route : stopRoutes) {
                    ShapeDrawable arc = new ShapeDrawable(new ArcShape(sweep * i, sweep));
                    arc.setIntrinsicWidth(30);
                    arc.setIntrinsicHeight(30);
                    arc.getPaint().setColor(Color.parseColor("#" + route.getColor()));
                    arcs[i++] = arc;
                }
                // set the result as the disk drawable
                stopmarker.setDrawableByLayerId(R.id.disk_layer_drawable, new LayerDrawable(arcs));

                // make new OverlayItem for this stop
                GeoPoint stopMarkerLocation = new GeoPoint(stop.getLatitude(), stop.getLongitude());
                OverlayItem item = new OverlayItem(stop.getName(), stop.getDescription(), stopMarkerLocation);
                item.setMarker(stopmarker);

                // add it to the overlay
                stopoverlay.addItem(item);
            }

            ArrayList<Polyline> allPolylines = new ArrayList<>();
            for (Collection<Polyline> routeline : routelines.values())
                allPolylines.addAll(routeline);
            map.setRoutesOverlay(allPolylines);
            map.setStopsOverlay(stopoverlay);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new UpdateVehiclesTask().execute(routes);
        }
    }

    class UpdateVehiclesTask extends AsyncTask<Collection<RouteModel>, Void, ItemizedIconOverlay> {

        @Override
        protected ItemizedIconOverlay doInBackground(Collection<RouteModel>... params) {
            // fetch vehicles
            HashMap<RouteModel, List<Vehicle>> vehicles = new HashMap<>();

            try {
                for (RouteModel route : routes){
                    List<Vehicle> v = TransLocAPI.getVehicles(route.getAgencyId(), route.getRouteId());
                    vehicles.put(route, v);
                }
            }
            catch (NetworkErrorException e) {

            }
            catch (SocketTimeoutException e) {

            }
            catch (JSONException e) {

            }

            ItemizedIconOverlay<OverlayItem> vehicleOverlay = new ItemizedIconOverlay<>(context, new ArrayList<OverlayItem>(), null);

            // build overlay
            for (RouteModel route : vehicles.keySet()) {
                // tint marker to match route color
                Drawable vehicleMarker = DrawableCompat.wrap(context.getResources().getDrawable(R.drawable.ic_directions_bus_white_24dp));
                DrawableCompat.setTint(vehicleMarker.mutate(), Color.parseColor("#" + route.getColor()));

                for (Vehicle vehicle : vehicles.get(route)) {
                    GeoPoint loc = new GeoPoint(vehicle.location.firstElement(), vehicle.location.lastElement());
                    OverlayItem marker = map.makeMarker(loc, vehicleMarker, vehicle.call_name, vehicle.description);
                    vehicleOverlay.addItem(marker);
                }
            }

            map.setVehiclesOverlay(vehicleOverlay);
            return vehicleOverlay;
        }

        @Override
        protected void onPostExecute(ItemizedIconOverlay vehicleOverlay) {
            super.onPostExecute(vehicleOverlay);
            map.invalidate();
        }
    }

    /**
     * Hacky workaround for all of MapView's touch callbacks being hosed.
     * This overlay covers the whole map and breaks follow-me when touched.
     */
    class TouchOverlay extends Overlay {
        Context context;

        public TouchOverlay(Context c) {
            super(c);
            context = c;
        }

        @Override
        protected void draw(Canvas c, MapView osmv, boolean shadow) { }
        @Override
        public boolean onDown(MotionEvent e, MapView mapView) {
            // disable follow-me; user goes to manual mode
            setFollowMe(false);
            return super.onDown(e, mapView);
        }
    }
}
