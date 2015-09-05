package us.v4lk.transrock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.util.ArrayList;

/**
 * Created by qly on 9/4/15.
 */
public class MapWrap {

    /** map zoom level */
    final int MAP_ZOOM_LEVEL = 20;
    /** map **/
    MapView map;
    /** context */
    Context context;
    /** various drawables */
    Drawable locationMarkerDrawable, defaultDrawable;
    /** overlay containing location marker */
    Overlay locationMarkerOverlay;
    /** whether the location marker is on */
    boolean locationMarkerOn = true;

    public MapWrap(Context c, MapView mapView) {
        context = c;
        map = mapView;
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setMultiTouchControls(true);
    }

    public void centerAndZoomOnLocation(GeoPoint center, boolean animate) {
        map.getController().setZoom(MAP_ZOOM_LEVEL);
        if (animate)
            map.getController().animateTo(center);
        else
            map.getController().setCenter(center);
    }
    public void centerAndZoomOnLocation(Location l, boolean animate) {
        centerAndZoomOnLocation(toGeoPoint(l), animate);
    }
    public Overlay markerAt(GeoPoint p, Drawable markerDrawable) {
        OverlayItem markerItem = makeMarker(p, markerDrawable);
        Overlay markerOverlay = makeOverlay(markerItem);
        map.getOverlays().add(markerOverlay);
        map.invalidate();
        return markerOverlay;
    }
    public Overlay markerAt(Location l, Drawable markerDrawable) {
        return markerAt(toGeoPoint(l), markerDrawable);
    }
    public void setScaleBar(boolean on) {
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(context);
        map.getOverlays().add(scaleBarOverlay);
        map.invalidate();
    }
    public void setLocationMarkerOn(boolean on) {
        boolean prev = locationMarkerOn;
        locationMarkerOn = on;
        if (locationMarkerOverlay != null)
            locationMarkerOverlay.setEnabled(on);
        if (prev != on)
            map.invalidate();
    }
    public void setLocationMarkerPosition(GeoPoint p) {
        // clear old overlay
        if (locationMarkerOverlay != null)
            map.getOverlays().remove(locationMarkerOverlay);
        // make updated overlay
        locationMarkerOverlay = markerAt(p, locationMarkerDrawable);
        // set enabled status
        locationMarkerOverlay.setEnabled(locationMarkerOn);
        // add to map & invalidate
        map.getOverlays().add(locationMarkerOverlay);
        map.invalidate();
    }
    public void setLocationMarkerPosition(Location l) {
        setLocationMarkerPosition(toGeoPoint(l));
    }
    public void setLocationMarkerDrawable(Drawable d) {
        locationMarkerDrawable = d;
    }
    public void setDefaultMarkerDrawable(Drawable d) {
        defaultDrawable = d;
    }

    private OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable, String title, String snippet) {
        OverlayItem marker = new OverlayItem(title, snippet, p);
        marker.setMarker(markerDrawable);
        return marker;
    }
    private OverlayItem makeMarker(GeoPoint p, Drawable markerDrawable) {
        return makeMarker(p, markerDrawable, "", "");
    }
    private Overlay makeOverlay(OverlayItem... items) {
        ArrayList<OverlayItem> overlays = new ArrayList<>(items.length);
        for (OverlayItem item : items)
            overlays.add(item);
        return new ItemizedIconOverlay(context, overlays, null);
    }


    public static GeoPoint toGeoPoint(Location l) {
        return new GeoPoint(l.getLatitude(), l.getLongitude());
    }
}
