package com.op_dfat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

class Adapter_Apps_to_scan extends ArrayAdapter<Apps> implements Filterable {

    Context context;
    private List<Apps> data;
    private List<Apps> filtered = new ArrayList<>();
    private Filter filter;


    Adapter_Apps_to_scan(Context context, int layoutResourceId, List<Apps> data){
        super(context, layoutResourceId);
        this.context = context;
        this.data = data;
        this.filtered = this.data;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View rowView = convertView;
        final ViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.adapter_apps_to_scan, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.iv = (ImageView) rowView.findViewById(R.id.appToScanIcon);
            viewHolder.tv = (TextView) rowView.findViewById(R.id.appToScanName);
            viewHolder.cb = (CheckBox) rowView.findViewById(R.id.apptoScanCheckbox);

            // checks if the checkboxes are changed and puts the unchecked items in a list to exclude them from the scan
            viewHolder.cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Apps apps = getItem(viewHolder.position);
                    if (apps != null) {
                        if (!viewHolder.cb.isChecked()) {
                            // Add the packageName to the list
                            ExcludedApps.addToExcludedAppsList(apps.packageName);
                        } else {
                            // remove the packageName to the list
                            ExcludedApps.removeFromExcludedAppsList(apps.packageName);
                        }
                    }
                }
            });

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        final Apps app = getItem(position);

        viewHolder.position = position;

        // set the icon and appName for the list item
        if (app != null) {
            viewHolder.iv.setImageDrawable(app.icon);
            viewHolder.tv.setText(app.appName);

            if (ExcludedApps.isAppExcluded(app.packageName))
                viewHolder.cb.setChecked(false);
            else
                viewHolder.cb.setChecked(true);
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
            filter = new FilterLauncherApps();
        }
        return filter;
    }

    private class FilterLauncherApps extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults results = new FilterResults();

            if (charSequence != null && charSequence.length() != 0) {
                List<Apps> resultList = new ArrayList<>();
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

    private static class ViewHolder {
        ImageView iv ;
        TextView tv;
        CheckBox cb;
        int position;
    }
}
