package com.op_dfat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

/**
 * Created by Robin Dieges
 */

public class MotionSensors extends BroadcastReceiver implements SensorEventListener {
    // reference to the Sensor Manager
    private SensorManager sensorManager;
    // reference to the Accelerometer Sensor
    private Sensor accelerometer;
    // reference to the PRoximity Sensor
    private Sensor proximity;
    // static reference to this class
    private static MotionSensors motionSensors;
    // reference to the context of the caller class
    Context context;

    // screen state, proximity and phone tilt
    public static float screenOrientation = 5;
    public static int screenState = 1;
    public static int closeToObject = 0;
    // variable, which stores the last time the sensors have been checked
    private long lastCheck;
    // variable, which stores the maximum amount of entries in a queue
    private static int maxSize = 10;
    // queues for the three sensors
    private static Deque<PhoneSensorState> screenStates = new ArrayDeque<>(maxSize);
    private static Deque<PhoneSensorState> screenOrientations = new ArrayDeque<>(maxSize);
    private static Deque<PhoneSensorState> closeToObjects = new ArrayDeque<>(maxSize);

    private MotionSensors(Context context) {
        this.context = context;
    }

    public static MotionSensors getInstance(Context context) {
        if (motionSensors == null) {
            motionSensors = new MotionSensors(context);
        }
        return motionSensors;
    }

    public void startCheckingSensors (Context context) {
        // get the Sensor Manager
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // get the accelerometer sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // register the listener to listen to events of the accelerometer sensor
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // get the proximity sensor
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        // register the listener to listen to events of the proximity sensor
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        // set the last time the sensors have been checked
        lastCheck = System.currentTimeMillis();
    }

    public void stopCheckingMotionSensors () {
        // unregister all the listeners
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, proximity);
    }

    public void registerReceiver(Context context){
        // setting up the intent filters
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        // register the receiver
        context.registerReceiver(this, intentFilter);
    }

    public void unregisterReceiver(Context context){
        // unregister the receiver, when not needed
        context.unregisterReceiver(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the current time in milli seconds
        long current = System.currentTimeMillis();
        // only register events of type accelerometer every 500 milliseconds (performance)
        if ((current - lastCheck) > 500) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                screenOrientation = sensorEvent.values[2];
                // queue the latest orientation
                queueScreenOrientations(new PhoneSensorState(System.currentTimeMillis(), Math.round(screenOrientation)));
            }
            lastCheck = current;
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            closeToObject = sensorEvent.values[0] < proximity.getMaximumRange()?1:0;
            // queue the latest information, whether the phone's screen was close to an object
            queueCloseToObject(new PhoneSensorState(System.currentTimeMillis(), closeToObject));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenState = 1;
            // queue the latest screen state
            queueScreenStates(new PhoneSensorState(System.currentTimeMillis(), screenState));
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenState = 0;
            // queue the latest screen state
            queueScreenStates(new PhoneSensorState(System.currentTimeMillis(), screenState));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // not in use
    }

    private synchronized void queueScreenStates (PhoneSensorState phoneSensorState) {
        // add screen states to the queue, if it has enough space
        if (screenStates.size() < maxSize) {
            screenStates.add(phoneSensorState);
        } else {
            // if not enough space in queue, remove the first (oldest) entry and add the new one to the tail
            screenStates.removeFirst();
            screenStates.add(phoneSensorState);
        }
    }

    private synchronized void queueScreenOrientations (PhoneSensorState phoneSensorState) {
        // add screen states to the queue, if it has enough space
        if (screenOrientations.size() < maxSize) {
            screenOrientations.add(phoneSensorState);
        } else {
            // if not enough space in queue, remove the first (oldest) entry and add the new one to the tail
            screenOrientations.removeFirst();
            screenOrientations.add(phoneSensorState);
        }
    }

    private synchronized void queueCloseToObject (PhoneSensorState phoneSensorState) {
        // add screen states to the queue, if it has enough space
        if (closeToObjects.size() < maxSize) {
            closeToObjects.add(phoneSensorState);
        } else {
            // if not enough space in queue, remove the first (oldest) entry and add the new one to the tail
            closeToObjects.removeFirst();
            closeToObjects.add(phoneSensorState);
        }
    }

    public static synchronized int getScreenStatesAtAccessTime (Date accessTime) {
        // (re)set the start screen state and set its actual value afterwards
        int state = 1;
        // take the latest screen state before the access time
        for (PhoneSensorState phoneSensorState: screenStates) {
            // break out of the loop, if a greater access time value was found in the queue
            if (phoneSensorState.timeStamp > accessTime.getTime())
                break;
            state = phoneSensorState.sensorState;
        }
        return state;
    }

    public static synchronized int getScreenOrientationAtAccessTime (Date accessTime) {
        // (re)set the start orientation and set its actual value afterwards
        int orientation = 5;
        // take the latest screen orientation before the access time
        for (PhoneSensorState phoneSensorState: screenOrientations) {
            // break out of the loop, if a greater access time value was found in the queue
            if (phoneSensorState.timeStamp > accessTime.getTime())
                break;
            orientation = phoneSensorState.sensorState;
        }
        return orientation;
    }

    public static synchronized int getCloseToObjectAtAccessTime (Date accessTime) {
        // (re)set the close to object variable to false and set its actual value afterwards
        int closeToObject = 0;
        // take the latest information whether the phone's screen was close to an object before the access time
        for (PhoneSensorState phoneSensorState: closeToObjects) {
            // break out of the loop, if a greater access time value was found in the queue
            if (phoneSensorState.timeStamp > accessTime.getTime())
                break;
            closeToObject = phoneSensorState.sensorState;
        }
        return closeToObject;
    }

    // struct, which stores the sensor state and the time in which the phone was in that sensor state
    private class PhoneSensorState {
        long timeStamp;
        int sensorState;

        PhoneSensorState(long timeStamp, int sensorState) {
            this.timeStamp = timeStamp;
            this.sensorState = sensorState;
        }
    }
}
