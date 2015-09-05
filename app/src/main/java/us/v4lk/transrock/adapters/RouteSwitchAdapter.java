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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import us.v4lk.transrock.R;
import us.v4lk.transrock.util.TransrockRoute;
import us.v4lk.transrock.util.Util;

/**
 * Adapts StoredRoute --> layout/route_switch_item.
 * Includes convenience constructor that accepts Route[], and will
 * transform to
 */
public class RouteSwitchAdapter extends ArrayAdapter<TransrockRoute> {

    /**
     * map that maps this adapter's data items to its view selection value
     */
    HashMap<TransrockRoute, Boolean> routesBefore;
    HashMap<TransrockRoute, Boolean> deltas;

    /**
     * @param context application context
     * @param resource resource id for layout of desired list item
     * @param routes collection of routesBefore to return views for
     */
    public RouteSwitchAdapter(Context context, int resource, Collection<? extends TransrockRoute> routes) {
        super(context, resource);

        // make this hashmap a snapshot of the state of routes at the beginning of the interaction
        this.routesBefore = new HashMap<>();
        HashSet<TransrockRoute> storedRoutes = Hawk.get(Util.ROUTES_STORAGE_KEY, new HashSet<TransrockRoute>());
        // for each route passed, check if it's stored and set the appropriate bool in the map
        for (TransrockRoute sr : routes) {
            boolean alreadyStored = storedRoutes.contains(sr);
            this.routesBefore.put(sr, alreadyStored);
        }

        // initialize list of deltas
        deltas = new HashMap<>();

        this.clear();
        this.addAll(this.routesBefore.keySet());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // capture inflater and item
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final TransrockRoute sr = getItem(position);

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

        // set the switch's checked value
        final Switch routeSwitch = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        // if the route selection status has been modified, use the new value; otherwise use old
        boolean checked = deltas.containsKey(sr) ? deltas.get(sr) : routesBefore.get(sr);
        routeSwitch.setChecked(checked);

        // if switch clicked to positive, add item to selected list & remove from deselected
        // if switched to negative, remove from selected list & add to deselected
        routeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update the map
                if (routesBefore.get(sr) == isChecked && deltas.containsKey(sr))
                    deltas.remove(sr);
                else
                    deltas.put(sr, isChecked);
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
    public HashMap<TransrockRoute, Boolean> getDeltas() {
        return deltas;
    }

}
