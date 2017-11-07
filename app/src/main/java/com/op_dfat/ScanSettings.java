package com.op_dfat;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.TimeUnit;

/**
 * Created by Arrigo Paterno
 */

class ScanSettings {

    // variable for the shared preference file
    private static final String SETTINGS = "com.op_dfat.ScanSettings.Settings";

    static final long[] scanDurationsInMS = new long[] {
            TimeUnit.MINUTES.toMillis(30),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.HOURS.toMillis(12),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.DAYS.toMillis(7)
    };
    // initial scan duration is set to 7 days
    static int indexScanDuration = 4;

    static final long[] scanIntervalInMS = new long[] {
            TimeUnit.SECONDS.toMillis(2),
            TimeUnit.SECONDS.toMillis(4),
            TimeUnit.SECONDS.toMillis(5)
    };
    // initial scan interval is 5 seconds
    static int indexScanInterval = 2;

    static final long[] scanDeletionInMS = new long[] {
            TimeUnit.DAYS.toMillis(7),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(0),
    };
    // initial time until scans are deleted is 0 (equivalent to never)
    static int indexScanDeletion = 3;

    // initially data should only be send within a wifi-network
    static boolean onlyWIFI = true;

    static void saveSettings (Context context) {
        // save important variable states in shared preference file
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor outState = settings.edit();
        outState.putInt("indexScanDuration", indexScanDuration);
        outState.putInt("indexScanInterval", indexScanInterval);
        outState.putInt("indexScanDeletion", indexScanDeletion);
        outState.putBoolean("onlyWIFI", onlyWIFI);

        // Commit the edits!
        outState.apply();
    }

    static void loadSettings (Context context) {
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, 0);

        indexScanDuration = settings.getInt("indexScanDuration", 4);
        indexScanInterval = settings.getInt("indexScanInterval", 2);
        indexScanDeletion = settings.getInt("indexScanDeletion", 3);
        onlyWIFI = settings.getBoolean("onlyWIFI", false);
    }
}
