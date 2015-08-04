package us.v4lk.transrock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.util.Storage;

/**
 * Adapts route --> layout/route_switch_item.
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

    // dynamic list of which routes are selected,
    // updated by corresponding view's switch being
    // flipped on / off
    Set<Route> selectedRoutes = new HashSet<>();
    Set<Route> deselectedRoutes = new HashSet<>();

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

        // set the switch's checked value to checked if the route has been selected, or if it
        // is in storage and is not scheduled for removal
        Switch s = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        boolean selected = selectedRoutes.contains(r);
        boolean deselected = deselectedRoutes.contains(r);
        boolean stored = Storage.contains(r, getContext());
        boolean checked = selected || (stored && !deselected);
        s.setChecked(checked);

        // if switch clicked to positive, add item to selected list & remove from deselected
        // if switched to negative, remove from selected list & add to deselected
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedRoutes.add(r);
                    deselectedRoutes.remove(r);
                }
                else {
                    selectedRoutes.remove(r);
                    deselectedRoutes.add(r);
                }
            }
        });

        // set convert view to clickable and to toggle switch when clicked
        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v.findViewById(R.id.route_switch_item_switch);
                s.toggle();
            }
        });

        return convertView;
    }

    public Route[] getSelected() {
        return selectedRoutes.toArray(new Route[selectedRoutes.size()]);
    }
    public Route[] getDeselected() {
        return deselectedRoutes.toArray(new Route[deselectedRoutes.size()]);
    }

}
