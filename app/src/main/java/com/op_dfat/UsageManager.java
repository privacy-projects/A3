package com.op_dfat;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Arrigo Paterno
 */

class UsageManager {
    // tag for permission access gratification
    static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1;

    private static UsageManager usageManager = null;
    // references to necessary classes
    private Context context;
    private Activity activity;
    private UsageStatsManager lUsageStatsManager;

    // enum, which stores the app states
    enum State {
        EXCEPTION (-100),
        SYSTEM_APP (-2),
        PRE_INSTALLED_APP (-1),
        INACTIVE (0),
        BACKGROUND (1),
        FOREGROUND (2),
        USER_INTERACTION (3),
        CONFIGURATION_CHANGE (4),
        SHORTCUT_INVOCATION (5),
        NONE (6);

        public final int value;

        State(int i) {
            value = i;
        }
    }

    private UsageManager(Context ctx, Activity act) {
        this.context = ctx;
        this.activity = act;
        // get a reference to the UsageStatsManager
        lUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public static UsageManager getInstance(Context cxt, Activity act) {
        if (usageManager == null) {
            usageManager = new UsageManager(cxt, act);
        }
        return usageManager;
    }

    // checks, whether this app has the permission to use the usage stats manager
    boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    // asks the user to grant access to the phone's usage stats, if this app lacks the permission
    void setPermission() {
        if (!hasPermission())
            openUsageStatsSettings();
    }

    private void openUsageStatsSettings () {
        // create an intent
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
    }

    private Map<String, UsageStats> readUsageLog(Date accessTime) {
        // always check, whether this app has permission to use the usage stats and if not ask the user to grant them
        setPermission();
        // get all usage stats from 1 day into the past starting at access time
        return lUsageStatsManager.queryAndAggregateUsageStats(accessTime.getTime() - TimeUnit.DAYS.toMillis(1), accessTime.getTime());
    }

    State checkUsageLog(String packageName, Date accessTime) {
        // read the usage log in an interval of 1 day into the past
        Map<String, UsageStats> lUsageStatsList = readUsageLog(accessTime);

        // if the usage stats list is empty or null, the permission has not been set
        if (lUsageStatsList != null && lUsageStatsList.size() != 0) {
            // check, whether an app with the given name appears in the usage stats list
            if (lUsageStatsList.containsKey(packageName)) {
                UsageStats usageStat = lUsageStatsList.get(packageName);
                // if it does, check its app status at the given time
                return checkAppStatus(usageStat, accessTime);
            } else {
                // if not, check whether this app is pre-installed
                if (Utility.isAppPreLoaded(context, packageName)) {
                    // check if it is even an android system app
                    if (Utility.isSystemApp(context, packageName)) {
                        return State.SYSTEM_APP;
                    } else {
                        return State.PRE_INSTALLED_APP;
                    }
                } else {
                    return State.NONE;
                }
            }
        } else {
            Log.d("USAGEMANAGER", "usage permission not set");
            return State.EXCEPTION;
        }
    }

    private State checkAppStatus (UsageStats u, Date accessTime) {
        // check whether the app is considered inactive
        if (lUsageStatsManager.isAppInactive(u.getPackageName())) {
            return State.INACTIVE;
        } else {
            // if not inactive, check its latest usage events
            return checkUsageEvent(u.getPackageName(), accessTime);
        }
    }

    private State checkUsageEvent(String packageName, Date accessTime) {
        // get the access time in milliseconds
        long accessTimeMS = accessTime.getTime();
        // get the usage events from the given time - 1 day to the given time of access
        UsageEvents events = lUsageStatsManager.queryEvents(accessTimeMS - TimeUnit.DAYS.toMillis(1), accessTimeMS);
        // create a new usage event
        UsageEvents.Event event = new UsageEvents.Event();
        // variable to store the latest state the app was in (default NONE)
        State currentState = State.NONE;

        // iterate over all events in the specified interval
        while (events.hasNextEvent()) {
            // get the next event and store it
            events.getNextEvent(event);
            // if time stamps appear, which are lower than the accessTime, stop searching
            if (event.getTimeStamp() > accessTimeMS){
                break;
            }
            // if this event is related to the given app...
            if (event.getPackageName().equals(packageName)) {
                // assign the state it was in at that time
                switch (event.getEventType()) {
                    case UsageEvents.Event.MOVE_TO_BACKGROUND:
                        currentState = State.BACKGROUND;
                        break;
                    case UsageEvents.Event.MOVE_TO_FOREGROUND:
                        currentState = State.FOREGROUND;
                        break;
                    case UsageEvents.Event.USER_INTERACTION:
                        currentState = State.USER_INTERACTION;
                        break;
                    case UsageEvents.Event.CONFIGURATION_CHANGE:
                        currentState = State.CONFIGURATION_CHANGE;
                        break;
                    case UsageEvents.Event.SHORTCUT_INVOCATION:
                        currentState = State.SHORTCUT_INVOCATION;
                        break;
                    case UsageEvents.Event.NONE:
                        currentState = State.NONE;
                        break;
                }
            }
        }

        // after iterating over all events the currentState variable should store the latest state the app was in
        // if the state is still NONE
        if (currentState == State.NONE) {
            // check whether this app is pre-installed
            if (Utility.isAppPreLoaded(context, packageName)) {
                currentState = State.PRE_INSTALLED_APP;
            }
            // check whether this app is an system app
            if (Utility.isSystemApp(context, packageName)) {
                currentState = State.SYSTEM_APP;
            }
        }
        return currentState;
    }
}
