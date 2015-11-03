package us.v4lk.transrock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Map;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.objects.Route;

/**
 * Adapts StoredRoute --> layout/route_switch_item.
 * Includes convenience constructor that accepts Route[].
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

    Map<Route, Boolean> data;

    /**
     * @param context application context
     * @param resource resource id for layout of desired list item
     * @param data route / boolean pair list; booleans determine switch truth value
     */
    public RouteSwitchAdapter(Context context, int resource, Map<Route, Boolean> data ) {
        super(context, resource);

        this.data = data;

        this.clear();
        this.addAll(data.keySet());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // capture inflater and item
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final Route item = getItem(position);

        // if this view is not a recycled view, inflate a new one
        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_switch_item, null);

        // grab the text view and set it to the item name
        TextView nameTextView = (TextView) convertView.findViewById(R.id.route_switch_item_text);
        nameTextView.setText(item.long_name);

        // set the switch's checked value and listener
        final Switch routeToggleSwitch = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        routeToggleSwitch.setOnCheckedChangeListener(null);
        routeToggleSwitch.setChecked(data.get(item));
        routeToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            data.put(item, isChecked);
            }
        });

        // set entire view to clickable & set onclick to switch toggle
        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeToggleSwitch.toggle();
            }
        });

        return convertView;
    }

    /** @return all routes whose selection status have been changed from their original value  */
    public Map<Route, Boolean> getData() { return data; }

}
