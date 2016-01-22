package us.v4lk.transrock.mapping;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;

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

/**
 * Wrapper for MapView.
 * Acts as an interface between view and model.
 * Streamlines common tasks for manipulating the map programatically.
 *
 * @author Quentin Young
 */
public class Map {

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

    final ItemizedIconOverlay<OverlayItem> locationMarkerOverlay;
    final ScaleBarOverlay scaleBarOverlay;
    Collection<Polyline> routeOverlays;
    ItemizedIconOverlay<OverlayItem> stopOverlay, vehiclesOverlay;

    /**
     * @param c context
     * @param mapView the mapview to wrap
     */
    public Map(Context c, MapView mapView) {
        this(c, mapView, 17);
    }
    public Map(Context c, MapView mapView, int defaultZoomLevel) {
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

        // location marker
        locationMarkerOverlay = new ItemizedIconOverlay<>(new ArrayList<OverlayItem>(), locationMarkerDrawable, null, resourceProxy);
        setLocationMarkerPosition(new GeoPoint(0, 0));
        map.getOverlayManager().add(locationMarkerOverlay);

        // routes
        routeOverlays = new ArrayList<>();

        MAP_ZOOM_LEVEL = defaultZoomLevel;
    }

    public static GeoPoint toGeoPoint(Location l) {
        return new GeoPoint(l.getLatitude(), l.getLongitude());
    }
    public void setScaleBar(boolean on) {
        scaleBarOverlay.setEnabled(on);
    }
    public void setLocationMarkerOn(boolean on) {
        locationMarkerOverlay.setEnabled(on);
    }
    public void setLocationMarkerPosition(GeoPoint p) {
        locationMarkerOverlay.removeAllItems();
        OverlayItem locationMarker = makeMarker(p, locationMarkerDrawable);
        locationMarkerOverlay.addItem(locationMarker);
    }
    public void setLocationMarkerPosition(Location l) {
        setLocationMarkerPosition(toGeoPoint(l));
    }
    public void setLocationMarkerDrawable(Drawable d) {
        locationMarkerDrawable = d;
        locationMarkerOverlay.getItem(0).setMarker(locationMarkerDrawable);
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
    public void invalidate() { map.invalidate(); }
    public MapView getMapView() {
        return map;
    }
    public Context getContext() { return context; }

    /* overlays */
    private void addOverlay(Overlay... overlays) {
        OverlayManager overlayManager = map.getOverlayManager();
        for (Overlay overlay : overlays)
            overlayManager.add(overlay);
    }
    private void addOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlayManager().addAll(overlays);
    }
    private void removeOverlay(Overlay... overlays) {
        OverlayManager overlayManager = map.getOverlayManager();
        for (Overlay overlay : overlays)
            overlayManager.remove(overlay);
    }
    private void removeOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlayManager().removeAll(overlays);
    }

    public void setRoutesOverlay(Collection<Polyline> segments) {
        clearRouteOverlays();
        routeOverlays = segments;
        addOverlay(segments);
    }
    public void setStopsOverlay(ItemizedIconOverlay stopOverlay) {
        clearStopsOverlay();
        this.stopOverlay = stopOverlay;
        addOverlay(this.stopOverlay);
    }
    public void setVehiclesOverlay(ItemizedIconOverlay vehicles) {
        clearVehiclesOverlay();
        this.vehiclesOverlay = vehicles;
        addOverlay(vehiclesOverlay);
    }
    private void clearRouteOverlays() {
        removeOverlay(routeOverlays);
        routeOverlays.clear();
    }
    private void clearStopsOverlay() {
        removeOverlay(this.stopOverlay);
        this.stopOverlay = null;
    }
    private void clearVehiclesOverlay() {
        removeOverlay(vehiclesOverlay);
        vehiclesOverlay = null;
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