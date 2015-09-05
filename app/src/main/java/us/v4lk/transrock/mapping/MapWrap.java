package us.v4lk.transrock.mapping;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.bitmap;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;

/**
 * Convenience wrapper for MapView that encapsulates many common
 * tasks into easy-to-use methods!
 */
public class MapWrap {

    /** map zoom level */
    final int MAP_ZOOM_LEVEL = 20;
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
    /** overlay with all map markers */
    final ItemizedIconOverlay markers;
    /** scale bar overlay */
    final ScaleBarOverlay scaleBarOverlay;

    /**
     * @param c context
     * @param mapView the mapview to wrap
     */
    public MapWrap(Context c, MapView mapView) {
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
        // markers
        locationMarker = makeMarker(new GeoPoint(0, 0), locationMarkerDrawable);
        // overlays
        markers = new ItemizedIconOverlay(context, new ArrayList<OverlayItem>(), null );
        markers.setEnabled(true);
        scaleBarOverlay = new ScaleBarOverlay(context);
        scaleBarOverlay.setEnabled(true);
        map.getOverlays().add(markers);
        map.getOverlays().add(scaleBarOverlay);
    }

    public static GeoPoint toGeoPoint(Location l) {
        return new GeoPoint(l.getLatitude(), l.getLongitude());
    }

    public OverlayItem putMarkerAt(GeoPoint p, Drawable markerDrawable) {
        OverlayItem markerItem = makeMarker(p, markerDrawable);
        markers.addItem(markerItem);
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
        markers.removeItem(locationMarker);
        if (on)
            markers.addItem(locationMarker);
        map.invalidate();
    }
    public void setLocationMarkerPosition(GeoPoint p) {
        markers.removeItem(locationMarker);
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

    private OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable, String title, String snippet) {
        OverlayItem marker = new OverlayItem(title, snippet, p);
        marker.setMarker(markerDrawable);
        return marker;
    }
    private OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable) {
        return makeMarker(p, markerDrawable, "", "");
    }
}
