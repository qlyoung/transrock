package us.v4lk.transrock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.util.RouteStorage;

/**
 * Adapts StoredRoute --> layout/route_switch_item.
 * Includes convenience constructor that accepts Route[], and will
 * transform to
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

    HashMap<Route, Boolean> routesBefore;
    HashMap<Route, Boolean> deltas;

    /**
     * @param context application context
     * @param resource resource id for layout of desired list item
     * @param routes collection of routesBefore to return views for
     */
    public RouteSwitchAdapter(Context context, int resource, Route[] routes) {
        super(context, resource);

        // make this hashmap a snapshot of the state of routes at the beginning of the interaction
        this.routesBefore = new HashMap<>();

        // for each route passed, check if it's stored and set the appropriate bool in the map
        for (Route route : routes)
            this.routesBefore.put(route, RouteStorage.contains(route.route_id));

        // initialize list of deltas
        deltas = new HashMap<>();

        this.clear();
        this.addAll(routes);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // capture inflater and item
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final Route route = getItem(position);

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
        longName.setText(route.long_name);

        // set the switch's checked value
        final Switch routeSwitch = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        // if the route selection status has been modified, use the new value; otherwise use old
        boolean checked = deltas.containsKey(route) ? deltas.get(route) : routesBefore.get(route);
        routeSwitch.setChecked(checked);

        // if switch clicked to positive, add item to selected list & remove from deselected
        // if switched to negative, remove from selected list & add to deselected
        routeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update the map
                if (routesBefore.get(route) == isChecked && deltas.containsKey(route))
                    deltas.remove(route);
                else
                    deltas.put(route, isChecked);
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
     * @return all routes whose selection status have been changed from their original value
     */
    public Map<Route, Boolean> getDeltas() {
        return deltas;
    }

}
