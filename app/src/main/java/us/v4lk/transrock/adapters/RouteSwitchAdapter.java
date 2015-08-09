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

import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.util.Util;

/**
 * Adapts route --> layout/route_switch_item.
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

    // dynamic list of which routes are selected / deselected
    // updated by corresponding view's switch being flipped on / off
    Set<Route> selectedRoutes = new HashSet<>();
    Set<Route> deselectedRoutes = new HashSet<>();

    // stored routes, so we don't have Hawk fetch them from Sqlite every time
    Set<Route> savedRoutes = Hawk.get(Util.ROUTES_STORAGE_KEY, new HashSet<Route>());


    /**
     * @param context application context
     * @param resource resource id for layout of desired list item
     * @param routes array of routes to return views for
     */
    public RouteSwitchAdapter(Context context, int resource, Route[] routes) {
        super(context, resource, routes);
    }

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
        // is in storage and has not been manually deselected by the user
        Switch s = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        boolean selected = selectedRoutes.contains(r);
        boolean deselected = deselectedRoutes.contains(r);
        boolean stored = savedRoutes.contains(r);
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

    /**
     * Gets the routes that the user has manually selected in this list.
     * @return user's manually selected routes
     */
    public Route[] getSelected() {
        return selectedRoutes.toArray(new Route[selectedRoutes.size()]);
    }
    /**
     * Gets the routes that the user has manually deselected in this list.
     * @return user's manually deselected routes
     */
    public Route[] getDeselected() {
        return deselectedRoutes.toArray(new Route[deselectedRoutes.size()]);
    }

}
