package com.op_dfat;

import android.app.AppOpsManager;
import android.content.Context;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Robin Dieges
 */

class PermissionLogReader {

    private static PermissionLogReader permissionLogReader = null;

    private Date lastLogScanTime;
    private Date accessTime;
    private long startScanTime;

    private static final Class<?> INT_ARRAY_TYPE = new int[0].getClass();
    private int[] allOpIDs;
    private AppOpsManager appOpsManager;

    private ArrayList<PermissionLogListener> eventListeners = new ArrayList<>();

    private PermissionLogReader(Context ctx) {
        // instantiate the variable
        lastLogScanTime = new Date();
        accessTime = new Date();
        // get all the found opIDs
        allOpIDs = Permission.getAllOps();
        // get a reference to the AppOpsManager
        appOpsManager = (AppOpsManager) ctx.getSystemService (Context.APP_OPS_SERVICE);
    }

    public static PermissionLogReader getInstance(Context ctx) {
        if (permissionLogReader == null) {
            permissionLogReader = new PermissionLogReader(ctx);
        }
        return permissionLogReader;
    }

    void subscribe (PermissionLogListener eventListener) {
        if (!eventListeners.contains(eventListener))
            this.eventListeners.add(eventListener);
    }

    void setStartScanTime(long startScanTimems) {
        startScanTime = startScanTimems;
        lastLogScanTime.setTime(startScanTimems);
    }

    void readPermissionLog() {
        try
        {
            long currentScanTime = System.currentTimeMillis();
            // get the anonymous class, which inherits from AppOpsManger, to get access to private methods of that class,
            // which delivers us the information we need
            Class<? extends AppOpsManager> clsAom = appOpsManager.getClass();
            // invoke a private method, which returns the list of objects, which have information about the package name of apps
            // and a list of permissions they apps can use
            List<?> packageOpsList = (List<?>) clsAom.getMethod ("getPackagesForOps", INT_ARRAY_TYPE).invoke (appOpsManager, allOpIDs);

            for (Object packageOps : packageOpsList)
            {
                // get yet again the anonymous class of every object in the list
                Class<?> clsPackageOps = packageOps.getClass ();
                // call a private method, wich gives us the package name of the app
                String packageName = (String)clsPackageOps.getMethod ("getPackageName").invoke (packageOps);
                // invoke a private method, which returns the list of objects again, that holds information about permissions, which the app has used
                List<?> opEntryList = (List<?>)clsPackageOps.getMethod ("getOps").invoke (packageOps);

                for (Object oe : opEntryList)
                {
                    // get the class of ervery object in the list
                    Class<?> clsOpEntry = oe.getClass ();
                    // get the opID used by the app
                    int oeOpid = (int) clsOpEntry.getMethod ("getOp").invoke (oe);
                    // get the time the app used that opID
                    accessTime.setTime((long) clsOpEntry.getMethod ("getTime").invoke (oe));
                    // if the time is before the start of our scan time, go to the next
                    if (accessTime.getTime() < startScanTime)
                        continue;
                    // only check the lasted entries
                    if (lastLogScanTime != null) {
                        if (accessTime.after(lastLogScanTime)) {
                            for (PermissionLogListener eventListener: eventListeners) {
                                eventListener.OnPermissionLogRead(packageName, oeOpid, accessTime);
                            }
                        }
                    }
                }
            }
            assert lastLogScanTime != null;
            lastLogScanTime.setTime(currentScanTime);
        }
        catch (InvocationTargetException | NoSuchMethodException e)
        {
            Log.e ("PERMISSIONLOGREADER","GET_APP_OPS_STATS permission not set");
        }
        catch (Exception e)
        {
            Log.e ("PERMISSIONLOGREADER", "PACKAGE_USAGE_STATS permission not set");
            e.printStackTrace();
        }
    }

    interface PermissionLogListener {
        void OnPermissionLogRead (String packageName, int opID, Date accessTime);
    }
}
