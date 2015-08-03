package us.v4lk.transrock.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;

/**
 * Adapts route --> layout/route_switch_item.
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

    // dynamic list of which routes are selected,
    // updated by corresponding view's switch being
    // flipped on / off
    Set<Route> selectedRoutes = new HashSet<>();

    /**
     * Boilerplate adapter constructor.
     * @param context application context.
     * @param resource list resource id
     * @param routes array of routes to return views for
     */
    public RouteSwitchAdapter(Context context, int resource, Route[] routes) {
        super(context, resource, routes);
    }

    /**
     * Rturn a view for the nth item in the source collection
     * @param position the position of the item in the array
     * @param convertView view to inflate into; may be null
     * @param parent parent that this view will eventually be attached to; may be null
     * @return a view representing the nth item in the source collection
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // capture inflater and item
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final Route r = getItem(position);

        // if this view is not a recycled view, inflate a new one
        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_switch_item, null);
        // otherwise, clean it out
        else {
            TextView name = (TextView) convertView.findViewById(R.id.route_switch_item_text);
            name.setText(null);

            Switch s = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
            s.setOnCheckedChangeListener(null);
        }

        // grab the text view and set it to the item name
        TextView name = (TextView) convertView.findViewById(R.id.route_switch_item_text);
        name.setText(r.long_name);

        // set the switch's checked value to correspond with the item's selection value
        Switch s = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        s.setChecked(selectedRoutes.contains(r));

        // if switch clicked to positive, add item to selected list; if switched
        // to negative, remove from selected list
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    selectedRoutes.add(r);
                else
                    selectedRoutes.remove(r);
            }
        });

        return convertView;
    }

    public Route[] getSelected() {
        return selectedRoutes.toArray(new Route[selectedRoutes.size()]);
    }

}
