package com.op_dfat;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dawid Wowniuk
 */

class Adapter_Scans extends ArrayAdapter<String> implements Filterable {

    private Context context;
    private Fragment_history fragment;
    private List<String> timestamp;
    private List<String> filtered;
    private FilterScans filter;

    Adapter_Scans(Context context, int layoutResourceId, List<String> timestamp, Fragment_history fragment) {
        super(context, layoutResourceId, timestamp);
        this.timestamp = timestamp;
        this.context = context;
        this.fragment = fragment;
        this.filtered = timestamp;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        View rowView = convertView;
        ViewHolder viewHolder;

        // sets up the references to the Views for later use
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.adapter_scan, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.textView = (TextView) rowView.findViewById(R.id.scanTimeStamp);
            viewHolder.button = (ImageButton) rowView.findViewById(R.id.buttonDeleteScan);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        final String scanTime = getItem(position);

        viewHolder.textView.setText(scanTime);

        // Deletes the scan onClick on the position, with a Dialog with a requested confirmation for the deletion in an alertDialog and refreshes the list
        viewHolder.button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(v.getRootView().getContext()).create();
                alertDialog.setTitle(R.string.adapter_scans_confirmation_title);
                alertDialog.setMessage(context.getResources().getString(R.string.adapter_scans_confirmation));
                alertDialog.setCancelable(true);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteOlderScan(scanTime);
                        notifyDataSetChanged();
                        fragment.getScanList();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });
        viewHolder.button.setFocusable(false);
        viewHolder.button.setFocusableInTouchMode(false);

        return rowView;
    }

    // deletes the scan from the tables
    private void deleteOlderScan (String date) {
        date = Utility.formatSimpleFormatToStandardDateTime(date);
        SqliteDBHelper.getInstance(getContext()).query("DELETE FROM " + SqliteDBStructure.SCAN_TIMES_LOGGED + " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + date + "'");
        SqliteDBHelper.getInstance(getContext()).query("DELETE FROM " + SqliteDBStructure.DATA_ANALYZING + " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + date + "'");
    }

    // returns the item-count from the filtered list, if the user has typed something in the searchBar
    @Override
    public int getCount() {
        return filtered.size();
    }

    // returns the item from the filtered list, if the user has typed something in the searchBar
    @Nullable
    @Override
    public String getItem(int position) {
        return filtered.get(position);
    }

    // gets a filter
    @NonNull
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterScans();
        }
        return filter;
    }

    // sets up the filter itself. It is linked to the timestamp in the list.
    private class FilterScans extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults results = new FilterResults();

            // checks wheather or not the input is null or zero
            if (charSequence != null && charSequence.length() != 0) {
                List<String> resultList = new ArrayList<>();

                // searches for the input in the timestamps and adds to the list
                for (String t: timestamp) {
                    if (t.contains(charSequence.toString())) {
                        resultList.add(t);
                    }
                }

                results.count = resultList.size();
                results.values = resultList;
            } else {
                results.count = timestamp.size();
                results.values = timestamp;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filtered = (List<String>) filterResults.values;

            notifyDataSetChanged();
        }
    }

    // ViewHolder defines the elements that are in a row of the list
    private static class ViewHolder {
        TextView textView;
        ImageButton button;
    }

}
