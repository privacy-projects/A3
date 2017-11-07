package com.op_dfat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marvin Duchmann & Arrigo Paterno
 */

class Datamining {

    // references to necessary classes
    private SqliteDBHelper sqliteDBHelper;
    private DataminingProcessingListener dataminingProcessingListener;
    private static Datamining datamining = null;
    private Context context;
    // variables for the scan
    private String scanTime;
    private Date start_scan_time, stop_scan_time;
    private boolean networking;
    // variables for threading
    private int tasks, finished;
    private final int MaxBatchSize = 400;

    private Datamining(Context context) {
        sqliteDBHelper = SqliteDBHelper.getInstance(context);
        start_scan_time = new Date();
        stop_scan_time = new Date();
        this.context = context;
    }

    // factory method to get a singleton of this class
    public static Datamining getInstance (Context context) {
        if (datamining == null) {
            datamining = new Datamining(context);
        }
        return datamining;
    }

    void startAnalysis(long analysis_start, long analysis_stop) {
        // set start and stop of this scan
        start_scan_time.setTime(analysis_start);
        stop_scan_time.setTime(analysis_stop);

        // convert the date object of the stop time to a string in standard date time format
        scanTime = Utility.standardDateTime(stop_scan_time);

        // check whether networking is possible
        networking = Utility.networking(context);

        // TODO OOSO: networking is commented, for logging we need to "send" the data.
        networking = true;
        if (networking) {
            // send all the apps, which have been tracked during this scan, to the server
            sendDataToServer();
        }
        // start complete evaluation of this scan
        evaluateData();
        // store this scans time in local DB for later use
        insertCurrentScanTime();
    }

    // classes which implement DataminingProcessingListener can subscribe so they are informed
    // on specific data mining events
    void subscribe (DataminingProcessingListener _dataminingProcessingListener) {
        if (dataminingProcessingListener == null) {
            dataminingProcessingListener = _dataminingProcessingListener;
        }
    }

    private void insertCurrentScanTime () {
        sqliteDBHelper.insertIntoScanTimesLogged(stop_scan_time);
    }

    private void sendDataToServer () {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                sendpackageNames();
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressWarnings("unchecked")
    private void sendpackageNames() {
        ArrayList<String> names =  sqliteDBHelper.query_select_list("SELECT " + SqliteDBStructure.PACKAGE_NAME + " FROM " + SqliteDBStructure.APPS_LOGGED_AT_SCAN_TIME);
        for (int i = 0; i < names.size(); i++){
            MyClient.POST_Name(names.get(i));
        }
    }

    private void evaluateData () {
        // first count how many rows there are in DB
        long rows = DatabaseUtils.longForQuery(sqliteDBHelper.openSqlDatabaseReadable(), "SELECT COUNT(*) FROM " + SqliteDBStructure.DATA_AGGREGATE
                                                                                                    + " JOIN " + SqliteDBStructure.INFO_AT_ACCESSTIME + " ON "
                                                                                                    + SqliteDBStructure.DATA_AGGREGATE + "." + SqliteDBStructure.ACCESS_TIME + " = " + SqliteDBStructure.INFO_AT_ACCESSTIME + "." + SqliteDBStructure.ACCESS_TIME, null);
        // calculate the amount of tasks (thread) depending on the MaxBatchSize
        tasks = rows >= MaxBatchSize? (int)(rows/MaxBatchSize + 1):1;
        // set the amount of finished tasks to 0
        finished = 0;
        // send the amount of task to the main activity so it can be displayed in the progress dialog
        sendTaskAmount(tasks + 1);
        // create a thread pool with tasks amount of threads
        final ExecutorService executorService = Executors.newFixedThreadPool(tasks);
        // create a list which holds all the tasks to be executed
        final List<ProcessingDataHandler> taskList = new LinkedList<>();
        // for each task create a batch of MaxBatchSize rows to evaluate
        for (int i = 0; i < tasks; i++) {
            // pass the offset (where to start) and the limit (how many rows)
            taskList.add(new ProcessingDataHandler(i*MaxBatchSize, MaxBatchSize));
        }
        // invoke all the task at once
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    executorService.invokeAll(taskList);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        updateProgressDialog();
    }

    private void sendRawData(String packageName, String permission, String accessTime, int appState, int screenState, int screenOrientation, int closeToObject) {
        MyClient.POST_rawdata(packageName, permission, accessTime, ""+appState, ""+screenState, ""+screenOrientation, ""+closeToObject, scanTime);
    }

    private void saveRawData(String packageName, String permission, String accessTime, int appState, int screenState, int screenOrientation, int closeToObject) {
        sqliteDBHelper.query("INSERT OR REPLACE INTO " + SqliteDBStructure.SAVE_SCAN + " VALUES ('" + packageName + "', " +
                                                                                                DatabaseUtils.sqlEscapeString(permission) + ", '" +
                                                                                                accessTime + "', " +
                                                                                                appState + ", " +
                                                                                                screenState + ", " +
                                                                                                screenOrientation + ", " +
                                                                                                closeToObject + ", '" +
                                                                                                scanTime + "');");
    }

    private void saveAnomaliesImage(String packageName, String permission, String anomalyReason, int fromUser) {
        sqliteDBHelper.query("INSERT OR REPLACE INTO " + SqliteDBStructure.SAVE_ANOMALIES + " VALUES (" + "'" + packageName + "'" +", " + "'" + permission +  "'" + ", " +  "'" + anomalyReason + "'" + ", " + fromUser + ");");
    }

    // method which inserts a bulk of data into the DB instead of one at a time
    private void bulkInsertIntoDataAnalyzing(ArrayList<DataInfo> tmp) {
        sqliteDBHelper.insertIntoDataAnalyzingBulk(tmp);
        sqliteDBHelper.insertIntoScannedAppsBulk(tmp);
    }

    // Data struct which holds important information
    class DataInfo {
        public String packageName;
        public String permission;
        String accessTime;
        long permissionCount;
        String isAnomalous;
        String scanTime;
        String anomalyReason;
        int reported;

        DataInfo(String packageName, String permission, String accessTime, long permissionCount, String isAnomalous, String anomalyReason, String scanTime, int reported) {
            this.packageName = packageName;
            this.permission = permission;
            this.accessTime = accessTime;
            this.permissionCount = permissionCount;
            this.isAnomalous = isAnomalous;
            this.anomalyReason = anomalyReason;
            this.scanTime = scanTime;
            this.reported = reported;
        }
    }

    // callable class which handles the evaluation of the scanned data
    private class ProcessingDataHandler implements Callable<Void> {
        // variables for the evaluation scope
        private int offset, limit;

        ProcessingDataHandler(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public Void call() throws Exception {
            process();
            return null;
        }

        // evaluation of the data happens here
        private void process () {

            ArrayList<String> anomaly;
            ArrayList<DataInfo> tmp = new ArrayList<>();
            int appState, screenState, screenOrientation, closeToObject, opID;
            long permissionCount;
            double scanDuration;
            String packageName, permission, accessTime;

            // get the data in the given scope (from offset, limit rows)
            Cursor cursor = sqliteDBHelper.get_data("SELECT " + SqliteDBStructure.PACKAGE_NAME + ", "
                    + SqliteDBStructure.PERMISSION + ", "
                    + SqliteDBStructure.DATA_AGGREGATE + "." + SqliteDBStructure.ACCESS_TIME + ", "
                    + SqliteDBStructure.APP_STATE + ", "
                    + SqliteDBStructure.SCREEN_STATE + ", "
                    + SqliteDBStructure.SCREEN_ORIENTATION + ", "
                    + SqliteDBStructure.CLOSE_TO_OBJECT
                    + " FROM " + SqliteDBStructure.DATA_AGGREGATE
                    + " JOIN " + SqliteDBStructure.INFO_AT_ACCESSTIME + " ON "
                    + SqliteDBStructure.DATA_AGGREGATE + "." + SqliteDBStructure.ACCESS_TIME + " = " + SqliteDBStructure.INFO_AT_ACCESSTIME + "." + SqliteDBStructure.ACCESS_TIME
                    + " LIMIT " + limit + " OFFSET " + offset);

            try {
                if (cursor != null) {
                    cursor.moveToFirst();

                    while (!cursor.isAfterLast()) {
                        packageName = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PACKAGE_NAME));
                        opID = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDBStructure.PERMISSION));
                        permission = Permission.opIDToName(opID);
                        accessTime = cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.ACCESS_TIME));
                        appState = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDBStructure.APP_STATE));
                        screenState = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDBStructure.SCREEN_STATE));
                        screenOrientation = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDBStructure.SCREEN_ORIENTATION));
                        closeToObject = cursor.getInt(cursor.getColumnIndexOrThrow(SqliteDBStructure.CLOSE_TO_OBJECT));

                        permissionCount = DatabaseUtils.longForQuery(sqliteDBHelper.getReadableDatabase(), "SELECT COUNT(*) FROM " + SqliteDBStructure.DATA_AGGREGATE + " WHERE " + SqliteDBStructure.PACKAGE_NAME + " = '" + packageName + "' " + " AND " + SqliteDBStructure.PERMISSION + " = '" + opID + "';", null);

                        //Scan duration in Minutes
                        scanDuration = (((double) stop_scan_time.getTime() + (double) start_scan_time.getTime()) / 1000 )/ 60;
                        //check for anomaly by ruleset class
                        anomaly = RuleSet.checkForAnomaly(packageName, permission, permissionCount, appState, screenState, screenOrientation, closeToObject, scanDuration, context);

                        //Send data to server or save it locally, if no valid internet connection is available
                        if (networking) {
                            sendRawData(packageName, permission, accessTime, appState, screenState, screenOrientation, closeToObject);
                            if (anomaly.get(0).equals("1"))
                                MyClient.POST_Anomaly(packageName, permission, anomaly.get(1), "0");
                        } else {
                            saveRawData(packageName, permission, accessTime, appState, screenState, screenOrientation, closeToObject);
                            if (anomaly.get(0).equals("1"))
                                saveAnomaliesImage(packageName, permission, anomaly.get(1), 0);
                        }

                        // create a info object which holds all the necessary information about that data block and store it temporarily in a list
                        DataInfo info = new DataInfo(packageName, Permission.opIDToDescription(opID), accessTime, permissionCount, anomaly.get(0), anomaly.get(1), scanTime, 0);
                        tmp.add(info);

                        // should the size of the temporary list exceeds MaxBatchSize, write list to the local DB and clear the temp list afterwards 
                        // technically this cannot happen but we are working with threads here so better be safe than sorry
                        // temp list should not be to big, because we have have to hold task amount of temp list, with each containing around MaxBatchSize entries, in memory at the same time
                        if (tmp.size() > MaxBatchSize) {
                            bulkInsertIntoDataAnalyzing(tmp);
                            tmp.clear();
                        }

                        cursor.moveToNext();
                    }

                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            // write the data stored in the temp list to the local DB 
            if (tmp.size() > 0) {
                bulkInsertIntoDataAnalyzing(tmp);
                tmp.clear();
            }
            // inform the progress dialog that a task has been finished
            updateProgressDialog();
            // decrease the amount of ongoing tasks by 1
            decreaseTasks();
        }
    }

    private synchronized int updateFinished() {
        return ++finished;
    }

    private synchronized void decreaseTasks () {
        tasks--;
        // if all tasks have been finished inform the listener about that
        if (tasks <= 0) {
            dataminingProcessingListener.OnProcessingFinished();
        }
    }

    private synchronized void updateProgressDialog() {
        Intent intent = new Intent();
        intent.setAction(MyService.UPDATE_DATA_MINING_PROGRESS);
        intent.putExtra("progress", updateFinished());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendTaskAmount (int maxValue) {
        Intent intent = new Intent();
        intent.putExtra("maxValue", maxValue);
        intent.setAction(MyService.SCAN_STOPPED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    interface DataminingProcessingListener {
        void OnProcessingFinished();
    }
}