package com.op_dfat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MyService extends Service implements Datamining.DataminingProcessingListener {
    // binder that is given to the clients, who wish to bind to this service
    private final IBinder mBinder = new LocalBinder();
    // stores the start time of the most recent scan
    public static long start_scan_time_ms;
    // stores the stop time of the most recent scan
    public long stop_scan_time_ms;
    // tags for various applications, mainly broadcasts
    public static final String SERVICE_WAKELOCK = "com.op_dfat.MyService.ServiceWakelock";
    public static final String START_SCAN = "com.op_dfat.MyService.StartScan";
    public static final String STOP_SCAN = "com.op_dfat.MyService.StopScan";
    public static final String SCAN_STOPPED = "com.op_dfat.MyService.ScanStopped";
    public static final String SCAN_STARTED = "com.op_dfat.MyService.ScanStarted";
    public static final String STOP_SCAN_TIME_STRING = "com.op_dfat.MyService.StopScanTimeString";
    public static final String UPDATE_DATA_MINING_PROGRESS = "com.op_dfat.MyService.UpdateDataMiningProgress";
    public static final String DATA_MINING_FINISHED = "com.op_dfat.MyService.DataMiningFinished";
    // scheduled thread pool executor, which handles tasks and threads
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = null;
    // futures, which store information about the scheduled tasks
    private ScheduledFuture<?> scan_future, check_motion_future;
    // variable, which checks, whether we are currently scanning or not
    public boolean scanning = false;
    // references the all the necessary class instances
    private PermissionLogReader permissionLogReader;
    private MotionSensors motionSensors;
    private Datamining datamining;
    private Evaluation evaluation;
    private NotificationHandler notificationHandler;
    // references for the wakelock
    private PowerManager.WakeLock wakeLock;

    class LocalBinder extends Binder
    {
        MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }

    private Runnable read_permission_log = new Runnable()
    {
        @Override
        public void run() {
            try {
                // read the permission log at a fixed interval
                permissionLogReader.readPermissionLog();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable start_motion_future_sensors = new Runnable() {
        @Override
        public void run() {
            // start the motion sensor and register the receiver
            motionSensors.startCheckingSensors(getApplicationContext());
            motionSensors.registerReceiver(getApplicationContext());
        }
    };

    private Runnable stop_check_motion_future = new Runnable() {
        @Override
        public void run() {
            // stop the motion senor tracking and unregister the receiver
            motionSensors.stopCheckingMotionSensors();
            motionSensors.unregisterReceiver(getApplicationContext());
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate ()
    {
        super.onCreate ();
        // get references to all necessary instances
        permissionLogReader = PermissionLogReader.getInstance(getApplicationContext());
        motionSensors = MotionSensors.getInstance(getApplicationContext());
        datamining = Datamining.getInstance(getApplicationContext());
        // subscribe, so this service is informed, when the data mining has been completed
        datamining.subscribe(this);
        evaluation = Evaluation.getInstance(getApplicationContext());
        notificationHandler = NotificationHandler.getInstance(getApplicationContext());

        // instantiate the thread pool executor, which handles the threads
        if (scheduledThreadPoolExecutor == null) {
            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);
            scheduledThreadPoolExecutor.allowCoreThreadTimeOut(true);
        }
        // set policies on task termination
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        // get a reference to the power manager
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        // setup a wakelock
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SERVICE_WAKELOCK);
    }

    @Override
    public void onDestroy ()
    {
        // shutdown all the running tasks
        shutdownRunningTasks();
        // shutdown the thread pool executor
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
        super.onDestroy ();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // when service is started, send the information whether the service is currently analyzing
        if (intent != null) {
            if (intent.getAction().equals(START_SCAN)) {
                // set the new scan start of this scan period
                start_scan_time_ms = System.currentTimeMillis();
                // the service should be in foreground so it won't be killed so easily by the OS
                startForeground(NotificationHandler.NOTIFICATION_ID, notificationHandler.buildScanNotification(start_scan_time_ms));
                // setup an wake-up alarm to stop the scan at a given time or default after x days
                AlarmReceiver.setupWakeUpAlarm(getApplicationContext(), start_scan_time_ms, ScanSettings.scanDurationsInMS[ScanSettings.indexScanDuration]);
                // start the scan process
                startAnalyzing();
            } else if (intent.getAction().equals(STOP_SCAN)) {
                // set the stop time
                stop_scan_time_ms = System.currentTimeMillis();
                // send an intent to the receiver which stops any ongoing alarm setups
                AlarmReceiver.cancelWakeUpAlarm(getApplicationContext());
                // stop the scan process
                stopAnalyzing();
            }
        }
        return START_STICKY;
    }

    private void startAnalyzing () {
        // if we are not currently analyzing ...
        if (!scanning) {
            // acquire the wakelock so the service cannot go to sleep
            if (wakeLock != null)
                wakeLock.acquire();
            // set the new scan state to true
            scanning = true;
            // send the current scan state to the main activity
            adjustFragment_analyze();
            // start all necessary tasks
            startTasks();
        }
    }

    private void stopAnalyzing () {
        // if we are currently scanning ...
        if (scanning) {
            // set the new scan state to false
            scanning = false;
            // ...stop all currently running tasks
            shutdownRunningTasks();
            // wait until the receiver in the main activity has been registered
            waitForRegisterReceiver();
            // start the data mining process
            dataMiningProcess();
        }
    }

    private void startTasks() {
        if (scheduledThreadPoolExecutor != null) {
            // start the motion sensors
            check_motion_future = scheduledThreadPoolExecutor.schedule(start_motion_future_sensors, 0, TimeUnit.MILLISECONDS);
            // set the scan start in the permission log reader
            permissionLogReader.setStartScanTime(start_scan_time_ms);
            // get the scan interval (in ms) and convert it into seconds
            long scanInterval = TimeUnit.MILLISECONDS.toSeconds(ScanSettings.scanIntervalInMS[ScanSettings.indexScanInterval]);
            // schedule the scan task at a fixed repeat rate
            scan_future = scheduledThreadPoolExecutor.scheduleAtFixedRate(read_permission_log, 0, scanInterval, TimeUnit.SECONDS);
        }
    }

    private void shutdownRunningTasks() {
        // cancel all currently running tasks
        if (scan_future != null) {
            scan_future.cancel(false);
        }
        if (check_motion_future != null) {
            check_motion_future.cancel(false);
            // stop the motion sensors
            scheduledThreadPoolExecutor.schedule(stop_check_motion_future, 0, TimeUnit.MILLISECONDS);
        }
    }

    private void dataMiningProcess () {
        // start the data mining process on another thread
        datamining.startAnalysis(start_scan_time_ms, stop_scan_time_ms);
    }

    private void waitForRegisterReceiver () {
        // wait passively until the receiver in the main activity is registered
        while (!MainActivity.registered) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // callback, which is called, when the data mining process finished evaluating the data
    @Override
    public void OnProcessingFinished() {
        // reset raw data tables and found sample count
        reset();
        // send a broadcast to inform the main activity that the data mining has been finished
        sendDataminingFinished();
        // show a notification to the user
        notificationHandler.showScanStoppedNotification(stop_scan_time_ms);
        // stop the service running in foreground
        stopForeground(false);
        // release the wakelock when the scan has been stopped
        if (wakeLock != null)
            wakeLock.release();
    }

    private void reset () {
        // reset the found sample count
        evaluation.resetSampleCounts();
        // delete the raw data table; they are not need anymore
        SqliteDBHelper.getInstance(getApplicationContext()).deleteRawDataTables();
    }

    private void adjustFragment_analyze () {
        // send a broadcast about the current analysis state, which will be received by the broadcast receiver in the main activity
        Intent intent = new Intent();
        intent.setAction(SCAN_STARTED);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void sendDataminingFinished () {
        // send a broadcast to inform the main activity that the data mining has been finished
        Intent intent = new Intent();
        intent.setAction(DATA_MINING_FINISHED);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
