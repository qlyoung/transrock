package us.v4lk.transrock.fragments;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import us.v4lk.transrock.R;
import us.v4lk.transrock.util.LocationManager;

/**
 * Created by qly on 8/11/15.
 */
public class MapFragment extends Fragment {

    LocationManager locationManager;
    MapView map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // capture & setup map
        map = (MapView) root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPQUESTOSM);
        map.setMultiTouchControls(true);

        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // location manager
        locationManager = new LocationManager(getActivity());
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
            case R.id.map_menu_center_location:
                centerAndZoomOnLocation();
                break;
        }

        return true;
    }

    public boolean centerAndZoomOnLocation() {
        Location l = locationManager.getLocation();
        if (l != null) {
            GeoPoint center = new GeoPoint(l.getLatitude(), l.getLongitude());
            map.getController().setCenter(center);
            map.getController().setZoom(20);
            return true;
        } else return false;
    }
}