package us.v4lk.transrock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.util.StoredRoute;
import us.v4lk.transrock.util.Util;

/**
 * Adapts route --> layout/route_switch_item.
 */
public class RouteSwitchAdapter extends ArrayAdapter<StoredRoute> {

    // map that maps this adapter's data items to its view selection value
    HashMap<StoredRoute, Boolean> routes;

    /**
     * @param context application context
     * @param resource resource id for layout of desired list item
     * @param routes array of routes to return views for
     */
    public RouteSwitchAdapter(Context context, int resource, Route[] routes) {
        super(context, resource);

        // for each route passed, check if we have a stored route, and if so add that one
        HashSet<StoredRoute> storedRoutes = Hawk.get(Util.ROUTES_STORAGE_KEY, new HashSet<StoredRoute>());
        this.routes = new HashMap<>();
        for (Route r : routes) {
            boolean foundStored = false;
            for (StoredRoute sr : storedRoutes) {
                if (sr.getRoute().hashCode() == r.hashCode()) {
                    this.routes.put(sr, true);
                    foundStored = true;
                    break;
                }
            }
            if (!foundStored) // otherwise, wrap the network-fetched route and add it
                this.routes.put(new StoredRoute(r, false), false);
        }

        this.clear();
        this.addAll(this.routes.keySet());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // capture inflater and item
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final StoredRoute sr = getItem(position);

        // if this view is not a recycled view, inflate a new one
        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_switch_item, null);
        // otherwise, clean it out
        else {
            TextView name = (TextView) convertView.findViewById(R.id.route_switch_item_text);
            name.setText(null);

            // clear listener from switch so we can set initial value without initiating random shit
            Switch s = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
            s.setOnCheckedChangeListener(null);
        }

        // grab the text view and set it to the item name
        TextView longName = (TextView) convertView.findViewById(R.id.route_switch_item_text);
        longName.setText(sr.getRoute().long_name);

        // set the switch's checked value to checked if the route has been selected, or if it
        // is in storage and has not been manually deselected by the user
        final Switch routeSwitch = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        routeSwitch.setChecked(routes.get(sr));

        // if switch clicked to positive, add item to selected list & remove from deselected
        // if switched to negative, remove from selected list & add to deselected
        routeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update the map
                routes.put(sr, isChecked);
            }
        });

        // set convert view to clickable and to toggle switch when clicked
        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSwitch.toggle();
            }
        });

        return convertView;
    }

    /**
     * @return the map of routes to selection value
     */
    public HashMap<StoredRoute, Boolean> getResults() {
        return routes;
    }

}
