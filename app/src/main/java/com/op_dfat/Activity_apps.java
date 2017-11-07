package com.op_dfat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Dawid Wowniuk
 * DB methods by Marvin Duchmann
 */

public class Activity_apps extends AppCompatActivity {

    private SearchView sv;
    private Filter filter;
    private ExpandableListView expandableListView;

    private ViewSwitcher viewSwitcher;
    private ListView listView;
    private EditText reportReasonEditText;
    private Button reportButton;
    private TextView detailedResultTitle, reportExplanationText;

    private SqliteDBHelper sqliteDBHelper;
    private ProgressDialog progressDialog;
    private ReportView reportView;

    private int finished = 0;
    private final int MaxBatchSize = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // get references to all necessary view components
        sv = (SearchView) findViewById(R.id.searchViewApps);
        sqliteDBHelper = SqliteDBHelper.getInstance(this);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.ViewSwitcher);
        expandableListView = (ExpandableListView) findViewById(R.id.listViewApps);
        listView = (ListView) findViewById(R.id.listViewDetail);
        reportReasonEditText = (EditText) findViewById(R.id.reportText);
        reportButton = (Button) findViewById(R.id.sendReport);
        Button cancelButton = (Button) findViewById(R.id.cancelReport);
        detailedResultTitle = (TextView) findViewById(R.id.detailtedResultTitle);
        reportExplanationText = (TextView) findViewById(R.id.reportExplanation);

        // setup a object, which contains all view components that are needed in the adapter class
        reportView = new ReportView(viewSwitcher, listView, detailedResultTitle, reportExplanationText, reportButton, reportReasonEditText);

        // collapse all view bodes which are currently open, when a report has been canceled and return to the previous view
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseAllGroups();
                viewSwitcher.showPrevious();
            }
        });

        // get the scan time from the send intent
        Intent intent = getIntent();
        String scanTime = intent.getStringExtra(MyService.STOP_SCAN_TIME_STRING);

        // create the list of apps
        createList(scanTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void collapseAllGroups() {
        for (int i = 0; i < expandableListView.getCount(); i++) {
            expandableListView.collapseGroup(i);
        }
    }

    @SuppressWarnings("unchecked")
    private void createList(final String scanTime) {

        new AsyncTask<Void, Void, List<AppInfo>>() {

            @Override
            protected void onPreExecute() {
                // show a progress dialog
                if (!Activity_apps.this.isFinishing()) {
                    ArrayList<String> apps = getPackageNames(scanTime);
                    int tasks = apps.size()>= MaxBatchSize?apps.size()/MaxBatchSize + 1:1;

                    progressDialog = new ProgressDialog(Activity_apps.this);
                    progressDialog.setMax(tasks);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMessage(getResources().getString(R.string.activity_apps_loading));
                    progressDialog.show();
                }
            }

            @Override
            protected List<AppInfo> doInBackground(Void... voids) {
                // list which will contain the results
                List<AppInfo> resultList = new LinkedList<>();
                // get all the apps which have been tracked during the "scan time" scan
                ArrayList<String> apps = getPackageNames(scanTime);
                // calculate the tasks (amount of threads) according to the fixed batch size per task (thread)
                int tasks = apps.size()>= MaxBatchSize?apps.size()/MaxBatchSize + 1:1;
                finished = 0;
                // create a thread pool with "tasks" threads (one thread per task and each thread handles MaxBatchSize DB entries)
                ExecutorService executorService = Executors.newFixedThreadPool(tasks);
                // create a list to hold all the necessary tasks
                List<Callable<List<AppInfo>>> taskList = new LinkedList<>();

                // create "tasks" amount of tasks and add them to the task list
                for (int i = 0; i < tasks; i++) {
                    taskList.add(new AppLoaderHandler(scanTime, i*MaxBatchSize, MaxBatchSize));
                }

                try {
                    // invoke all the tasks stored in task list
                    List<Future<List<AppInfo>>> results = executorService.invokeAll(taskList);
                    // every task returns a list of app infos which will be added to the final result list
                    for (Future future: results) {
                        List<AppInfo> appInfos = (List<AppInfo>)future.get();
                        resultList.addAll(appInfos);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                // finally sort the result list by name and anomaly appearance and return it
                Collections.sort(resultList, AppInfo.AppComparator.decending(AppInfo.AppComparator.getComparator(AppInfo.AppComparator.SORT_BY_ANOMALY, AppInfo.AppComparator.SORT_BY_NAME)));
                return resultList;
            }

            @Override
            protected void onPostExecute(final List<AppInfo> appsList) {
                // create and set an expandable adapter and pass in the result list and the report view which contains references to all the necessary view components
                final ExpandableAdapter adapter = new ExpandableAdapter(getApplicationContext(), appsList, reportView);
                expandableListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                // dismiss the progress dialog
                if (progressDialog != null)
                    progressDialog.dismiss();
                // disables the default TextFilter
                expandableListView.setTextFilterEnabled(false);
                // gets filter for the adapter
                filter = adapter.getFilter();
                // sets up the searchView
                setupSearchView();
                // listener, which toggles the report button between visible/invisible defending on its current visibility
                expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                        Button button = (Button) view.findViewById(R.id.reportButton);
                        button.setVisibility(button.getVisibility()== View.VISIBLE ?View.GONE:View.VISIBLE);

                        return false;
                    }
                });

                // listener for the child of a group item
                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(final ExpandableListView expandableListView, View view, final int i, int i1, long l) {

                        // setup text views and buttons accordingly
                        detailedResultTitle.setText(R.string.anomalousResourceDialogTitle);
                        reportExplanationText.setText(R.string.anomalousResourceDeReportText);
                        reportButton.setText(R.string.buttonDeReport);

                        dereportApp(adapter, appsList, scanTime, i, i1);
                        return false;
                    }
                });
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void dereportApp (final ExpandableAdapter expandableAdapter, final List<AppInfo> appsList, final String scanTime, final int groupPosition, int groupChildPosition) {
        boolean anomalous = false;
        // get the app infos of the app at group position
        final AppInfo appInfo = appsList.get(groupPosition);
        // then get the permission infos of the app at groupChildPosition
        final PermissionInfo permissionInfo = appInfo.permissionInfos.get(groupChildPosition);
        // and get the list of anomaly infos of that permission info
        List<AnomalyInfo> anomalyInfos = permissionInfo.anomalyInfos;
        // check every anomaly info whether it was an anomalous permission access or not
        for (AnomalyInfo anomalyInfo: anomalyInfos) {
            if (anomalyInfo.isAnomalous.equals("1")) {
                anomalous = true;
                break;
            }
        }
        // if it was an anomalous access...
        if (anomalous) {
            // ...create a list which will hold all anomaly infos of that permission
            List<String> result = new ArrayList<>();
            // store the permission, its access time and the reason why it is anomalous in the result list
            for (AnomalyInfo anomalyInfo: anomalyInfos) {
                result.add(permissionInfo.permission + "\n" + anomalyInfo.accessTime.substring(11) + " \n" + anomalyInfo.anomalyReason);
            }
            // setup an simple array adapter an pass in the result list
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, result);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_list_item, result);
            listView.setAdapter(adapter);

            // button listener for pressing the report button
            reportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get the string from the edit text, containing the reason why the user thinks this access was not anomalous
                    String reasonForDeReporting = reportReasonEditText.getText().toString();
                    // dereport the app and set is as dereported so it cannot be dereported multiple times
                    dereportApp(appInfo.appName, permissionInfo.permission, reasonForDeReporting);
                    setAsDereported(appInfo.packageName, permissionInfo.permission, scanTime);
                    // collapse all groups
                    collapseAllGroups();
                    // recreate the list with the change
                    for (AnomalyInfo anomalyInfo: permissionInfo.anomalyInfos) {
                        anomalyInfo.isAnomalous = "0";
                    }
                    appInfo.anomalyFound = anomalyFound(appInfo.packageName, scanTime)?1:0;
                    Collections.sort(appsList, AppInfo.AppComparator.decending(AppInfo.AppComparator.getComparator(AppInfo.AppComparator.SORT_BY_ANOMALY, AppInfo.AppComparator.SORT_BY_NAME)));
                    expandableAdapter.updateAppList(appsList);

                    // reset the edit text
                    reportReasonEditText.setText("");
                    // switch to the prevois view
                    viewSwitcher.showPrevious();
                }
            });
            // switch to the dereport app view
            viewSwitcher.showNext();
        }
    }

    //Return aller Paket Namen, die zum Analysezeitpunt "scanTime" in der DB stehen
    private ArrayList getPackageNames(String scanTime){
        return sqliteDBHelper.query_select_list("SELECT " + SqliteDBStructure.PACKAGE_NAME +
                " FROM " + SqliteDBStructure.SCANNED_APPS +
                " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime + "'");
    }

    //Return aller Paket Namen, die zum Analysezeitpunt "scanTime" in der DB stehen
    private ArrayList getPackageNames(String scanTime, int offset, int  limit){
        return sqliteDBHelper.query_select_list("SELECT " + SqliteDBStructure.PACKAGE_NAME +
                " FROM " + SqliteDBStructure.SCANNED_APPS +
                " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime + "'" +
                " LIMIT " + limit + " OFFSET " + offset);
    }

    private int getPermissionsUsed (String packageName, String scanTime) {
        return sqliteDBHelper.entries("SELECT * FROM " + SqliteDBStructure.DATA_ANALYZING +
                " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime + "' AND " + SqliteDBStructure.PACKAGE_NAME + " = '" + packageName + "'" +
                " GROUP BY " + SqliteDBStructure.PERMISSION);
    }

    private boolean anomalyFound(String packageName, String scanTime) {
        ArrayList tmp = sqliteDBHelper.query_select_list("SELECT " + SqliteDBStructure.IS_ANOMALOUS +
                " FROM " + SqliteDBStructure.DATA_ANALYZING +
                " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime + "' AND " + SqliteDBStructure.PACKAGE_NAME + " = '" + packageName + "'");
        return tmp.contains("1");
    }

    private List<PermissionInfo> getAnomalyInfos(String packageName, String scanTime) {

        ArrayList<PermissionInfo> tmp = new ArrayList<>();

        try (Cursor cursor = sqliteDBHelper.get_data("SELECT " + SqliteDBStructure.PERMISSION + ", " + SqliteDBStructure.COUNT +
                " FROM " + SqliteDBStructure.DATA_ANALYZING +
                " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime + "' AND " + SqliteDBStructure.PACKAGE_NAME + " = '" + packageName + "'" +
                " GROUP BY " + SqliteDBStructure.PERMISSION +
                " ORDER BY " + SqliteDBStructure.COUNT + " DESC")) {
            if (cursor != null) {
                cursor.moveToFirst();
            } else {
                tmp.add(new PermissionInfo("Error:No Permissions", "Error:0", null));
                return tmp;
            }

            while (!cursor.isAfterLast()) {

                String permission = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PERMISSION));
                String count = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.COUNT));

                try (Cursor cursor1 = sqliteDBHelper.get_data("SELECT " + SqliteDBStructure.IS_ANOMALOUS + ", " + SqliteDBStructure.ANOMALY_REASON + ", " + SqliteDBStructure.ACCESS_TIME +
                        " FROM " + SqliteDBStructure.DATA_ANALYZING +
                        " WHERE " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime +
                        "' AND " + SqliteDBStructure.PACKAGE_NAME + " = '" + packageName +
                        "' AND " + SqliteDBStructure.PERMISSION + " = " + DatabaseUtils.sqlEscapeString(permission) + "")) {
                    if (cursor1 != null) {
                        cursor1.moveToFirst();
                    }

                    List<AnomalyInfo> anomalyInfos = new ArrayList<>();

                    assert cursor1 != null;
                    while (!cursor1.isAfterLast()) {
                        String isAnomalous = cursor1.getString(cursor1.getColumnIndexOrThrow(SqliteDBStructure.IS_ANOMALOUS));
                        String anomalyReason = cursor1.getString(cursor1.getColumnIndexOrThrow(SqliteDBStructure.ANOMALY_REASON));
                        String accessTime = cursor1.getString(cursor1.getColumnIndexOrThrow(SqliteDBStructure.ACCESS_TIME));

                        anomalyInfos.add(new AnomalyInfo(isAnomalous, anomalyReason, accessTime));

                        cursor1.moveToNext();
                    }

                    tmp.add(new PermissionInfo(permission, count, anomalyInfos));
                }

                cursor.moveToNext();
            }
        }

        return tmp;
    }

    private void setAsDereported(String packageName, String permission, String scanTime) {
        sqliteDBHelper.query("UPDATE " + SqliteDBStructure.DATA_ANALYZING + " SET " + SqliteDBStructure.IS_ANOMALOUS + " = '0' " +
                             "WHERE " + SqliteDBStructure.PACKAGE_NAME + " = '" + packageName + "' AND " + SqliteDBStructure.PERMISSION + " = '" + permission + "' AND " + SqliteDBStructure.SCAN_TIME + " = '" + scanTime + "'");
    }

    private void dereportApp(String packageName, String permission, String reason){
        MyClient.POST_Anomaly(packageName, permission, reason, "-1");
    }

    // Sets up the SearchView
    private void setupSearchView(){
        sv.clearFocus();
        sv.setIconifiedByDefault(false);
        sv.setOnQueryTextListener(
                new android.support.v7.widget.SearchView.OnQueryTextListener(){

                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filter.filter(newText);
                        return true;
                    }
                }
        );
        sv.setSubmitButtonEnabled(false);
        sv.setQueryHint("Search Here");
    }

    // sets up the options in the menue in the action bar
    public void getAppsToScan(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), Activity_Apps_to_scan.class);
        startActivity(intent);
    }

    public void settingsButton(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
    }

    public void getHelp(MenuItem item) {
        ImpressumAndHelp.setupHelpDialog(Activity_apps.this);
    }

    public void getAboutUs(MenuItem item){
        ImpressumAndHelp.setupImpressumDialog(Activity_apps.this);
    }

    // Callable class which will be handled by a single thread
    private class AppLoaderHandler implements Callable<List<AppInfo>> {

        // scan time
        private String scanTime;
        // offset, which indicates the row in DB from where to start and the limit to indicate how many rows
        private int offset, limit;

        AppLoaderHandler(String scanTime, int offset, int limit) {
            this.scanTime = scanTime;
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public List<AppInfo> call() throws Exception {
            return loadApps();
        }

        @SuppressWarnings("unchecked")
        private List<AppInfo> loadApps () {

            // get all the package names that were logged at the given time stamp
            ArrayList<String> apps = getPackageNames(scanTime, offset, limit);

            if (apps == null || apps.isEmpty()) {
                return new ArrayList<>();
            }
            // create an array of app objects, which holds all the necessary information about all the found apps
            final List<AppInfo> appList = new LinkedList<>();

            for (String packageName: apps) {
                Drawable icon = Utility.getAppIcon(getApplicationContext(), packageName);
                String appName = Utility.packageToAppName(getApplicationContext(), packageName);
                List<PermissionInfo> permissionInfos = getAnomalyInfos(packageName, scanTime);

                int anomalyFound = anomalyFound(packageName, scanTime)?1:0;
                int permissionsUsed = getPermissionsUsed(packageName, scanTime);

                AppInfo appInfo = new AppInfo(packageName, appName, icon, anomalyFound, permissionsUsed, permissionInfos);

                if (!appList.contains(appInfo)) {
                    appList.add(appInfo);
                }
            }
            // this thread has finished its work
            progressDialog.setProgress(++finished);
            return appList;
        }
    }

    class ReportView {
        ViewSwitcher viewSwitcher;
        ListView listView;
        TextView reportTitle, reportExplanationText;
        Button reportButton;
        EditText reportReasonEditText;

        ReportView(ViewSwitcher viewSwitcher, ListView listView, TextView reportTitle, TextView reportExplanationText, Button reportButton, EditText reportReasonEditText) {
            this.reportButton = reportButton;
            this.reportExplanationText = reportExplanationText;
            this.reportTitle = reportTitle;
            this.viewSwitcher = viewSwitcher;
            this.listView = listView;
            this.reportReasonEditText = reportReasonEditText;
        }
    }
}
