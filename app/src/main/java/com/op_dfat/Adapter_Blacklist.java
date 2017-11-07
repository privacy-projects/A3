package com.op_dfat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dawid Wowniuk
 */

class Adapter_Blacklist extends ArrayAdapter<Apps> implements Filterable {

    private Context context;
    private List<Apps> data;
    private List<Apps> filtered = new ArrayList<>();

    private FilterNames filter;


    Adapter_Blacklist(Context context, int layoutResourceId, List<Apps> data){
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        filtered = this.data;
    }

    // fills the each row  with the wanted views and values
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View rowView = convertView;
        ViewHolder viewHolder;

        // sets up the references to the Views for later use
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.adapter_blacklist, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.ivIcon = (ImageView) rowView.findViewById(R.id.blacklistAppIcon);
            viewHolder.tvName = (TextView) rowView.findViewById(R.id.blacklistAppName);
            viewHolder.tvScore = (TextView) rowView.findViewById(R.id.blacklistAnomalyScore);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        Apps app = getItem(position);

        // resizes icon and sets all, icon, name and score
        if (app != null) {
            Picasso.with(context).load(app.iconURL).resize(50, 50).into(viewHolder.ivIcon);

            viewHolder.tvName.setText(app.appName);
            viewHolder.tvScore.setText(app.score);
        }
        return rowView;
    }

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Nullable
    @Override
    public Apps getItem(int position) {
        return filtered.get(position);
    }


    @NonNull
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterNames();
        }
        return filter;
    }

    private class FilterNames extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults results = new FilterResults();

            // checks wheather or not the input is null or zero
            if (charSequence != null && charSequence.length() != 0) {
                List<Apps> resultList = new ArrayList<>();

                // searches for the input in the Apps appName and adds to the list if found
                for (Apps app: data) {
                    if (app.appName.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        resultList.add(app);
                    }
                }

                results.count = resultList.size();
                results.values = resultList;
            } else {
                results.count = data.size();
                results.values = data;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filtered = (List<Apps>) filterResults.values;

            notifyDataSetChanged();
        }
    }

    // ViewHolder defines the elements that are in a row of the list
    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvScore;
    }
}
