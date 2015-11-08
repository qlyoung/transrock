package us.v4lk.transrock.fragments;

import android.app.Activity;
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.mapping.MapWrap;
import us.v4lk.transrock.mapping.Polylines;
import us.v4lk.transrock.transloc.objects.Stop;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.RouteStorage;
import us.v4lk.transrock.util.TransrockRoute;
import us.v4lk.transrock.util.Util;

/**
 * Map fragment. Draws routes and lets the user move around the map.
 */
public class MapFragment extends Fragment implements LocationListener {

    @BindDrawable(R.drawable.location_marker) Drawable location_marker;
    @BindDrawable(R.drawable.stop_marker) Drawable stop_marker;
    @Bind(R.id.map) MapView map;

    /** location manager */
    LocationManager locationManager;
    /** map wrapper */
    MapWrap mapWrap;
    /** root view */
    View root;
    /** whether to center the map on the user's location */
    boolean followMe = true;


    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, root);
        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // capture & setup map
        mapWrap = new MapWrap(getActivity(), map);
        mapWrap.setLocationMarkerDrawable(location_marker);
        mapWrap.setDefaultMarkerDrawable(stop_marker);
        mapWrap.setScaleBar(true);

        // add follow-me watchdog overlay
        TouchOverlay touchOverlay = new TouchOverlay(mapWrap.getContext());
        touchOverlay.setEnabled(true);
        mapWrap.getMapView().getOverlays().add(touchOverlay);

        // get a reference to the location manager
        locationManager = LocationManager.getInstance(this.getActivity().getApplicationContext());

        // get location updates
        locationManager.addLocationListener(this);
    }
    @Override
    public void onResume() {
        super.onResume();

        // do a proactive call to try to get location directly on resume
        Location l = locationManager.getLocation();
        if (l != null) {
            mapWrap.centerAndZoomOnPosition(l, false);
            mapWrap.setLocationMarkerPosition(l);
            mapWrap.setLocationMarkerOn(true);
        }

        updateOverlays();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // if location button selected, animate & zoom to current location
            case R.id.map_menu_center_location:
                // enable follow-me user on location updates && simulate location update
                followMe = true;
                mapWrap.centerAndZoomOnPosition(locationManager.getLocation(), true);
                break;
        }

        return true;
    }

    /* play services location api callback */
    @Override
    public void onLocationChanged(Location location) {
        mapWrap.setLocationMarkerPosition(location);
        if (followMe)
            mapWrap.centerOnPosition(location, true);
    }

    /* overlays */
    /**
     * Pulls route overlays from storage and adds them to the map.
     */
    public void updateOverlays() {
        mapWrap.removeAllRouteOverlays();
        new AddRouteOverlays().execute();
        new AddStopsOverlay().execute();
        mapWrap.invalidate();
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
            followMe = false;
            return super.onDown(e, mapView);
        }
    }
    /**
     * AsyncTask which fetches and draws segments on the map
     */
    class AddRouteOverlays extends AsyncTask<Void, Integer, Overlay[]> {

        private Collection<TransrockRoute> activeRoutes;

        @Override
        protected void onPreExecute() {
            // do this on the UI thread to avoid synchronization blocking
            activeRoutes = RouteStorage.getActivatedRoutes();
            super.onPreExecute();
        }
        @Override
        protected Overlay[] doInBackground(Void... params) {

            // calculate how many times each segment is reused across all routes
            Map<String, Integer> totalCount = new LinkedHashMap<>(activeRoutes.size());
            for (TransrockRoute route : activeRoutes)
                for (String segment : route.segments) {
                    int prevCount = totalCount.get(segment) == null ? 0 : totalCount.get(segment);
                    totalCount.put(segment, prevCount + 1);
                }

            // build overlays
            ArrayList<Polyline> polylines = new ArrayList<>();
            Map<String, Integer> visitedCount = new LinkedHashMap<>(totalCount.size());
            int basePolylineSize = 10;
            float dashScale = 100;

            for (TransrockRoute route : activeRoutes) {
                for (String segment : route.segments) {
                    int timesVisited = visitedCount.get(segment) == null ? 0 : visitedCount.get(segment);

                    // get the polyline
                    Polyline p = Polylines.encodedPolylineToOverlay(segment, getActivity());
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
                    polylines.add(p);
                }
            }

            return polylines.toArray(new Polyline[polylines.size()]);
        }
        @Override
        protected void onPostExecute(Overlay[] overlays) {
            // add overlays to map
            //TODO: change this to use the new addRouteOverlay method
            //TODO: fix route removal after update bug
            //TODO: figure out how to update after routesfragment route has switch changed
            mapWrap.addOverlay(overlays);
            super.onPostExecute(overlays);
        }
    }
    /** AsyncTask which fetches and draws stops on the map */
    class AddStopsOverlay extends AsyncTask<Void, Integer, Overlay> {

        private Collection<TransrockRoute> activeRoutes;

        @Override
        protected void onPreExecute() {
            // do this on the UI thread to avoid synchronization blocking
            activeRoutes = RouteStorage.getActivatedRoutes();
            super.onPreExecute();
        }

        @Override
        protected Overlay doInBackground(Void... params) {
            // get active routes
            int[] ids = Util.getAgencyIds(activeRoutes);

            // get stops
            Map<String, Stop> stops = null;
            try { stops = TransLocAPI.getStops(ids); }
            catch (Exception e) { /* todo: actual exception handling */ }

            // map each stop to the routes containing it
            Map<Stop, Collection<TransrockRoute>> stopsToRoutes = new LinkedHashMap<>();
            for (TransrockRoute route : activeRoutes) {
                for (String stopId : route.stopIds) {
                    Collection<TransrockRoute> existing = stopsToRoutes.get(stops.get(stopId));
                    if (existing == null)
                        existing = new HashSet<>();
                    existing.add(route);
                    stopsToRoutes.put(stops.get(stopId), existing);
                }
            }

            // single itemized overlay for all stop markers
            Activity e = getActivity();
            ItemizedIconOverlay<OverlayItem> stopsOverlay = new ItemizedIconOverlay<>(getActivity(), new ArrayList<OverlayItem>(), null);
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
                drawables[numRoutes] = getResources().getDrawable(R.drawable.stop_marker); // border
                LayerDrawable stopMarkerDrawable = new LayerDrawable(drawables);

                // make new OverlayItem for this stop
                GeoPoint stopMarkerLocation = new GeoPoint(stop.location.get(0), stop.location.get(1));
                OverlayItem item = new OverlayItem(stop.name, stop.description, stopMarkerLocation);
                item.setMarker(stopMarkerDrawable);

                // add it to the overlay
                stopsOverlay.addItem(item);
            }

            return stopsOverlay;

        }
        @Override
        protected void onPostExecute(Overlay overlay) {
            mapWrap.addOverlay(overlay);
            super.onPostExecute(overlay);
        }
    }
}
