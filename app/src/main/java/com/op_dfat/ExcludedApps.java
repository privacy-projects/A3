package com.op_dfat;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Arrigo Paterno
 */

class ExcludedApps {

    // variable for the shared preference file
    private static final String PREFS_NAME = "com.op_dfat.ExcludedApps";
    // list of all the apps, which are excluded from the scan
    private static List<String> excludedApps = new ArrayList<>();

    // add an app to the excluded apps list
    static void addToExcludedAppsList(String packageName) {
        if (!excludedApps.contains(packageName)) {
            excludedApps.add(packageName);
        }
    }

    // remove an app from the excluded apps list
    static void removeFromExcludedAppsList(String packageName) {
        if (excludedApps.contains(packageName)) {
            excludedApps.remove(packageName);
        }
    }

    // checks, whether an app is excluded
    static boolean isAppExcluded(String packageName) {
        return excludedApps.contains(packageName);
    }

    // save the excluded apps list in shared preferences
    static void saveExcludedApps(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Set<String> excludedAppsSet = new HashSet<>(excludedApps);
        editor.putStringSet("excludedApps", excludedAppsSet);

        editor.apply();
    }

    // load the excluded apps list from shared preferences
    static void loadExcludedApps(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> loadedExcludedApps = preferences.getStringSet("excludedApps", null);

        if (loadedExcludedApps != null) {
            for (String excludedApp: loadedExcludedApps) {
                if (!excludedApps.contains(excludedApp)) {
                    excludedApps.add(excludedApp);
                }
            }
        }
    }
}
