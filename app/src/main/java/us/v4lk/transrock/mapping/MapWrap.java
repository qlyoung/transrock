package us.v4lk.transrock.mapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.events.MapListener;
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
import java.util.List;
import java.util.Map;

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
    OverlayItem locationMarker;
    /** overlay with all map markersOverlay */
    final ItemizedIconOverlay markersOverlay;
    /** scale bar overlay */
    final ScaleBarOverlay scaleBarOverlay;
    /** route polyline overlays */
    final Map<String, Collection<Polyline>> routeOverlays;

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
        // markersOverlay
        locationMarker = makeMarker(new GeoPoint(0, 0), locationMarkerDrawable);
        // overlays
        markersOverlay = new ItemizedIconOverlay(context, new ArrayList<OverlayItem>(), null );
        markersOverlay.setEnabled(true);
        scaleBarOverlay = new ScaleBarOverlay(context);
        scaleBarOverlay.setEnabled(true);
        map.getOverlays().add(markersOverlay);
        map.getOverlays().add(scaleBarOverlay);
        routeOverlays = new HashMap<>();


        MAP_ZOOM_LEVEL = defaultZoomLevel;
    }

    public static GeoPoint toGeoPoint(Location l) {
        return new GeoPoint(l.getLatitude(), l.getLongitude());
    }

    public OverlayItem putMarkerAt(GeoPoint p, Drawable markerDrawable) {
        OverlayItem markerItem = makeMarker(p, markerDrawable);
        markersOverlay.addItem(markerItem);
        map.invalidate();
        return markerItem;
    }
    public void setScaleBar(boolean on) {
        scaleBarOverlay.setEnabled(on);
    }
    public void setLocationMarkerOn(boolean on) {
        markersOverlay.removeItem(locationMarker);
        if (on)
            markersOverlay.addItem(locationMarker);
    }
    public void setLocationMarkerPosition(GeoPoint p) {
        markersOverlay.removeItem(locationMarker);
        locationMarker = putMarkerAt(p, locationMarkerDrawable);
        map.invalidate();
    }
    public void setLocationMarkerPosition(Location l) {
        setLocationMarkerPosition(toGeoPoint(l));
    }
    public void setLocationMarkerDrawable(Drawable d) {
        locationMarkerDrawable = d;
        locationMarker.setMarker(locationMarkerDrawable);
    }
    public void setDefaultMarkerDrawable(Drawable d) {
        defaultMarkerDrawable = d;
    }
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

    /* overlays */
    public void addOverlay(Overlay... overlays) {
        OverlayManager overlayManager = map.getOverlayManager();
        for (Overlay overlay : overlays)
            overlayManager.add(overlay);
    }
    public void addOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlayManager().addAll(overlays);
    }
    public void removeOverlay(Overlay... overlays) {
        OverlayManager overlayManager = map.getOverlayManager();
        for (Overlay overlay : overlays)
            overlayManager.remove(overlay);
    }
    public void removeOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlayManager().removeAll(overlays);
    }
    public void addRouteOverlay(String route_id, Collection<Polyline> segmentOverlays) {
        routeOverlays.put(route_id, segmentOverlays);
        addOverlay(segmentOverlays);
    }
    public void removeRouteOverlay(String... route_ids) {
        if (routeOverlays.containsKey(route_ids)) {
            for (String id : route_ids) {
                map.getOverlayManager().removeAll(routeOverlays.get(id));
                map.getOverlays().removeAll(routeOverlays.get(id));
            }
        }
    }
    public void removeAllRouteOverlays() {
        for (Collection<Polyline> routeOverlay : routeOverlays.values())
            map.getOverlayManager().removeAll(routeOverlay);

        routeOverlays.clear();
    }

    public void invalidate() { map.invalidate(); }
    public MapView getMapView() {
        return map;
    }
    public Context getContext() { return context; }

    private OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable, String title, String snippet) {
        OverlayItem marker = new OverlayItem(title, snippet, p);
        marker.setMarker(markerDrawable);
        return marker;
    }
    private OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable) {
        return makeMarker(p, markerDrawable, "", "");
    }

}