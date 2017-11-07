package com.op_dfat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

/**
 * Created by Arrigo Paterno
 */

public class AlarmReceiver extends BroadcastReceiver {

    public static final String SETUP_ALARM = "com.op_dfat.AlarmReceiver.SetupAlarm";

    @Override
    public void onReceive(Context context, Intent _intent) {
        switch(_intent.getAction()) {
            case SETUP_ALARM:
                // wake up the app
                wakeUp(context);
                break;
        }
    }

    private void wakeUp (Context context) {
        // vibrate the phone to indicate that the scan has been stopped (only, when the main activity is not active)
        if (!MainActivity.isActive) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(new long[]{0, 200, 100, 200, 100, 200}, -1);
        }

        // first (re)start the main activity or open it, if already started
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // start the service with the intend to stop the scan
        intent = new Intent(context, MyService.class);
        intent.setAction(MyService.STOP_SCAN);

        context.startService(intent);
    }

    public static void setupWakeUpAlarm (Context context, long startTime, long duration) {
        // get a reference to the alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // setup an intent
        Intent intent = new Intent();
        // specify an action for the receiver to listen to
        intent.setAction(SETUP_ALARM);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        // set the alarm to fire at the given time
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime + duration, alarmIntent);
    }

    public static void cancelWakeUpAlarm (Context context) {
        // get a reference to the alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // setup an intent
        Intent intent = new Intent();
        // specifiy an action for the receiver to listen to
        intent.setAction(SETUP_ALARM);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        // set the alarm to fire at the given time
        alarmManager.cancel(alarmIntent);
    }
}
