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
import org.osmdroid.events.MapListener;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Convenience wrapper for MapView that encapsulates many common
 * tasks into easy-to-use methods!
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
    public OverlayItem putMarkerAt(Location l, Drawable markerDrawable) {
        return putMarkerAt(toGeoPoint(l), markerDrawable);
    }
    public OverlayItem putMarkerAt(GeoPoint p) {
        return putMarkerAt(p, defaultMarkerDrawable);
    }
    public OverlayItem putMarkerAt(Location l) {
        return putMarkerAt(toGeoPoint(l), defaultMarkerDrawable);
    }
    public void setScaleBar(boolean on) {
        scaleBarOverlay.setEnabled(on);
        map.invalidate();
    }
    public void setLocationMarkerOn(boolean on) {
        markersOverlay.removeItem(locationMarker);
        if (on)
            markersOverlay.addItem(locationMarker);
        map.invalidate();
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
    public void setMapListener(MapListener listener) {
        map.setMapListener(listener);
    }
    public void removeMapListener() {
        map.setMapListener(null);
    }
    public void addOverlay(Overlay... overlays) {
        List<Overlay> overlayList = map.getOverlays();
        for (Overlay overlay : overlays)
            overlayList.add(overlay);

        map.invalidate();
    }
    public void addOverlay(Collection<? extends Overlay> overlays) {
        map.getOverlays().addAll(overlays);
        map.invalidate();
    }
    public void removeOverlay(Overlay overlay){
        map.getOverlays().remove(overlay);
        map.invalidate();
    }
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