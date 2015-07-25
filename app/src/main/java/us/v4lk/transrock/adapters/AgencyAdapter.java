package us.v4lk.transrock.adapters;

import android.content.Context;
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

    private int numActive, numLocal;

    /**
     * Boilerplate adapter constructor.
     *
     * This adapter asks for numActive and numLocal agencies, which it uses
     * to place section headers at the appropriate place. Setting either or both
     * to <=0 will hide the corresponding section header. The list must be sorted
     * appropriately for these sections to be meaningful.
     *
     * @param context application context.
     * @param resource list resource id
     * @param agencies array of agencies to return views for
     * @param numActive number of active agencies
     * @param numLocal number of local agencies
     */
    public AgencyAdapter(Context context, int resource, Agency[] agencies, int numActive, int numLocal) {
        super(context, resource, agencies);
        this.numActive = Math.max(numActive, 0);
        this.numLocal = Math.max(numLocal, 0);
    }

    /**
     * Return a view for the nth item in the source collection
     * @param position the position of the item in the array
     * @param convertView view to inflate into; may be null
     * @param parent parent that this view will eventually be attached to; may be null
     * @return a view representing the nth item in the source collection
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // capture inflater
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // inflate new view if necessary
        if (convertView == null)
            convertView = inflater.inflate(R.layout.agency_list_item, null);

        // make sure the header is set to GONE by default since listview recycles views
        View header = convertView.findViewById(R.id.sectionheader);
        header.setVisibility(View.GONE);

        // cue disgusting
        if (position == 0) {
            if (numActive > 0) {
                ((TextView) header.findViewById(R.id.sectionheader_label)).setText(R.string.active);
                header.setVisibility(View.VISIBLE);
            }
            else if (numLocal > 0) {
                ((TextView) header.findViewById(R.id.sectionheader_label)).setText(R.string.local);
                header.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (numActive > 0 && numLocal > 0 && position == numActive) {
                ((TextView) header.findViewById(R.id.sectionheader_label)).setText(R.string.local);
                header.setVisibility(View.VISIBLE);
            }

            if ((numLocal > 0 || numActive > 0) && position == numLocal + numActive ) {
                ((TextView) header.findViewById(R.id.sectionheader_label)).setText(R.string.all);
                header.setVisibility(View.VISIBLE);
            }
        }

        // capture badge and text views
        ImageView badge = (ImageView) convertView.findViewById(R.id.agency_list_item_badge);
        TextView text = (TextView) convertView.findViewById(R.id.agency_list_item_text);

        // set badge and text views
        Drawable d = getContext().getResources().getDrawable(R.drawable.bluesquare);
        badge.setImageDrawable(d);
        text.setText(getItem(position).long_name);

        // tag list item with the object it is sourced from
        convertView.setTag(getItem(position));

        return convertView;
    }

}
