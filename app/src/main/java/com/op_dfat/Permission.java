package com.op_dfat;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michael Krapp
 */


class Permission {
    /** @hide Access to coarse location information. */
    private static final int OP_COARSE_LOCATION = 0;
    /** @hide Access to fine location information. */
    private static final int OP_FINE_LOCATION = 1;
    /** @hide Causing GPS to run. */
    private static final int OP_GPS = 2;
    /** @hide */
    private static final int OP_VIBRATE = 3;
    /** @hide */
    private static final int OP_READ_CONTACTS = 4;
    /** @hide */
    private static final int OP_WRITE_CONTACTS = 5;
    /** @hide */
    private static final int OP_READ_CALL_LOG = 6;
    /** @hide */
    private static final int OP_WRITE_CALL_LOG = 7;
    /** @hide */
    private static final int OP_READ_CALENDAR = 8;
    /** @hide */
    private static final int OP_WRITE_CALENDAR = 9;
    /** @hide */
    private static final int OP_WIFI_SCAN = 10;
    /** @hide */
    private static final int OP_POST_NOTIFICATION = 11;
    /** @hide */
    private static final int OP_NEIGHBORING_CELLS = 12;
    /** @hide */
    private static final int OP_CALL_PHONE = 13;
    /** @hide */
    private static final int OP_READ_SMS = 14;
    /** @hide */
    private static final int OP_WRITE_SMS = 15;
    /** @hide */
    private static final int OP_RECEIVE_SMS = 16;
    /** @hide */
    private static final int OP_RECEIVE_EMERGECY_SMS = 17;
    /** @hide */
    private static final int OP_RECEIVE_MMS = 18;
    /** @hide */
    private static final int OP_RECEIVE_WAP_PUSH = 19;
    /** @hide */
    private static final int OP_SEND_SMS = 20;
    /** @hide */
    private static final int OP_READ_ICC_SMS = 21;
    /** @hide */
    private static final int OP_WRITE_ICC_SMS = 22;
    /** @hide */
    private static final int OP_WRITE_SETTINGS = 23;
    /** @hide */
    private static final int OP_SYSTEM_ALERT_WINDOW = 24;
    /** @hide */
    private static final int OP_ACCESS_NOTIFICATIONS = 25;
    /** @hide */
    private static final int OP_CAMERA = 26;
    /** @hide */
    private static final int OP_RECORD_AUDIO = 27;
    /** @hide */
    private static final int OP_PLAY_AUDIO = 28;
    /** @hide */
    private static final int OP_READ_CLIPBOARD = 29;
    /** @hide */
    private static final int OP_WRITE_CLIPBOARD = 30;
    /** @hide */
    private static final int OP_TAKE_MEDIA_BUTTONS = 31;
    /** @hide */
    private static final int OP_TAKE_AUDIO_FOCUS = 32;
    /** @hide */
    private static final int OP_AUDIO_MASTER_VOLUME = 33;
    /** @hide */
    private static final int OP_AUDIO_VOICE_VOLUME = 34;
    /** @hide */
    private static final int OP_AUDIO_RING_VOLUME = 35;
    /** @hide */
    private static final int OP_AUDIO_MEDIA_VOLUME = 36;
    /** @hide */
    private static final int OP_AUDIO_ALARM_VOLUME = 37;
    /** @hide */
    private static final int OP_AUDIO_NOTIFICATION_VOLUME = 38;
    /** @hide */
    private static final int OP_AUDIO_BLUETOOTH_VOLUME = 39;
    /** @hide */
    private static final int OP_WAKE_LOCK = 40;
    /** @hide Continually monitoring location data. */
    private static final int OP_MONITOR_LOCATION = 41;
    /** @hide Continually monitoring location data with a relatively high power request. */
    private static final int OP_MONITOR_HIGH_POWER_LOCATION = 42;
    /** @hide Retrieve current usage stats via UsageStatsManager. */
    private static final int OP_GET_USAGE_STATS = 43;
    /** @hide */
    private static final int OP_MUTE_MICROPHONE = 44;
    /** @hide */
    private static final int OP_TOAST_WINDOW = 45;
    /** @hide Capture the device's display contents and/or audio */
    private static final int OP_PROJECT_MEDIA = 46;
    /** @hide Activate a VPN connection without user intervention. */
    private static final int OP_ACTIVATE_VPN = 47;
    /** @hide Access the WallpaperManagerAPI to write wallpapers. */
    private static final int OP_WRITE_WALLPAPER = 48;
    /** @hide Received the assist structure from an app. */
    private static final int OP_ASSIST_STRUCTURE = 49;
    /** @hide Received a screenshot from assist. */
    private static final int OP_ASSIST_SCREENSHOT = 50;
    /** @hide Read the phone state. */
    private static final int OP_READ_PHONE_STATE = 51;
    /** @hide Add voicemail messages to the voicemail content provider. */
    private static final int OP_ADD_VOICEMAIL = 52;
    /** @hide Access APIs for SIP calling over VOIP or WiFi. */
    private static final int OP_USE_SIP = 53;
    /** @hide Intercept outgoing calls. */
    private static final int OP_PROCESS_OUTGOING_CALLS = 54;
    /** @hide User the fingerprint API. */
    private static final int OP_USE_FINGERPRINT = 55;
    /** @hide Access to body sensors such as heart rate, etc. */
    private static final int OP_BODY_SENSORS = 56;
    /** @hide Read previously received cell broadcast messages. */
    private static final int OP_READ_CELL_BROADCASTS = 57;
    /** @hide Inject mock location into the system. */
    private static final int OP_MOCK_LOCATION = 58;
    /** @hide Read external storage. */
    private static final int OP_READ_EXTERNAL_STORAGE = 59;
    /** @hide Write external storage. */
    private static final int OP_WRITE_EXTERNAL_STORAGE = 60;
    /** @hide Turned on the screen. */
    private static final int OP_TURN_SCREEN_ON = 61;
    /** @hide Get device accounts. */
    private static final int OP_GET_ACCOUNTS = 62;
    /** @hide Control whether an application is allowed to run in the background. */
    private static final int OP_RUN_IN_BACKGROUND = 63;

    static int [] getAllOps() {
        return new int[] {
                OP_COARSE_LOCATION,
                OP_FINE_LOCATION,
//                OP_GPS,
//                OP_VIBRATE,
                OP_READ_CONTACTS,
//                OP_WRITE_CONTACTS ,
                OP_READ_CALL_LOG,
//                OP_WRITE_CALL_LOG ,
                OP_READ_CALENDAR ,
//                OP_WRITE_CALENDAR ,
                OP_WIFI_SCAN,
//                OP_POST_NOTIFICATION,
//                OP_NEIGHBORING_CELLS ,
                OP_CALL_PHONE ,
                OP_READ_SMS,
//                OP_WRITE_SMS,
                OP_RECEIVE_SMS,
//                OP_RECEIVE_EMERGECY_SMS,
                OP_RECEIVE_MMS,
                OP_RECEIVE_WAP_PUSH ,
//                OP_SEND_SMS,
                OP_READ_ICC_SMS,
//                OP_WRITE_ICC_SMS,
//                OP_WRITE_SETTINGS,
//                OP_SYSTEM_ALERT_WINDOW ,
                OP_ACCESS_NOTIFICATIONS ,
                OP_CAMERA,
                OP_RECORD_AUDIO,
//                OP_PLAY_AUDIO,
                OP_READ_CLIPBOARD,
//                OP_WRITE_CLIPBOARD,
//                OP_TAKE_MEDIA_BUTTONS,
//                OP_TAKE_AUDIO_FOCUS,
//                OP_AUDIO_MASTER_VOLUME,
//                OP_AUDIO_VOICE_VOLUME,
//                OP_AUDIO_RING_VOLUME,
//                OP_AUDIO_MEDIA_VOLUME ,
//                OP_AUDIO_ALARM_VOLUME,
//                OP_AUDIO_NOTIFICATION_VOLUME,
//                OP_AUDIO_BLUETOOTH_VOLUME,
//                OP_WAKE_LOCK,
                OP_MONITOR_LOCATION,
                OP_MONITOR_HIGH_POWER_LOCATION,
                OP_GET_USAGE_STATS,
//                OP_MUTE_MICROPHONE,
                OP_TOAST_WINDOW,
                OP_PROJECT_MEDIA,
                OP_ACTIVATE_VPN ,
//                OP_WRITE_WALLPAPER,
                OP_ASSIST_STRUCTURE ,
                OP_ASSIST_SCREENSHOT,
                OP_READ_PHONE_STATE,
                OP_ADD_VOICEMAIL ,
                OP_USE_SIP,
                OP_PROCESS_OUTGOING_CALLS,
                OP_USE_FINGERPRINT,
                OP_BODY_SENSORS,
                OP_READ_CELL_BROADCASTS,
//                OP_MOCK_LOCATION,
                OP_READ_EXTERNAL_STORAGE,
//                OP_WRITE_EXTERNAL_STORAGE,
//                OP_TURN_SCREEN_ON ,
                OP_GET_ACCOUNTS,
//                OP_RUN_IN_BACKGROUND
        };
    }

    static Map<Integer, String[]> permissionDictionary;

    static void instantiateDictionary(final Context context) {

        if (permissionDictionary == null) {
            permissionDictionary = new HashMap<Integer, String[]>() {
                {
                    put(OP_COARSE_LOCATION, new String[]{"OP_COARSE_LOCATION", context.getResources().getString(R.string.OP_COARSE_LOCATION)});
                    put(OP_FINE_LOCATION, new String[]{"OP_FINE_LOCATION", context.getResources().getString(R.string.OP_FINE_LOCATION)});
//                    put(OP_GPS, new String[]{"OP_GPS", context.getResources().getString(R.string.OP_GPS)});
//                    put(OP_VIBRATE, new String[]{"OP_VIBRATE", "Allows access to the vibrator"});
                    put(OP_READ_CONTACTS, new String[]{"OP_READ_CONTACTS", context.getResources().getString(R.string.OP_READ_CONTACTS)});
//                    put(OP_WRITE_CONTACTS, new String[]{"OP_WRITE_CONTACTS", "Allows an application to write to the user's contacts data"});
                    put(OP_READ_CALL_LOG, new String[]{"OP_READ_CALL_LOG", context.getResources().getString(R.string.OP_READ_CALL_LOG)});
//                    put(OP_WRITE_CALL_LOG, new String[]{"OP_WRITE_CALL_LOG", "Allows an application to write to the user's call log"});
                    put(OP_READ_CALENDAR, new String[]{"OP_READ_CALENDAR", context.getResources().getString(R.string.OP_READ_CALENDAR)});
//                    put(OP_WRITE_CALENDAR, new String[]{"OP_WRITE_CALENDAR", "Allows an application to write to the user's calendar data"});
                    put(OP_WIFI_SCAN, new String[]{"OP_WIFI_SCAN", context.getResources().getString(R.string.OP_WIFI_SCAN)});
//                    put(OP_POST_NOTIFICATION, new String[]{"OP_POST_NOTIFICATION", "Allows an application to post notifications"});
//                    put(OP_NEIGHBORING_CELLS, new String[]{"OP_NEIGHBORING_CELLS", "-"});
                    put(OP_CALL_PHONE, new String[]{"OP_CALL_PHONE", context.getResources().getString(R.string.OP_CALL_PHONE)});
                    put(OP_READ_SMS, new String[]{"OP_READ_SMS", context.getResources().getString(R.string.OP_READ_SMS)});
//                    put(OP_WRITE_SMS, new String[]{"OP_WRITE_SMS", "Allows an application to write SMS messages"});
                    put(OP_RECEIVE_SMS, new String[]{"OP_RECEIVE_SMS", context.getResources().getString(R.string.OP_RECEIVE_SMS)});
//                    put(OP_RECEIVE_EMERGECY_SMS, new String[]{"OP_RECEIVE_EMERGECY_SMS", "Allows an application to receive emergency SMS "});
                    put(OP_RECEIVE_MMS, new String[]{"OP_RECEIVE_MMS", context.getResources().getString(R.string.OP_RECEIVE_MMS)});
                    put(OP_RECEIVE_WAP_PUSH, new String[]{"OP_RECEIVE_WAP_PUSH", context.getResources().getString(R.string.OP_RECEIVE_WAP_PUSH)});
//                    put(OP_SEND_SMS, new String[]{"OP_SEND_SMS", "Allows an application to send SMS messages"});
                    put(OP_READ_ICC_SMS, new String[]{"OP_READ_ICC_SMS", context.getResources().getString(R.string.OP_READ_ICC_SMS)});
//                    put(OP_WRITE_ICC_SMS, new String[]{"OP_WRITE_ICC_SMS", "-"});
//                    put(OP_WRITE_SETTINGS, new String[]{"OP_WRITE_SETTINGS", "Required to write/modify/update system settings"});
//                    put(OP_SYSTEM_ALERT_WINDOW, new String[]{"OP_SYSTEM_ALERT_WINDOW", "Required to draw on top of other apps"});
                    put(OP_ACCESS_NOTIFICATIONS, new String[]{"OP_ACCESS_NOTIFICATIONS", context.getResources().getString(R.string.OP_ACCESS_NOTIFICATIONS)});
                    put(OP_CAMERA, new String[]{"OP_CAMERA", context.getResources().getString(R.string.OP_CAMERA)});
                    put(OP_RECORD_AUDIO, new String[]{"OP_RECORD_AUDIO", context.getResources().getString(R.string.OP_RECORD_AUDIO)});
//                    put(OP_PLAY_AUDIO, new String[]{"OP_PLAY_AUDIO", context.getResources().getString(R.string.OP_PLAY_AUDIO)});
                    put(OP_READ_CLIPBOARD, new String[]{"OP_READ_CLIPBOARD", context.getResources().getString(R.string.OP_READ_CLIPBOARD)});
//                    put(OP_WRITE_CLIPBOARD, new String[]{"OP_WRITE_CLIPBOARD", context.getResources().getString(R.string.OP_WRITE_CLIPBOARD)});
//                    put(OP_TAKE_MEDIA_BUTTONS, new String[]{"OP_TAKE_MEDIA_BUTTONS", context.getResources().getString(R.string.OP_TAKE_MEDIA_BUTTONS)});
//                    put(OP_TAKE_AUDIO_FOCUS, new String[]{"OP_TAKE_AUDIO_FOCUS", context.getResources().getString(R.string.OP_TAKE_AUDIO_FOCUS)});
//                    put(OP_AUDIO_MASTER_VOLUME, new String[]{"OP_AUDIO_MASTER_VOLUME", context.getResources().getString(R.string.OP_AUDIO_MASTER_VOLUME)});
//                    put(OP_AUDIO_VOICE_VOLUME, new String[]{"OP_AUDIO_VOICE_VOLUME", context.getResources().getString(R.string.OP_AUDIO_VOICE_VOLUME)});
//                    put(OP_AUDIO_RING_VOLUME, new String[]{"OP_AUDIO_RING_VOLUME", context.getResources().getString(R.string.OP_AUDIO_RING_VOLUME)});
//                    put(OP_AUDIO_MEDIA_VOLUME, new String[]{"OP_AUDIO_MEDIA_VOLUME", context.getResources().getString(R.string.OP_AUDIO_MEDIA_VOLUME)});
//                    put(OP_AUDIO_ALARM_VOLUME, new String[]{"OP_AUDIO_ALARM_VOLUME", context.getResources().getString(R.string.OP_AUDIO_ALARM_VOLUME)});
//                    put(OP_AUDIO_NOTIFICATION_VOLUME, new String[]{"OP_AUDIO_NOTIFICATION_VOLUME", context.getResources().getString(R.string.OP_AUDIO_NOTIFICATION_VOLUME)});
//                    put(OP_AUDIO_BLUETOOTH_VOLUME, new String[]{"OP_AUDIO_BLUETOOTH_VOLUME", context.getResources().getString(R.string.OP_AUDIO_BLUETOOTH_VOLUME)});
//                    put(OP_WAKE_LOCK, new String[]{"OP_WAKE_LOCK", context.getResources().getString(R.string.OP_WAKE_LOCK)});
                    put(OP_MONITOR_LOCATION, new String[]{"OP_MONITOR_LOCATION", context.getResources().getString(R.string.OP_MONITOR_LOCATION)});
                    put(OP_MONITOR_HIGH_POWER_LOCATION, new String[]{"OP_MONITOR_HIGH_POWER_LOCATION", context.getResources().getString(R.string.OP_MONITOR_HIGH_POWER_LOCATION)});
                    put(OP_GET_USAGE_STATS, new String[]{"OP_GET_USAGE_STATS", context.getResources().getString(R.string.OP_GET_USAGE_STATS)});
//                    put(OP_MUTE_MICROPHONE, new String[]{"OP_MUTE_MICROPHONE", context.getResources().getString(R.string.OP_MUTE_MICROPHONE)});
                    put(OP_TOAST_WINDOW, new String[]{"OP_TOAST_WINDOW", context.getResources().getString(R.string.OP_TOAST_WINDOW)});
                    put(OP_PROJECT_MEDIA, new String[]{"OP_PROJECT_MEDIA", context.getResources().getString(R.string.OP_PROJECT_MEDIA)});
                    put(OP_ACTIVATE_VPN, new String[]{"OP_ACTIVATE_VPN", context.getResources().getString(R.string.OP_ACTIVATE_VPN)});
//                    put(OP_WRITE_WALLPAPER, new String[]{"OP_WRITE_WALLPAPER", context.getResources().getString(R.string.OP_WRITE_WALLPAPER)});
                    put(OP_ASSIST_STRUCTURE, new String[]{"OP_ASSIST_STRUCTURE", context.getResources().getString(R.string.OP_ASSIST_STRUCTURE)});
                    put(OP_ASSIST_SCREENSHOT, new String[]{"OP_ASSIST_SCREENSHOT", context.getResources().getString(R.string.OP_ASSIST_SCREENSHOT)});
                    put(OP_READ_PHONE_STATE, new String[]{"OP_READ_PHONE_STATE", context.getResources().getString(R.string.OP_READ_PHONE_STATE)});
                    put(OP_ADD_VOICEMAIL, new String[]{"OP_ADD_VOICEMAIL", context.getResources().getString(R.string.OP_ADD_VOICEMAIL)});
                    put(OP_USE_SIP, new String[]{"OP_USE_SIP", context.getResources().getString(R.string.OP_USE_SIP)});
                    put(OP_PROCESS_OUTGOING_CALLS, new String[]{"OP_PROCESS_OUTGOING_CALLS", context.getResources().getString(R.string.OP_PROCESS_OUTGOING_CALLS)});
                    put(OP_USE_FINGERPRINT, new String[]{"OP_USE_FINGERPRINT", context.getResources().getString(R.string.OP_USE_FINGERPRINT)});
                    put(OP_BODY_SENSORS, new String[]{"OP_BODY_SENSORS", context.getResources().getString(R.string.OP_BODY_SENSORS)});
                    put(OP_READ_CELL_BROADCASTS, new String[]{"OP_READ_CELL_BROADCASTS", context.getResources().getString(R.string.OP_READ_CELL_BROADCASTS)});
//                    put(OP_MOCK_LOCATION, new String[]{"OP_MOCK_LOCATION", context.getResources().getString(R.string.OP_MOCK_LOCATION)});
                    put(OP_READ_EXTERNAL_STORAGE, new String[]{"OP_READ_EXTERNAL_STORAGE", context.getResources().getString(R.string.OP_READ_EXTERNAL_STORAGE)});
//                    put(OP_WRITE_EXTERNAL_STORAGE, new String[]{"OP_WRITE_EXTERNAL_STORAGE", context.getResources().getString(R.string.OP_WRITE_EXTERNAL_STORAGE)});
//                    put(OP_TURN_SCREEN_ON, new String[]{"OP_TURN_SCREEN_ON", context.getResources().getString(R.string.OP_TURN_SCREEN_ON)});
                    put(OP_GET_ACCOUNTS, new String[]{"OP_GET_ACCOUNTS", context.getResources().getString(R.string.OP_GET_ACCOUNTS)});
//                    put(OP_RUN_IN_BACKGROUND, new String[]{"OP_RUN_IN_BACKGROUND", context.getResources().getString(R.string.OP_RUN_IN_BACKGROUND)});
                }
            };
        }
    }

    // convenience method to convert an opID to the corresponding name
    static String opIDToName (int opID) {
        if (Permission.permissionDictionary.containsKey(opID)) {
            return Permission.permissionDictionary.get(opID)[0];
        } else {
            Log.d("UTILITY", opID + " not found");
            return  ""+ opID;
        }
    }

    // convenience method to convert an opID to the corresponding name
    static String opIDToDescription (int opID) {
        if (Permission.permissionDictionary.containsKey(opID)) {
            return Permission.permissionDictionary.get(opID)[1];
        } else {
            Log.d("UTILITY", opID + " not found");
            return  ""+ opID;
        }
    }
}




