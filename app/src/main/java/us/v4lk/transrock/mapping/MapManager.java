package us.v4lk.transrock.mapping;

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

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

import butterknife.BindDrawable;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import us.v4lk.transrock.R;
import us.v4lk.transrock.model.Route;
import us.v4lk.transrock.model.Segment;
import us.v4lk.transrock.model.Stop;
import us.v4lk.transrock.model.Vehicle;
import us.v4lk.transrock.transloc.TransLocAPI2;
import us.v4lk.transrock.util.Util;

/**
 * Takes care of asynchronously updating the map.
 */
public class MapManager {

    /** The map */
    Map map;
    /** The owning activity context */
    Context context;
    /** The global realm */
    Realm globalRealm;

    /** whether to center the map on the user's location each time we get a location update */
    boolean followMe = true;

    @BindDrawable(R.drawable.default_marker) Drawable location_marker;
    @BindDrawable(R.drawable.stop_marker) Drawable stop_marker;

    public MapManager(MapView map, Context context, View root) {
        this.context = context;

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

        // get a handle on the realm
        globalRealm = Realm.getInstance(context);
    }

    /**
     *
     */
    public void buildAndDraw() {
        new BuildOverlaysTask().execute();
    }

    /**
     * Updates position markers for vehicles on all routes.
     */
    public void updateVehicles() {
        new UpdateVehiclesTask().execute();
    }

    /**
     * Sets the position of the user location marker.
     * If isFollowMe() return true, then the map will automatically center
     * on this new location.
     * @param loc the new location
     */
    public void setLocation(Location loc) {
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

    /**
     * Sets or unsets follow-me mode. If set to true, the map will
     * automatically center on the location provided to setLocation() every
     * time that method is called.
     * @param followMe whether the map will automatically center on locations
     *                 set with setLocation
     */
    public void setFollowMe(boolean followMe) {
        this.followMe = followMe;
    }

    /**
     * @return Whether or not the map should automatically center on the user's
     *         location when calling setLocation()
     */
    public boolean isFollowMe() { return this.followMe; }

    class BuildOverlaysTask extends AsyncTask<Void, Void, Void> {

        private RealmResults<Route> activatedRoutes;

        public void buildSegments() {
            HashMap<String, Collection<Polyline>> routelines = new HashMap<>();

            // calculate how many times each segment is reused across all routes
            HashMap<Segment, Integer> totalCount = new LinkedHashMap<>(activatedRoutes.size());
            for (Route route : activatedRoutes)
                // for each segment in this route, increment that segment's global usage count
                for (Segment segment : route.getSegments()) {
                    int prevCount = totalCount.get(segment) == null ? 0 : totalCount.get(segment);
                    totalCount.put(segment, prevCount + 1);
                }

            // build overlays
            LinkedHashMap<Segment, Integer> visitedCount = new LinkedHashMap<>(totalCount.size());
            int basePolylineSize = 10;
            float dashScale = 100;

            for (Route route : activatedRoutes) {
                ArrayList<Polyline> segments = new ArrayList<>();

                for (Segment segment : route.getSegments()) {
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

            ArrayList<Polyline> allPolylines = new ArrayList<>();
            for (Collection<Polyline> routeline : routelines.values())
                allPolylines.addAll(routeline);

            map.setRoutesOverlay(allPolylines);
        }
        public void buildStops() {
            ItemizedIconOverlay stopoverlay = new ItemizedIconOverlay<>(context, new ArrayList<OverlayItem>(), null);

            // map each stop to the routes containing it
            HashMap<Stop, Collection<Route>> stopsToRoutes = new LinkedHashMap<>();
            for (Route route : activatedRoutes) {
                for (Stop stop : route.getStops()) {
                    Collection<Route> existing = stopsToRoutes.get(stop);
                    if (existing == null)
                        existing = new HashSet<>();
                    existing.add(route);
                    stopsToRoutes.put(stop, existing);
                }
            }

            // calculate & place marker items
            for (Stop stop : stopsToRoutes.keySet()) {
                // get the routes for this stop
                Collection<Route> stopRoutes = stopsToRoutes.get(stop);
                int numRoutes = stopRoutes.size();

                // get base marker drawable
                LayerDrawable stopmarker = (LayerDrawable) context.getResources().getDrawable(R.drawable.stop_marker);

                // add colored arcs, one for each route, that together form an evenly partitioned disk
                Drawable[] arcs = new Drawable[numRoutes];
                float sweep = 360f / numRoutes;
                int i = 0;
                for (Route route : stopRoutes) {
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

            map.setStopsOverlay(stopoverlay);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            map.clearVehiclesOverlay();
            map.clearStopsOverlay();
            map.invalidate();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // get a view into the Realm
            Realm realm = Realm.getInstance(context);
            activatedRoutes = realm.where(Route.class).equalTo("activated", true).findAll();

            try {
                // pull segments and stops for routes that don't have them yet (recently added)
                realm.beginTransaction();
                for (int i = 0; i < activatedRoutes.size(); i++) {
                    Route route = activatedRoutes.get(i);

                    // if route has no segments
                    if (route.getSegments().size() == 0) {
                        // fetch them all
                        String url = TransLocAPI2.segments(route.getAgencyId(), route.getRouteId());
                        JsonObject response = Ion.with(context).load(url).setHeader(TransLocAPI2.API_KEY_HEADER, TransLocAPI2.API_KEY).asJsonObject().get();
                        JSONObject re = new JSONObject(response.toString());
                        Segment[] segments = TransLocAPI2.buildSegments(re);

                        // and add them to the route
                        for (Segment sm : segments)
                            route.getSegments().add(sm);
                    }

                    // if route has no stops, fetch them all and add as relation to route
                    if (route.getStops().size() == 0) {
                        String url = TransLocAPI2.stops(route.getAgencyId());
                        JsonObject response = Ion.with(context).load(url).setHeader(TransLocAPI2.API_KEY_HEADER, TransLocAPI2.API_KEY).asJsonObject().get();
                        JSONObject re = new JSONObject(response.toString());
                        Stop[] stops = TransLocAPI2.buildStops(re, route.getRouteId());

                        for (Stop stop : stops)
                            route.getStops().add(stop);
                    }
                }
                realm.commitTransaction();

                // build segments and stop overlays
                buildSegments();
                buildStops();

            } catch (InterruptedException e) {
                // TODO: display an error
                this.cancel(true);
                return null;
            } catch (ExecutionException e) {
                // TODO: display an error
                this.cancel(true);
                return null;
            } catch (JSONException e) {
                // TODO: display an error
                this.cancel(true);
                return null;
            } finally {
                if (realm != null) {
                    if (realm.isInTransaction())
                        realm.cancelTransaction();
                    realm.close();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            map.invalidate();
        }
    }

    class UpdateVehiclesTask extends AsyncTask<Void, Void, ItemizedIconOverlay> {

        @Override
        protected ItemizedIconOverlay doInBackground(Void... params) {
            // get an instance of the global persisted realm for this thread
            Realm realm = Realm.getInstance(context);

            // list of vehicles associated by route
            HashMap<Route, Vehicle[]> vehicles = new HashMap<>();
            try {
                RealmResults<Route> activated = realm.where(Route.class).equalTo("activated", true).findAll();

                for (Route route : activated){
                    String url = TransLocAPI2.vehicles(route.getAgencyId(), route.getRouteId());
                    // TODO: see if we can reimplement this asynchronously and eliminate the asynctask
                    JsonObject response = Ion.with(context).load(url).setHeader(TransLocAPI2.API_KEY_HEADER, TransLocAPI2.API_KEY).asJsonObject().get();
                    // TODO: switch to using Gson for this instead of converting to JSONObject
                    JSONObject re = new JSONObject(response.toString());
                    Vehicle[] vs = TransLocAPI2.buildVehicles(re);

                    vehicles.put(realm.copyFromRealm(route), vs);
                }
            }
            catch (InterruptedException e) {
                // TODO: display an error
                this.cancel(true);
                return null;
            }
            catch (ExecutionException e) {
                // TODO: display an error
                this.cancel(true);
                return null;
            }
            catch (JSONException e) {
                // TODO: display an error
                this.cancel(true);
                return null;
            }

            // overlay for vehicles
            ItemizedIconOverlay<OverlayItem> vehicleOverlay = new ItemizedIconOverlay<>(context, new ArrayList<OverlayItem>(), null);

            // build overlay
            for (Route route : vehicles.keySet()) {
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
            realm.close();
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
