package us.v4lk.transrock.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Agency;

/**
 * Adapts agency --> layout/agency_list_item.
 */
public class AgencyAdapter extends ArrayAdapter<Agency> {

    /**
     * Boilerplate adapter constructor.
     * @param context application context.
     * @param resource list resource id
     * @param agencies array of agencies to return views for
     */
    public AgencyAdapter(Context context, int resource, Agency[] agencies) {
        super(context, resource, agencies);
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
            convertView = inflater.inflate(R.layout.agency_list_item, null);

        ImageView badge = (ImageView) convertView.findViewById(R.id.agency_list_item_badge);
        TextView title = (TextView) convertView.findViewById(R.id.agency_list_item_title);

        Drawable d = getContext().getResources().getDrawable(R.drawable.bluesquare);
        badge.setImageDrawable(d);
        title.setText(getItem(position).long_name);

        convertView.setTag(getItem(position));

        return convertView;
    }

}
