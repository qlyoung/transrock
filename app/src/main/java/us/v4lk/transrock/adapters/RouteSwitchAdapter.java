package us.v4lk.transrock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;

/**
 * Adapts route --> layout/route_switch_item.
 */
public class RouteSwitchAdapter extends ArrayAdapter<Route> {

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
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null)
            convertView = inflater.inflate(R.layout.route_switch_item, null);

        TextView name = (TextView) convertView.findViewById(R.id.route_switch_item_text);
        name.setText(this.getItem(position).long_name);

        convertView.setTag(getItem(position));

        return convertView;
    }

}
