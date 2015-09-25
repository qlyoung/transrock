package us.v4lk.transrock.fragments;

import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;
import com.orhanobut.hawk.Hawk;

import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.mapping.MapWrap;
import us.v4lk.transrock.mapping.Polylines;
import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.TransrockRoute;
import us.v4lk.transrock.util.Util;

/**
 * Map fragment. Draws routes and lets the user move around the map.
 */
public class MapFragment extends Fragment implements LocationListener {

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
        // inflate layout
        root = inflater.inflate(R.layout.fragment_map, container, false);

        // return whatever should be the root of this fragment's view hierarchy
        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // capture & setup map
        mapWrap = new MapWrap(getActivity(), (MapView) root.findViewById(R.id.map));
        mapWrap.setScaleBar(true);
        mapWrap.setLocationMarkerDrawable(getResources().getDrawable(R.drawable.location_marker));
        TouchOverlay touchOverlay = new TouchOverlay(mapWrap.getContext());
        touchOverlay.setEnabled(true);
        mapWrap.getMapView().getOverlays().add(touchOverlay);

        // initialize location manager
        locationManager = new LocationManager(getActivity());

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

        new AddRouteOverlays().execute();
    }
    @Override
    public void onPause() {
        super.onPause();
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

    /**
     * Hacky workaround for all of MapView's touch callbacks
     * being hosed. Overlay whose sole purpose is to do something
     * when it gets touched.
     */
    private class TouchOverlay extends Overlay {
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
    class AddRouteOverlays extends AsyncTask<TransrockRoute, Integer, Overlay[]> {
        @Override
        protected Overlay[] doInBackground(TransrockRoute... params) {

            // get the active routes
            Set<TransrockRoute> storedRoutes = Hawk.get(Util.ROUTES_STORAGE_KEY);
            Set<TransrockRoute> activeRoutes = new HashSet<>();
            for (TransrockRoute route : storedRoutes)
                    if (route.isActive())
                        activeRoutes.add(route);

            // update their segments
            for (TransrockRoute route : activeRoutes) {
                try {
                    Collection<String> segments = TransLocAPI.getSegments(route.getRoute()).values();
                    route.setSegments(segments);
                } catch (Exception e) {
                    Log.e("TransRock", e.getMessage());
                }
            }

            // calculate how many times each segment is reused across all routes
            Map<String, Integer> totalCount = new LinkedHashMap<>(activeRoutes.size());
            for (TransrockRoute route : activeRoutes)
                for (String segment : route.getSegments()) {
                    int prevCount = totalCount.get(segment) == null ? 0 : totalCount.get(segment);
                    totalCount.put(segment, prevCount + 1);
                }

            // build overlays
            ArrayList<Polyline> polylines = new ArrayList<>();
            Map<String, Integer> visitedCount = new LinkedHashMap<>(totalCount.size());
            int basePolylineSize = 10;
            float dashScale = 100;

            for (TransrockRoute route : activeRoutes) {
                for (String segment : route.getSegments() ) {
                    int timesVisited = visitedCount.get(segment) == null ? 0 : visitedCount.get(segment);

                    // get the polyline
                    Polyline p = Polylines.encodedPolylineToOverlay(segment, getActivity());
                    p.setWidth(basePolylineSize);
                    p.setColor(Color.parseColor("#" + route.getRoute().color));

                    // calculate the dash effect
                    float split = 1f / totalCount.get(segment);             // ratio of this line : all other lines
                    float dashOn = split * dashScale;                       // magnitude of 'on' segments
                    float dashOff = dashOn * (totalCount.get(segment) - 1); // magnitude of 'off' segments, which should equal split * lines remaining
                    DashPathEffect dashFect = new DashPathEffect(new float[]{dashOn, dashOff}, timesVisited * dashOn);
                    // set effect
                    p.getPaint().setPathEffect(dashFect);

                    // increment segment's visited count
                    visitedCount.put(segment, timesVisited + 1);

                    // add polyline to list
                    polylines.add(p);
                }
            }

            return polylines.toArray(new Polyline[0]);
        }

        @Override
        protected void onPostExecute(Overlay[] overlays) {
            for (Overlay l : overlays)
                mapWrap.addOverlay(l);
            super.onPostExecute(overlays);
        }
    }
}
