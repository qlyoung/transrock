package us.v4lk.transrock.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import java.util.Set;

import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.util.Util;

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

        Bitmap color = Util.colorToBitmap(Color.parseColor("#" + item.color), 50, 50);
        badge.setImageBitmap(color);
        longname.setText(item.long_name);
        agencyname.setText(String.valueOf(item.agency_id));

        convertView.setTag(getItem(position));

        return convertView;
    }

}
