package com.op_dfat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import java.util.Date;

/**
 * Created by Arrigo Paterno
 */

class NotificationHandler {
    // global ID, to identify a notification and to prevent multiple notifications on the stack
    static final int NOTIFICATION_ID = 12;
    // necessary references
    private static NotificationHandler notificationHandler = null;
    private NotificationManager notificationManager;
    private Context context;

    private NotificationHandler (Context context) {
        this.context = context;
        notificationManager = (NotificationManager)this.context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationHandler getInstance (Context context) {
        if (notificationHandler == null) {
            notificationHandler = new NotificationHandler(context);
        }
        return notificationHandler;
    }

    // show a notification, when the scan has been stopped
    void showScanStoppedNotification(long analysis_stop) {
        // bind an intent to the notification, so when it is pressed the scan result is loaded
        Intent intent = new Intent(context, Activity_apps.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // convert the scan stop into a string and put it as an extra to the intent, so it is delivered with the intent
        String analysisStopAsString = Utility.standardDateTime(new Date(analysis_stop));
        intent.putExtra(MyService.STOP_SCAN_TIME_STRING, analysisStopAsString);
        // finally wrap the intent into a pending intent
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // build th notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
//        notificationBuilder.setSmallIcon(R.drawable.cog);
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_name);
        notificationBuilder.setContentTitle(context.getString(R.string.notificationInformationTitle));
        notificationBuilder.setContentText(context.getString(R.string.notificationInformationSucsess));
        notificationBuilder.setContentIntent(contentIntent);
        notificationBuilder.setAutoCancel(true);
        // set a style for it
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        String[] events = {context.getString(R.string.notificationInformationSucsess), context.getString(R.string.notificationInformationSucsess1)};

        for (String event : events) {
            inboxStyle.addLine(event);
        }
        notificationBuilder.setStyle(inboxStyle);
        // build the notification and inform the notification manager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    // notification builder specifically designed for the startForeground call
    Notification buildScanNotification (long analysis_start) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationBuilder;

        String msg = context.getString(R.string.notificationInformationScanning) + " "
                + context.getString(R.string.notificationInformationScanning1) + " "
                + Utility.formatStandardDateTimeToSimpleFormat(Utility.standardDateTime(new Date(analysis_start + ScanSettings.scanDurationsInMS[ScanSettings.indexScanDuration])));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(context.getString(R.string.notificationInformationTitle));

        String[] events = {context.getString(R.string.notificationInformationScanning), context.getString(R.string.notificationInformationScanning2), context.getString(R.string.notificationInformationScanning1), Utility.formatStandardDateTimeToSimpleFormat(Utility.standardDateTime(new Date(analysis_start + ScanSettings.scanDurationsInMS[ScanSettings.indexScanDuration])))};
        for (String event : events) {
            inboxStyle.addLine(event);
        }

        notificationBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.cog)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.notificationInformationTitle))
                .setContentText(msg)
                .setStyle(inboxStyle)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();

        return notificationBuilder;
    }
}
