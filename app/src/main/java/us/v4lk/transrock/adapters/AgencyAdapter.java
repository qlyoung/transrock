package us.v4lk.transrock.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import us.v4lk.transrock.R;
import us.v4lk.transrock.transloc.Agency;
import us.v4lk.transrock.util.Util;

/**
 * Adapts agency --> layout/agency_list_item.
 */
public class AgencyAdapter extends ArrayAdapter<Agency> implements StickyListHeadersAdapter {

    ArrayList<Agency> active, local;
    int HEADER_ID_ACTIVE = 0,
        HEADER_ID_LOCAL = 1,
        HEADER_ID_ALL = 2;

    /**
     * This adapter asks for numActive and numLocal agencies, which it uses
     * to setup section headers. Setting either or both to 0 will hide the
     * corresponding section header. The list must be sorted appropriately for
     * these sections to be meaningful, in the following order:
     * active, local, all
     * @param context application context.
     * @param resource list resource id
     * @param agencies array of agencies to adapt
     * @param numActive number of active agencies
     * @param numLocal number of local agencies
     */
    public AgencyAdapter(Context context, int resource, Agency[] agencies, int numActive, int numLocal) {
        super(context, resource, agencies);

        // build list of active & local agencies to reference later for headers
        active = new ArrayList<>(numActive);
        local = new ArrayList<>(numLocal);

        if (agencies != null) {
            int i = 0;
            for (int j = 0; j < numActive; j++)
                active.add(agencies[i++]);
            for (int j = 0; j < numLocal; j++)
                local.add(agencies[i++]);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // capture inflater
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // inflate new view if necessary
        if (convertView == null)
            convertView = inflater.inflate(R.layout.agency_list_item, null);

        // capture badge and text views
        ImageView badge = (ImageView) convertView.findViewById(R.id.agency_list_item_badge);
        TextView text = (TextView) convertView.findViewById(R.id.agency_list_item_text);

        // set badge and text views
        int color = getContext().getResources().getColor(R.color.agency_badge);
        badge.setImageBitmap(Util.colorToBitmap(color, 50, 50));
        text.setText(getItem(position).long_name);

        // tag list item with the object it is sourced from
        convertView.setTag(getItem(position));

        return convertView;
    }

    /**
     * Gets a unique number identifying the header corresponding to the item at the given position.
     * @param position the position of the item whose header id to return
     * @return an arbitrary unique integer identifying the header
     */
    @Override
    public long getHeaderId(int position) {
        Agency agency = getItem(position);

        // determine appropriate id for given position
        int id;
        if (active.contains(agency))
           id = HEADER_ID_ACTIVE;
        else if (local.contains(agency))
            id = HEADER_ID_LOCAL;
        else
            id = HEADER_ID_ALL;

        return id;
    }
    /**
     * Gets the corresponding header view for the item at the given position.
     * This implementation does not recycle views since there are only 3 list sections.
     * @param position the position of the item whose header to return
     * @param view the recycled header
     * @param viewGroup the ViewGroup the returned view will be attached to
     * @return the header view for the specified item
     */
    @Override
    public View getHeaderView(int position, View view, ViewGroup viewGroup) {
        // capture inflater
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // capture item
        Agency agency = getItem(position);

        // inflate section header
        view = inflater.inflate(R.layout.agency_section_header, null);
        TextView header = (TextView) view.findViewById(R.id.agency_section_header_text);

        // set appropriate header text
        if (active.contains(agency))
            header.setText(R.string.active);
        else if (local.contains(agency))
            header.setText(R.string.local);
        else
            header.setText(R.string.all);

        return view;
    }
}
