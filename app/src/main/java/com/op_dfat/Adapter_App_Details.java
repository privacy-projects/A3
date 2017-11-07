package com.op_dfat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;


public class Adapter_App_Details extends ArrayAdapter<PermissionInfo> {

    Context context;
    private int layoutResourceId;
    private List<PermissionInfo> resources;
    private String packageName;
    SqliteDBHelper sqliteDBHelper;

    public Adapter_App_Details(Context context, int layoutResourceId, List<PermissionInfo> resources, String packageName){
        super(context, layoutResourceId, resources);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.resources = resources;
        this.packageName = packageName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.adapter_app_details, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.tvCount = (TextView) rowView.findViewById(R.id.resourceCount);
            viewHolder.tvResource = (TextView) rowView.findViewById(R.id.resource);
            viewHolder.cbSelected = (CheckBox) rowView.findViewById(R.id.checkboxMassReport);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        sqliteDBHelper = SqliteDBHelper.getInstance(context);

        // change the resource count and resource name
        viewHolder.tvCount.setText(resources.get(position).count + "x");
        viewHolder.tvResource.setText(resources.get(position).permission);
        viewHolder.tvResource.setTextColor(BLACK);
        viewHolder.cbSelected.setAlpha(1);
        viewHolder.cbSelected.setClickable(true);
        viewHolder.cbSelected.setEnabled(true);

        if(resources.get(position).anomalyInfos.get(position).isAnomalous.equals("1")){
            viewHolder.tvResource.setTextColor(RED);
        }

        if(alreadyReported(packageName, resources.get(position).permission)){
            viewHolder.cbSelected.setAlpha(.5f);
            viewHolder.cbSelected.setClickable(false);
            viewHolder.cbSelected.setEnabled(false);
        }
        return rowView;
    }

    private boolean alreadyReported(String app, String res) {
        ArrayList<String> temp = sqliteDBHelper.query_select_list("SELECT " + SqliteDBStructure.REPORTED + " FROM " + SqliteDBStructure.DATA_ANALYZING + " WHERE " + SqliteDBStructure.PACKAGE_NAME + " = '" + app + "' AND " + SqliteDBStructure.PERMISSION + " = '" + res + "'");
        return temp.contains("1");
    }

    private static class ViewHolder {
        TextView tvCount;
        TextView tvResource;
        CheckBox cbSelected;
    }
}
