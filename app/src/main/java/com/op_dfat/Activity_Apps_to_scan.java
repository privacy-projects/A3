package com.op_dfat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Activity_Apps_to_scan extends AppCompatActivity implements LauncherAppsLoader.LauncherAppsListener {

    private ListView listView;
    private SearchView sv;
    private Filter filter;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_to_scan);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // get references to view components
        listView = (ListView) findViewById(R.id.appsToScanListView);
        sv = (SearchView) findViewById(R.id.appsSearch);

        // subscribe as a listener and load all the installed launcher apps
        LauncherAppsLoader.subscribe(this);
        loadInstalledApps();

        // load all the apps which are excluded from scans
        ExcludedApps.loadExcludedApps(getApplicationContext());
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

    @Override
    protected void onStop() {
        super.onStop();
        // save the excluded apps when this activity is closed
        ExcludedApps.saveExcludedApps(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void loadInstalledApps () {
        if (!Activity_Apps_to_scan.this.isFinishing())
            progressDialog = ProgressDialog.show(Activity_Apps_to_scan.this, getResources().getString(R.string.loadingInfo), getResources().getString(R.string.activity_apps_to_scan_loading));
        LauncherAppsLoader.loadLauncherApps(getApplicationContext());
    }

    private List<Apps> createAppsList (List<ResolveInfo> launcherApps) {
        // list, which will store all the installed apps
        List<Apps> installedApps = new ArrayList<>();

        // create an apps object for each entry in launcher apps and add it to the list of installed apps
        for (ResolveInfo resolveInfo: launcherApps) {
            String packageName = resolveInfo.activityInfo.packageName;
            Apps tmp = new Apps(Utility.getAppIcon(getApplicationContext(), packageName), Utility.packageToAppName(getApplicationContext(), packageName), packageName);
            if (!installedApps.contains(tmp))
                installedApps.add(tmp);
        }

        // sort the installed apps list by name and return it
        Collections.sort(installedApps, Apps.AppComparator.decending(Apps.AppComparator.getComparator(Apps.AppComparator.SORT_BY_NAME)));

        return installedApps;
    }

    public void settingsButton(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
    }

    public void getHelp(MenuItem item) {
        ImpressumAndHelp.setupHelpDialog(Activity_Apps_to_scan.this);
    }

    public void getAboutUs(MenuItem item) {
        ImpressumAndHelp.setupImpressumDialog(Activity_Apps_to_scan.this);
    }

    public void getAppsToScan(MenuItem item){

    }

    // callback method, which is called when the list of installed apps has been loaded
    @Override
    public void OnLoadingLauncherAppsFinished(List<ResolveInfo> _launcherApps) {
        showInstalledAppList(_launcherApps);
    }

    private void showInstalledAppList(final List<ResolveInfo> launcherApps) {
        new AsyncTask<Void, Void, List<Apps>>() {

            @Override
            protected List<Apps> doInBackground(Void... params) {
                return createAppsList(launcherApps);
            }

            @Override
            protected void onPostExecute(List<Apps> appList) {
                // setup the array adapter with the created list of installed apps
                final Adapter_Apps_to_scan adapter = new Adapter_Apps_to_scan(getApplicationContext(), R.id.adapter_apps_to_scan, appList);
                listView.setAdapter(adapter);
                filter = adapter.getFilter();
                adapter.notifyDataSetChanged();
                setupSearchView();
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Sets up the SearchView
    private void setupSearchView(){
        sv.setIconifiedByDefault(false);
        sv.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){

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

}
