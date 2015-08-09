package us.v4lk.transrock.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;

/**
 * Adapts route --> layout/route_list_item.
 */
public class RouteAdapter extends ArrayAdapter<Route> {

    /**
     * @param context application context.
     * @param resource resource id for layout of desired list item
     */
    public RouteAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Route item = getItem(position);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_list_item, null);

        CircularImageView badge = (CircularImageView) convertView.findViewById(R.id.route_list_item_badge);
        TextView longname = (TextView) convertView.findViewById(R.id.route_list_item_longname);
        TextView agencyname = (TextView) convertView.findViewById(R.id.route_list_item_agency);

        badge.setBackgroundColor(Color.parseColor("#" + item.color));
        longname.setText(item.long_name);
        agencyname.setText(String.valueOf(item.agency_id));

        convertView.setTag(getItem(position));

        return convertView;
    }

}
