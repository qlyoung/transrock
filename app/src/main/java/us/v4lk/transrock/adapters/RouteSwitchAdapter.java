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

import io.realm.Realm;
import us.v4lk.transrock.R;
import us.v4lk.transrock.model.Route;

/**
 * Adapts StoredRoute --> layout/route_switch_item.
 * Includes convenience constructor that accepts RouteModel[].
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

    Realm backingRealm;

    /**
     * @param context application context
     * @param resource resource id for layout of desired list item
     * @param routes array of routes to show in the list, members of the realm passed
     * @param realm the realm that the backing Agency objects are part of
     */
    public RouteSwitchAdapter(Context context, int resource, Route[] routes, Realm realm) {
        super(context, resource);

        this.clear();
        this.addAll(routes);
        this.backingRealm = realm;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Route item = getItem(position);

        // capture inflater and item
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // if this view is not a recycled view, inflate a new one
        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_switch_item, null);
        else {
            // important to null out old listener before changing checked value, otherwise the old listener
            // will get called and the wrong record will be updated!
            Switch routeToggleSwitch = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
            routeToggleSwitch.setOnCheckedChangeListener(null);
        }

        // grab the text view and set it to the item name
        TextView nameTextView = (TextView) convertView.findViewById(R.id.route_switch_item_text);
        nameTextView.setText(item.getLongName());

        // capture switch & set checked state to reflect realm record
        final Switch routeToggleSwitch = (Switch) convertView.findViewById(R.id.route_switch_item_switch);
        routeToggleSwitch.setChecked(item.isSaved());

        // when the switch is toggled, update the corresponding realm record
        routeToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                backingRealm.beginTransaction();
                item.setSaved(isChecked);
                backingRealm.commitTransaction();
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

}
