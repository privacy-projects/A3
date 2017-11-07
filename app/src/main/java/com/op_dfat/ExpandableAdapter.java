package com.op_dfat;

import android.content.Context;
import android.database.DatabaseUtils;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import java.util.ArrayList;
import java.util.List;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;

/**
 * Created by Arrigo Paterno, Dawid Wowniuk
 */

class ExpandableAdapter extends BaseExpandableListAdapter implements Filterable{

    // lists which hold all the information about the scanned apps
    private List<AppInfo> appList;
    private List<AppInfo> filteredAppList;

    private Context context;
    private SqliteDBHelper sqliteDBHelper;

    // references to all the necessary view components
    private ViewSwitcher viewSwitcher;
    private ListView listView;
    private EditText reportReason;
    private Button reportButton;
    private TextView reportTitle, reportExplanationText;
    private Filter filter;

    // map, which assigns a list of check box indices values to a group position
    //private Map<Integer, List<String>> cbIndices;
    private SparseArray<List<String>> cbIndices;

    ExpandableAdapter(Context context, List<AppInfo> appList, Activity_apps.ReportView reportView) {
        // assign values
        this.context = context;
        this.appList = appList;
        this.filteredAppList = this.appList;
        this.viewSwitcher = reportView.viewSwitcher;
        this.listView = reportView.listView;
        this.reportReason = reportView.reportReasonEditText;
        this.reportButton = reportView.reportButton;
        this.reportTitle = reportView.reportTitle;
        this.reportExplanationText = reportView.reportExplanationText;
        sqliteDBHelper = SqliteDBHelper.getInstance(context);
        // initialize the map of check box indices
        initializeIndices();
    }

    @SuppressWarnings("unchecked")
    private void initializeIndices () {
        // create a new map, if null
        if (cbIndices == null)
            cbIndices = new SparseArray(getGroupCount());

        // create a group position - array list pair for each group item (app) in the view
        for (int i = 0; i < getGroupCount(); i++) {
            cbIndices.put(i, new ArrayList<String>());
        }
    }

    @Override
    public int getGroupCount() {
        return filteredAppList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return filteredAppList.get(i).permissionInfos.size();
    }

    @Override
    public Object getGroup(int i) {
        return filteredAppList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return filteredAppList.get(i).permissionInfos.get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        clearIndicesListAtPosition(groupPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        clearIndicesListAtPosition(groupPosition);
    }

    private void clearIndicesListAtPosition(int groupPosition) {
        // get the list of indices from key: group position and clear it
        List<String> indices = cbIndices.get(groupPosition);
        indices.clear();
        cbIndices.setValueAt(groupPosition, indices);
    }

    void updateAppList(List<AppInfo> appInfos) {
        this.appList = appInfos;
        this.filteredAppList = this.appList;
    }

    @Override
    public View getGroupView(final int i, final boolean expanded, View view, final ViewGroup viewGroup) {
        // create a view holder object which stores all the references to view components and the position of the group in the view
        final ViewHolderParent viewHolder;
        // get the app info of this group item
        final AppInfo app = (AppInfo) getGroup(i);

        if (view == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_apps, null);
            // setup view holder
            viewHolder = new ViewHolderParent();

            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.appIcon);
            viewHolder.tvName = (TextView) view.findViewById(R.id.appName);
            viewHolder.tvCount = (TextView) view.findViewById(R.id.permissionCount);
            viewHolder.ivAnomalyIcon = (ImageView) view.findViewById(R.id.appAnomalyIcon);
            viewHolder.anomalyIcon = context.getDrawable(R.drawable.anomalyicon);
            viewHolder.reportButton = (Button) view.findViewById(R.id.reportButton);

            // button listener for when the button has been pressed
            viewHolder.reportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    reportTitle.setText(R.string.massReportResourceDialogTitle);
                    reportExplanationText.setText(R.string.massReportResourceDialogText);
                    reportButton.setText(R.string.buttonReport);
                    // setup the report app view
                    setupReportAppView(viewHolder.position);
                }
            });

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderParent) view.getTag();
        }

        // reset the position of the group item
        viewHolder.position = i;

        // set the view components
        if (app != null) {
            viewHolder.ivIcon.setImageDrawable(app.appIcon);
            viewHolder.tvName.setText(app.appName);
            String permissionsUsed = (app.permisionsUsed > 1)? app.permisionsUsed + " " + context.getResources().getString(R.string.expandableAdapter_permissions_used):
                                                                app.permisionsUsed + " " + context.getResources().getString(R.string.expandableAdapter_permission_used);
            viewHolder.tvCount.setText(permissionsUsed);

            // sets a warning icon when a anomaly is thrown for the app
            if (app.anomalyFound == 1){
                viewHolder.tvName.setTextColor(RED);
                viewHolder.ivAnomalyIcon.setImageDrawable(viewHolder.anomalyIcon);
            } else {
                viewHolder.tvName.setTextColor(BLACK);
                viewHolder.ivAnomalyIcon.setImageDrawable(null);
            }
        }
        // set the visibility of the report button
        if (!expanded) {
            viewHolder.reportButton.setVisibility(View.GONE);
        } else {
            viewHolder.reportButton.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public View getChildView(final int i, final int i1, final boolean lastChild, final View view, final ViewGroup viewGroup) {
        // create a view holder for this child group object
        View rowView = view;
        final ViewHolderChild viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.adapter_app_details, null);
            // setup view holder
            viewHolder = new ViewHolderChild();

            viewHolder.tvCount = (TextView) rowView.findViewById(R.id.resourceCount);
            viewHolder.tvResource = (TextView) rowView.findViewById(R.id.resource);
            viewHolder.cbSelected = (CheckBox) rowView.findViewById(R.id.checkboxMassReport);
            viewHolder.cbSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewHolder.cbSelected.isChecked()) {
                        addIndexToList(viewHolder.parentPosition, viewHolder.childPosition);
                    } else {
                        removeIndexFromList(viewHolder.parentPosition, viewHolder.childPosition);
                    }
                }
            });

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderChild) rowView.getTag();
        }

        PermissionInfo permissionInfo = (PermissionInfo) getChild(i, i1);

        viewHolder.parentPosition = i;
        viewHolder.childPosition = i1;

        viewHolder.cbSelected.setChecked(false);

        // change the resource count and resource name
        String permissionCount = permissionInfo.count + "x";
        viewHolder.tvCount.setText(permissionCount);
        viewHolder.tvResource.setText(permissionInfo.permission);

        boolean anomalyFound = false;

        List<AnomalyInfo> anomalyInfos = permissionInfo.anomalyInfos;
        for (AnomalyInfo anomalyInfo : anomalyInfos) {
            if (anomalyInfo.isAnomalous.equals("1")) {
                anomalyFound = true;
                break;
            }
        }

        // changes the appearance of the resources and checkboxes depending on whether or not the resources are anomalous, were already reported or dereported
        if (anomalyFound){
            viewHolder.tvResource.setTextColor(RED);
        } else {
            viewHolder.tvResource.setTextColor(BLACK);
        }

        if (alreadyReported(appList.get(viewHolder.parentPosition).packageName, permissionInfo.permission)){
            viewHolder.cbSelected.setAlpha(.5f);
            viewHolder.cbSelected.setClickable(false);
            viewHolder.cbSelected.setEnabled(false);
        } else {
            viewHolder.cbSelected.setAlpha(1);
            viewHolder.cbSelected.setClickable(true);
            viewHolder.cbSelected.setEnabled(true);
        }

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterNames();
        }
        return filter;
    }
    
    private void setupReportAppView(int groupPosition) {
        boolean anythingChecked = false;

        // get the index of the group item...
        // ...and then the corresponding list of checked checkbox indices
        final List<String> listOfCheckedCheckboxes = cbIndices.get(groupPosition);

        // if the list isn't empty, at least one check box has been checked
        if (listOfCheckedCheckboxes != null) {
            anythingChecked = listOfCheckedCheckboxes.size() > 0;
        }

        if (anythingChecked) {
            // get the app info of this group item
            final AppInfo appInfo = filteredAppList.get(groupPosition);
            // and the list permissions this app has used
            final List<PermissionInfo> permissionList = appInfo.permissionInfos;
            // result list for the array adapter
            List<String> result = new ArrayList<>();
            // parse every index in the checkbox indices list to an integer and get the anomaly info list of the permission list at that index
            for (String index: listOfCheckedCheckboxes) {
                int indexParsed = Integer.parseInt(index);
                List<AnomalyInfo> anomalyInfos = permissionList.get(indexParsed).anomalyInfos;
                // add the anomaly infos and the permission to the result list
                for (AnomalyInfo anomalyInfo: anomalyInfos) {
                    result.add(permissionList.get(indexParsed).permission + "\n"
                            + anomalyInfo.accessTime.substring(11) + " \n" +
                            anomalyInfo.anomalyReason);
                }
            }
            // setup the adapter with the result list
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, result);
            // OOSO
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.custom_list_item, result);
            listView.setAdapter(adapter);

            // report the app when the button has been pushed
            reportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reportApp(listOfCheckedCheckboxes, appInfo, permissionList);
                }
            });
            // show the next view
            viewSwitcher.showNext();
        }else{
            Toast.makeText(context, context.getString(R.string.noCheckboxSelectedText), Toast.LENGTH_SHORT).show();
        }
    }

    private void addIndexToList (int groupPosition, int childIndex) {
        List<String> list = cbIndices.get(groupPosition);
        list.add("" + childIndex);
        cbIndices.setValueAt(groupPosition, list);
    }

    private void removeIndexFromList (int groupPosition, int childIndex) {
        List<String> list = cbIndices.get(groupPosition);
        list.remove("" + childIndex);
        cbIndices.setValueAt(groupPosition, list);
    }
    
    private void reportApp(List<String> listOfCheckedCheckboxes, AppInfo appInfo, List<PermissionInfo> permissionList) {
        // get the text from the user
        String reasonForReporting = reportReason.getText().toString();
        // send every anomaly to the server
        for (String index: listOfCheckedCheckboxes) {
            int indexParsed = Integer.parseInt(index);

            if (Utility.networking(context)) {
                MyClient.POST_Anomaly(appInfo.packageName, permissionList.get(indexParsed).permission, reasonForReporting, "1");
                // set this permission access from this app as reported in local DB
                updateReported(appInfo.packageName, permissionList.get(indexParsed).permission);
                Toast.makeText(context, context.getString(R.string.toastTransmissionConfirmation), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
            }
        }
        // reset user input field and switch to the previous view
        reportReason.setText("");
        viewSwitcher.showPrevious();
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    private boolean alreadyReported(String app, String res) {
        ArrayList<String> temp = sqliteDBHelper.query_select_list("SELECT " +
                SqliteDBStructure.REPORTED +
                " FROM " + SqliteDBStructure.DATA_ANALYZING +
                " WHERE " + SqliteDBStructure.PACKAGE_NAME + " = '" + app +
                "' AND " + SqliteDBStructure.PERMISSION + " = " + DatabaseUtils.sqlEscapeString(res) + "");
        return temp.contains("1");
    }

    private void updateReported(String packageName, String permission) {
        SqliteDBHelper.getInstance(context).query("UPDATE " + SqliteDBStructure.DATA_ANALYZING +
                " SET " + SqliteDBStructure.REPORTED + " = 1 WHERE " +
                SqliteDBStructure.PACKAGE_NAME + " = '" + packageName +
                "' AND " + SqliteDBStructure.PERMISSION + " = " + DatabaseUtils.sqlEscapeString(permission) + "");
    }

    // view holder for the group items
    private static class ViewHolderParent {
        ImageView ivIcon;
        TextView tvName;
        TextView tvCount;
        ImageView ivAnomalyIcon;
        Drawable anomalyIcon;
        Button reportButton;
        int position;
    }

    // view holder for the children of the group items
    private static class ViewHolderChild {
        TextView tvCount;
        TextView tvResource;
        CheckBox cbSelected;
        int childPosition, parentPosition;
    }

    // sets up the filter itself. It is linked to the appName in the list.
    private class FilterNames extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults results = new FilterResults();

            // checks wheather or not the input is null or zero
            if (charSequence != null && charSequence.length() != 0) {
                List<AppInfo> resultList = new ArrayList<>();

                // searches for the input in the appList/appName and adds to the list
                for (AppInfo app: appList) {
                    if (app.appName.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        resultList.add(app);
                    }
                }

                results.count = resultList.size();
                results.values = resultList;
            } else {
                results.count = appList.size();
                results.values = appList;
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredAppList = (List<AppInfo>) filterResults.values;

            notifyDataSetChanged();
        }
    }
}
