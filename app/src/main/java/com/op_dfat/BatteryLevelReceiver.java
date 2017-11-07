package com.op_dfat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Arrigo Paterno
 */

public class BatteryLevelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // if battery to low, stop the scan and store everything from the scan
        emergencyDump(context);
        // cancel any ongoing alarms
        cancelWakeUpAlarm(context);
    }

    private void emergencyDump (Context context) {
        // stop the scan
        Intent intent = new Intent(context, MyService.class);
        intent.setAction(MyService.STOP_SCAN);

        context.startService(intent);
    }

    private void cancelWakeUpAlarm (Context context) {
        // get a reference to the alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // setup an intent
        Intent intent = new Intent();
        // specifiy an action for the receiver to listen to
        intent.setAction(AlarmReceiver.SETUP_ALARM);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        // set the alarm to fire at the given time
        alarmManager.cancel(alarmIntent);
    }
}
