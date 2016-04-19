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

import io.realm.Realm;
import us.v4lk.transrock.R;
import us.v4lk.transrock.model.Route;
import us.v4lk.transrock.util.Util;

/**
 * Adapts TransrockRoute --> layout/route_list_item.
 */
public class RouteAdapter extends ArrayAdapter<Route> {

    /** Interfaces between data and views for list of routes in RoutesFragment */
    Realm realm;

    /**
     * @param context application context.
     * @param resource resource id for list item layout
     * @param realm the realm that this adapter's dataset is stored in
     */
    public RouteAdapter(Context context, int resource, Realm realm) {
        super(context, resource);
        this.realm = realm;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Route item = getItem(position);

        // capture layout inflater
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // inflate new view if necessary
        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_list_item, null);
        else {
            // important to null out old listener before changing checked value, otherwise the old listener
            // will get called and the wrong record will be updated!
            Switch routeActivationSwitch = (Switch) convertView.findViewById(R.id.route_list_item_switch);
            routeActivationSwitch.setOnCheckedChangeListener(null);
        }

        // badge
        CircularImageView badge = (CircularImageView) convertView.findViewById(R.id.route_list_item_badge);
        Bitmap color = Util.colorToBitmap(Color.parseColor("#" + item.getColor()), 50, 50);
        badge.setImageBitmap(color);

        // long name
        TextView longName = (TextView) convertView.findViewById(R.id.route_list_item_longname);
        longName.setText(item.getLongName());

        // short name if present (hidden otherwise)
        TextView shortName = (TextView) convertView.findViewById(R.id.route_list_item_shortname);
        String sn = item.getShortName();
        if (!sn.isEmpty())
            shortName.setText(sn);
        else
            shortName.setVisibility(View.GONE);

        // activation switch
        final Switch routeActivationSwitch = (Switch) convertView.findViewById(R.id.route_list_item_switch);
        routeActivationSwitch.setChecked(item.isActivated());
        routeActivationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                item.setActivated(isChecked);
                realm.commitTransaction();
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

        return convertView;
    }

}
