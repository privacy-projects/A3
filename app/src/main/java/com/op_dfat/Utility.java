package com.op_dfat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Arrigo Paterno
 */

 class Utility {

    // convenience method to convert a package name to a readable app name
     static String packageToAppName(Context context, String packageName) {
        PackageManager packageManager= context.getApplicationContext().getPackageManager();

        try {
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0));
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("UTILITY", "app name for " + packageName + " not found");
            return packageName;
        }
    }

    // convenience method to load the icon of the corresponding app
     static Drawable getAppIcon(Context context, String packageName) {
        PackageManager packageManager= context.getApplicationContext().getPackageManager();

        try {
            return packageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("UTILITY", "app icon for " + packageName + " not found");
            return  context.getResources().getDrawable(R.mipmap.ic_launcher, null);
        }
    }

    // convenience method to convert the time of a date object to as string in standard format
     static String standardDateTime(Date time_stamp) {
        if (time_stamp == null) {
            Date d = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(d);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(time_stamp);
        }
    }

    // convenience method to convert a string in standard format into more readable format
     static String formatStandardDateTimeToSimpleFormat(String inputDate){
        Date parsed;

        SimpleDateFormat df_input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("dd.MM.yyy HH:mm:ss", java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            inputDate = df_output.format(parsed);

        } catch (ParseException e) {
            Log.d("UTILITY", inputDate + " is in the wrong format - yyyy-MM-dd HH:mm:ss");
        }
        return inputDate;
    }

    // convenience method to convert a string in simple format into standard format
     static String formatSimpleFormatToStandardDateTime(String inputDate) {
        Date parsed;

        SimpleDateFormat df_input = new SimpleDateFormat("dd.MM.yyy HH:mm:ss", java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            inputDate = df_output.format(parsed);

        } catch (ParseException e) {
            Log.d("UTILITY", inputDate + " is in the wrong format - dd.MM.yyy HH:mm:ss");
        }
        return inputDate;
    }

    // convenience method to check, whether an app is marked as an system app
    @SuppressLint("PackageManagerGetSignatures")
    static boolean isSystemApp(Context context, String packageName) {
        PackageManager mPackageManager = context.getPackageManager();

        try {
            // Get package info for target application
            PackageInfo targetPkgInfo = mPackageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            // Get packageinfo for system package
            PackageInfo sys = mPackageManager.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            // Match both packageinfo for there signatures
            return (targetPkgInfo != null && targetPkgInfo.signatures != null && sys.signatures[0].equals(targetPkgInfo.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // convenience method to check, whether an app is marked as being pre-installed
    static boolean isAppPreLoaded(Context context, String packageName) {
        PackageManager mPackageManager = context.getPackageManager();

        try {
            ApplicationInfo ai = mPackageManager.getApplicationInfo(packageName, 0);
            // check if it is preloaded.
            return (ai.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // convenience method to check, whether internet connection is available
    private static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    @SuppressLint("WifiManagerPotentialLeak")
    private static boolean wifiConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // Connected to an access point
            return wifiInfo.getNetworkId() != -1;
        }
        else {
            // Wi-Fi adapter is OFF
            return false;
        }
    }

    static boolean networking(Context context) {
        if (ScanSettings.onlyWIFI) {
            return wifiConnected(context);
        } else {
            return isInternetAvailable(context);
        }
    }

    static public void appendLog(String text)
    {
        //File logFile = new File(MainActivity.getAppContext().getFilesDir(), "DAFT_log.txt");
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        String strDate = sdf.format(c.getTime());
        File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/A3_log_" + strDate + ".txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.flush();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
