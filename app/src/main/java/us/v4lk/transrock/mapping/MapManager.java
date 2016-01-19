package us.v4lk.transrock.mapping;

import android.accounts.NetworkErrorException;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.AsyncTask;
import android.provider.Telephony;

import org.json.JSONException;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.transloc.objects.Stop;
import us.v4lk.transrock.transloc.objects.Vehicle;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.TransrockRoute;

/**
 * Created by qly on 1/18/16.
 */
public class MapManager {

    MapWrap map;
    ArrayList<TransrockRoute> routes;
    Context context;

    public MapManager(MapWrap map, Context context) {
        this.map = map;
        this.context = context;
        this.routes = new ArrayList<>();
    }

    /**
     * Takes a list of TransrockRoutes and does everything necessary to
     * draw all components of the route onto the map.
     * Builds polyline overlays, fetches stops from the network,
     * @param routes
     */
    public void setRoutes(Collection<TransrockRoute> routes) {
        this.routes.clear();
        this.routes.addAll(routes);
        SetRoutesTask srt = new SetRoutesTask();
        srt.execute(routes);
    }
    public void updateVehicles() {
        UpdateVehiclesTask uvt = new UpdateVehiclesTask();
        uvt.execute(routes);
    }
    class SetRoutesTask extends AsyncTask<Collection<TransrockRoute>, Void, Void> {

        private Map<String, Collection<Polyline>> routelines;
        private ItemizedIconOverlay stopoverlay;

        @Override
        protected Void doInBackground(Collection<TransrockRoute>... params) {
            Collection<TransrockRoute> routes = params[0];

            // calculate how many times each segment is reused across all routes
            Map<String, Integer> totalCount = new LinkedHashMap<>(routes.size());
            for (TransrockRoute route : routes)
                for (String segment : route.segments) {
                    int prevCount = totalCount.get(segment) == null ? 0 : totalCount.get(segment);
                    totalCount.put(segment, prevCount + 1);
                }

            /* ------------ SEGMENTS --------------- */

            // build overlays
            Map<String, Integer> visitedCount = new LinkedHashMap<>(totalCount.size());
            int basePolylineSize = 10;
            float dashScale = 100;
            routelines = new HashMap<>();

            Map<String, Collection<Polyline>> routeOverlays = new HashMap<>();
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
                    DashPathEffect dashFect = new DashPathEffect(new float[]{dashOn, dashOff}, timesVisited * dashOn);

                    // set effect
                    p.getPaint().setPathEffect(dashFect);

                    // increment segment's visited count
                    visitedCount.put(segment, timesVisited + 1);

                    // add polyline to list
                    segments.add(p);
                }

                routelines.put(route.route_id, segments);
            }


            /* ------------ STOPS ----------------- */

            stopoverlay = new ItemizedIconOverlay<>(context, new ArrayList<OverlayItem>(), null);

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

            // calculate & place marker items
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
            map.invalidate();
        }
    }
    class UpdateVehiclesTask extends AsyncTask<Collection<TransrockRoute>, Void, ItemizedIconOverlay> {

        @Override
        protected ItemizedIconOverlay doInBackground(Collection<TransrockRoute>... params) {
            // fetch vehicles
            ArrayList<Vehicle> vehicles = new ArrayList<>();
            try {
                for (TransrockRoute route : routes)
                    vehicles.addAll(TransLocAPI.getVehicles(route.agency_id, route.route_id));
            }
            catch (NetworkErrorException e) {

            }
            catch (SocketTimeoutException e) {

            }
            catch (JSONException e) {

            }

            ItemizedIconOverlay<OverlayItem> vehicleOverlay =
                    new ItemizedIconOverlay<OverlayItem>(context, new ArrayList<OverlayItem>(), null);

            // build overlay
            for (Vehicle v : vehicles) {
                GeoPoint loc = new GeoPoint(v.location.firstElement(), v.location.lastElement());
                OverlayItem marker = map.makeMarker(loc, map.defaultMarkerDrawable, v.call_name, v.description);
                vehicleOverlay.addItem(marker);
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
}
