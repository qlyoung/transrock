package us.v4lk.transrock.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import us.v4lk.transrock.R;
import us.v4lk.transrock.util.StoredRoute;
import us.v4lk.transrock.util.Util;

/**
 * Adapts StoredRoute --> layout/route_list_item.
 */
public class StoredRouteAdapter extends ArrayAdapter<StoredRoute> {

    /**
     * @param context application context.
     * @param resource resource id for layout of desired list item
     */
    public StoredRouteAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final StoredRoute storedRoute = getItem(position);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_list_item, null);
        else {
            // clear listener from switch so we can set initial value without initiating random shit
            Switch routeActivationSwitch = (Switch) convertView.findViewById(R.id.route_list_item_switch);
            routeActivationSwitch.setOnCheckedChangeListener(null);
        }

        // badge
        CircularImageView badge = (CircularImageView) convertView.findViewById(R.id.route_list_item_badge);
        Bitmap color = Util.colorToBitmap(Color.parseColor("#" + storedRoute.getRoute().color), 50, 50);
        badge.setImageBitmap(color);

        // long name
        TextView longName = (TextView) convertView.findViewById(R.id.route_list_item_longname);
        longName.setText(storedRoute.getRoute().long_name);

        // short name if present (hidden otherwise)
        TextView shortName = (TextView) convertView.findViewById(R.id.route_list_item_shortname);
        String sn = storedRoute.getRoute().short_name;
        if (!sn.isEmpty())
            shortName.setText(sn);
        else
            shortName.setVisibility(View.GONE);

        // activation switch
        final Switch routeActivationSwitch = (Switch) convertView.findViewById(R.id.route_list_item_switch);
        routeActivationSwitch.setChecked(storedRoute.isActive());
        routeActivationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                storedRoute.setActive(isChecked);
            }
        });

        // set convert view to clickable and to toggle switch when clicked
        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeActivationSwitch.toggle();
            }
        });

        convertView.setTag(storedRoute);

        return convertView;
    }

}
