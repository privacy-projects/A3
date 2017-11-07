package com.op_dfat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arrigo Paterno
 */

class LauncherAppsLoader {

    // list of all the launcher apps
    private static List<ResolveInfo> launcherApps = null;
    // list of all listeners which have subscribed
    private static List<LauncherAppsListener> launcherAppsListeners = new ArrayList<>();

    static void subscribe(LauncherAppsListener launcherAppsListener) {
        // add a launcher apps listener to the list, if its not already subscribed
        if (!launcherAppsListeners.contains(launcherAppsListener))
            launcherAppsListeners.add(launcherAppsListener);
    }

    static void loadLauncherApps(final Context context) {

        if (launcherApps == null) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {
                    // get a list of all the installed launcher apps from the package manager
                    final PackageManager packageManager = context.getPackageManager();
                    final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);

                    launcherApps = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    // inform all subscribers that the list has been loaded
                    OnLoadingLauncherAppsFinished();
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // inform all subscribers that the list has been loaded
            OnLoadingLauncherAppsFinished();
        }
    }

    private static void OnLoadingLauncherAppsFinished () {
        for (LauncherAppsListener listener: launcherAppsListeners) {
            listener.OnLoadingLauncherAppsFinished(launcherApps);
        }
    }

    // interface, which has to be implemented by all classes, which want to be informed, when the list of launcher apps has been loaded
    interface LauncherAppsListener {
        void OnLoadingLauncherAppsFinished (List<ResolveInfo> _launcherApps);
    }
}
