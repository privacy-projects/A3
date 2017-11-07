package com.op_dfat;

import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.support.v4.content.LocalBroadcastManager;
import java.util.Date;

/**
 * Created by Arrigo Paterno
 */

class Evaluation implements PermissionLogReader.PermissionLogListener {

    // identifier for the MyReceiver
    static final String SAMPLE_COUNT = "com.op_dfat.Evaluation.SampleCount";

    // references to necessary classes
    private static Evaluation evaluation = null;
    private UsageManager usageManager;
    private SqliteDBHelper sqliteDBHelper;
    public Context context;

    // variables to keep track of the amount of gathered data
    static long sampleCount = 0;
    private long currentSampleCount = 0;

    private Evaluation(Context context) {
        // get the instance to all necessary classes
        PermissionLogReader permissionLogReader = PermissionLogReader.getInstance(context);
        usageManager = UsageManager.getInstance(context, null);
        sqliteDBHelper = SqliteDBHelper.getInstance(context);
        // subscribe this listener
        permissionLogReader.subscribe(this);
        // store references to the context
        this.context  = context;
    }

    // factory method, to only have one instance of this class
    public static Evaluation getInstance(Context context) {
        if (evaluation == null) {
            return new Evaluation(context);
        }
        return evaluation;
    }

    // callback whenever a new data set was read from the permission log reader
    @Override
    public void OnPermissionLogRead(String packageName, int opID, Date accessTime) {
        evaluateSample(packageName, opID, accessTime);
    }

    // method which evaluates the data set from the permission log reader
    private void evaluateSample (String packageName, int opID, Date accessTime) {
        // if the app with packageName is not excluded from this scan
        if (!ExcludedApps.isAppExcluded(packageName)) {
            // get the current state the app is in at accessTime
            UsageManager.State appState = usageManager.checkUsageLog(packageName, accessTime);
            // get the screen state, screen orientation and proximity from objects to screen
            int screenOrientation = MotionSensors.getScreenOrientationAtAccessTime(accessTime);
            int screenState = MotionSensors.getScreenStatesAtAccessTime(accessTime);
            int closeToObject = MotionSensors.getCloseToObjectAtAccessTime(accessTime);

            // insert log data into raw data storage
            sqliteDBHelper.insertIntoDataAggregate(packageName, opID, accessTime, appState.value);
            sqliteDBHelper.insertIntoInfoAtAccessTime(accessTime, screenState, screenOrientation, closeToObject);

            sampleCount = DatabaseUtils.longForQuery(sqliteDBHelper.getReadableDatabase(), "SELECT COUNT(*) FROM " + SqliteDBStructure.DATA_AGGREGATE, null);

            // only update UI, if new entries have been added to DB and the display is on and DFAT is not in background
            if (sampleCount > currentSampleCount && MotionSensors.screenState == 1 && MainActivity.isActive) {
                Intent intent = new Intent();
                intent.setAction(SAMPLE_COUNT);
                intent.putExtra("currentSampleCount", sampleCount);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                // set the current sample count to the new sample count
                currentSampleCount = sampleCount;
            }
        }
    }

    // method for resetting sample count and current sample count
    void resetSampleCounts () {
        sampleCount = 0;
        currentSampleCount = 0;
    }
}
