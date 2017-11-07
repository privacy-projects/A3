package com.op_dfat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Date;
import java.util.List;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


/**
 * Created by Dawid Wowniuk, Arrigo Paterno, Robin Dieges, Michael Krapp
 */

public class MainActivity extends AppCompatActivity implements Fragment_analyze.OnFragmentInteractionListener, Fragment_history.OnFragmentInteractionListener, Fragment_blacklist.OnFragmentInteractionListener{

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    // reference to the background service
    public MyService mService;
    // is the service bound to this activity
    boolean isBound = false;
    // reference to the custom UsageManager
    public UsageManager usageManager;
    public SqliteDBHelper sqliteDBHelper;

    // is app scanning
    public static boolean scanning;
    // is receiver registered
    public static boolean registered = false;
    // Receiver instance
    private MyReceiver mReceiver;
    // static variable, which holds the state of the main activity
    public static boolean isActive;

    // OOSO: needed to save the log file in the corresponding directory
    private static Context context;
    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.context = getApplicationContext();

        //getApplicationContext().setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        // get the reference to the instance of the UsageManager class
        usageManager = UsageManager.getInstance(getApplicationContext(), this);
        sqliteDBHelper = SqliteDBHelper.getInstance(getApplicationContext());

        Permission.instantiateDictionary(getApplicationContext());

        // bind this activity to the background service
        bindBackgroundService();
        // register receiver
        registerReceiver();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle("@s");
        toolbar.setTitleTextColor(WHITE);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabview);
        tabLayout.setTabTextColors(WHITE, BLACK);
        tabLayout.setupWithViewPager(mViewPager);

        TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        Fragment_history fHist = (Fragment_history) getFragment(tab.getPosition());
                        assert fHist != null;
                        fHist.getScanList();
                        break;
                    case 2:
                        Fragment_blacklist fBlack = (Fragment_blacklist) getFragment(tab.getPosition());
                        assert fBlack != null;
                        fBlack.showBlackList();
                        break;
                    default:
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        };
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        checkFirstRun();

        loadConfiguration();
    }

    private void loadConfiguration () {
        ExcludedApps.loadExcludedApps(getApplicationContext());
        ScanSettings.loadSettings(getApplicationContext());
        IconLoader.loadPlayStoreData(getApplicationContext());
        if (Utility.networking(getApplicationContext())) {
            BlackListReceiver.setupBlackList(getApplicationContext());
            MyClient.updateBlacklist();
        }
    }

    private void checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {
            showPermissionNotification();

        } else if (currentVersionCode > savedVersionCode) {
            showPermissionNotification();

        }
        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    @Override
    protected void onStart() {
        isActive = true;
        sqliteDBHelper.deleteOlderScans();
        super.onStart();
    }

    @Override
    protected void onResume() {
        isActive = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        isActive = false;
        IconLoader.savePlayStoreData(getApplicationContext());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // if the service is still bound to this activity...
        if (mConnection != null) {
            // ...unbind it to prevent data leaks
            unbindService(mConnection);
        }
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
            registered = false;
        }
        isActive = false;
        super.onDestroy();
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
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        FragmentManager fragmentManager;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new Fragment_analyze();
                case 1:
                    return new Fragment_history();
                case 2:
                    return new Fragment_blacklist();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.analysis);
                case 1:
                    return getString(R.string.history);
                case 2:
                    return getString(R.string.blacklist);
            }
            return null;
        }
    }
    /*
    * Sets up the options in the options menu in the action bar*/
    public void settingsButton(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
    }

    public void getHelp(MenuItem item) {
       ImpressumAndHelp.setupHelpDialog(MainActivity.this);
    }

    public void getAboutUs(MenuItem item) {
        ImpressumAndHelp.setupImpressumDialog(MainActivity.this);
    }

    public void getAppsToScan(MenuItem item){
        Intent intent = new Intent(getApplicationContext(), Activity_Apps_to_scan.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /*
     * Author Daniel Mattes
     */
    void showPermissionNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.permissionNotificationTitle);
        builder.setMessage(R.string.permissionNotificationMessage);
        builder.setPositiveButton(R.string.permissionNotificationPositive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                usageManager.setPermission();
            }
        });

        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    // button start analyzing
    public void startStopAnalyzing(View view)
    {
        if (!mService.scanning) {
            startScan();
        }
        else {
            stopScan();
        }
    }


    private void startScan ()
    {
        // start the clock in the fragment
        Fragment_analyze analyze = (Fragment_analyze) getFragment(0);
        assert analyze != null;
        analyze.setClock(2);

        // build an intent which will be send to the service
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        intent.setAction(MyService.START_SCAN);

        // start the analyzing process in the background service
        startService(intent);
    }

    private void stopScan ()
    {
        // build an intent which will be send to the service
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        intent.setAction(MyService.STOP_SCAN);

        // stop the analyzing process in the background service
        startService(intent);
    }

    private Fragment getFragment (int position) {
        // get the fragment manager
        FragmentManager man = getSupportFragmentManager();
        // get all the fragments from the fragment manager
        List<Fragment> list = man.getFragments();

        if (list.size() > 0) {
            return list.get(position);
        } else {
            return null;
        }
    }

    void showScanResult (String scanStopString) {
        // start an activity, which shows the results of the scan
        Intent intent = new Intent(this, Activity_apps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MyService.STOP_SCAN_TIME_STRING, scanStopString);
        startActivity(intent);
    }

    // callback for the ActivityCompact request to set the UsageStats permission
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case UsageManager.MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS:
                if (!usageManager.hasPermission()){
                    usageManager.setPermission();
                }
                break;
        }
    }

    // bind to the background service
    private void bindBackgroundService() {
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // We've bound to the Service, cast the IBinder and get Service instance
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            // set this classes mService to the service instance
            mService = binder.getService();
            // service is bound
            isBound = true;
            // get the current scan state
            scanning = mService.scanning;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    private void registerReceiver(){
        //Register BroadcastReceiver
        //to receive events from our service
        mReceiver = new MyReceiver();
        // setting up the intent filters
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.SCAN_STOPPED);
        intentFilter.addAction(MyService.SCAN_STARTED);
        intentFilter.addAction(MyService.DATA_MINING_FINISHED);
        intentFilter.addAction(MyService.UPDATE_DATA_MINING_PROGRESS);
        intentFilter.addAction(Evaluation.SAMPLE_COUNT);
        // registering the receiver
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);
        registered = true;
    }

    // local broadcast receiver
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            Fragment_analyze analyze;

            switch (arg1.getAction()) {
                case MyService.SCAN_STARTED:
                    scanning = mService.scanning;
                    analyze = (Fragment_analyze) getFragment(0);
                    assert analyze != null;
                    applyScanningState(analyze);
                    break;
                case MyService.SCAN_STOPPED:
                    analyze = (Fragment_analyze) getFragment(0);
                    assert analyze != null;
                    scanning = mService.scanning;
                    analyze.stopClock();
                    applyScanningState(analyze);
                    analyze.showProgressDialog(arg1.getIntExtra("maxValue", 0));
                    break;
                case MyService.DATA_MINING_FINISHED:
                    String stopScanTimeString = Utility.standardDateTime(new Date(mService.stop_scan_time_ms));
                    analyze = (Fragment_analyze) getFragment(0);
                    assert analyze != null;
                    analyze.dismissProgressDialog();
                    if (MainActivity.isActive)
                        showScanResult(stopScanTimeString);
                    break;
                case MyService.UPDATE_DATA_MINING_PROGRESS:
                    analyze = (Fragment_analyze) getFragment(0);
                    assert analyze != null;
                    analyze.updateProgressDialog(arg1.getIntExtra("progress", 1));
                    break;
                case Evaluation.SAMPLE_COUNT:
                    setSamples(arg1.getLongExtra("currentSampleCount", 0));
                    break;
            }
        }

        void setSamples (final long samples) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Fragment_analyze analyze = (Fragment_analyze) getFragment(0);
                    if (analyze != null)
                        analyze.setSampleCount(samples);
                    }
            });
        }

        void applyScanningState (Fragment_analyze analyze) {
            analyze.applyChanges(scanning);
        }
    }
}