package com.op_dfat;

/**
 * Created by Arrigo Paterno
 */

 class SqliteDBStructure {


     static final String DATABASE_NAME = "DFAT.db";
     static int VERSION = 52;

    // table column names
     static final String PACKAGE_NAME = "package_name";
     static final String PERMISSION = "permission";
     static final String ACCESS_TIME = "access_time";
     static final String APP_STATE = "app_state";
     static final String SCREEN_STATE = "screen_state";
     static final String SCREEN_ORIENTATION = "screen_orientation";
     static final String CLOSE_TO_OBJECT = "close_to_object";
     static final String COUNT = "count";
     static final String IS_ANOMALOUS = "is_anomalous";
     static final String ANOMALY_REASON = "anomaly_reason";
     static final String SCAN_TIME = "scan_time";
     static final String REPORTED = "reported";
     static final String FROM_USER = "from_user";
     static final String APP_TITLE = "app_title";
     static final String ICON_URL = "icon_url";

    // table names
     static final String APPS_LOGGED_AT_SCAN_TIME = "app_logged_at_scan_time";
     static final String DATA_AGGREGATE = "data_aggregate";
     static final String INFO_AT_ACCESSTIME = "info_at_access_time";
     static final String DATA_ANALYZING = "data_analysis";
     static final String SCAN_TIMES_LOGGED = "scan_times_logged";
     static final String SCANNED_APPS= "scanned_apps";
     static final String SAVE_SCAN = "save_scan";
     static final String SAVE_ANOMALIES = "save_anomalies";
     static final String PLAY_STORE_APP_INFOS = "play_store_app_infos";


     static final String CREATE_APPS_LOGGED_AT_SCAN_TIME = "CREATE TABLE " + APPS_LOGGED_AT_SCAN_TIME + " (" +
                                                    PACKAGE_NAME + " VARCHAR(100), " +
                                                    "PRIMARY KEY (" + PACKAGE_NAME + "));";

     static final String CREATE_DATA_AGGREGATE = "CREATE TABLE " + DATA_AGGREGATE + " ("
             + PACKAGE_NAME + " VARCHAR(100), "
             + PERMISSION + " INTEGER, "
             + ACCESS_TIME + " DATETIME, "
             + APP_STATE + " INTEGER, "
             + "PRIMARY KEY (" + PACKAGE_NAME + ", " + PERMISSION + ", " + ACCESS_TIME + "));";

     static final String CREATE_INFO_AT_ACCESSTIME = "CREATE TABLE " + INFO_AT_ACCESSTIME + " ("
            + ACCESS_TIME + " DATETIME, "
            + SCREEN_STATE + " INTEGER, "
            + SCREEN_ORIENTATION + " REAL, "
            + CLOSE_TO_OBJECT + " INTEGER, "
            + "PRIMARY KEY ("+ ACCESS_TIME + "));";

     static final String CREATE_DATA_ANALYZING = "CREATE TABLE " + DATA_ANALYZING
            + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PACKAGE_NAME + " VARCHAR(100), "
            + PERMISSION + " VARCHAR(100), "
            + ACCESS_TIME + " DATETIME, "
            + SCAN_TIME + " DATETIME, "
            + IS_ANOMALOUS + " INTEGER, "
            + COUNT + " INTEGER, "
            + ANOMALY_REASON + " VARCHAR(200), "
            + REPORTED + " INTEGER); ";

     static final String CREATE_SCAN_TIMES_LOGGED = "CREATE TABLE " + SCAN_TIMES_LOGGED + " (" +
            SCAN_TIME + " DATETIME, " +
            "PRIMARY KEY (" + SCAN_TIME + "));";

     static final String CREATE_SCANNED_APPS = "CREATE TABLE " + SCANNED_APPS + " (" +
            PACKAGE_NAME + " VARCHAR(100), " +
            SCAN_TIME + " DATETIME, " +
            "PRIMARY KEY(" + PACKAGE_NAME + ", " + SCAN_TIME + "));";

     static final String CREATE_SAVE_SCAN = "CREATE TABLE " + SAVE_SCAN
            + " (" + PACKAGE_NAME + " VARCHAR(100), "
            + PERMISSION + " VARCHAR(100), "
            + ACCESS_TIME + " DATETIME, "
            + APP_STATE + " NUMERIC, "
            + SCREEN_STATE + " NUMERIC, "
            + SCREEN_ORIENTATION + " REAL, "
            + CLOSE_TO_OBJECT + " NUMERIC, "
            + SCAN_TIME + " DATETIME, "
            + "PRIMARY KEY (" + PACKAGE_NAME + ", " + PERMISSION + ", " + ACCESS_TIME + ", " + SCAN_TIME +"));";

     static final String CREATE_SAVE_ANOMALIES = "CREATE TABLE " + SAVE_ANOMALIES
            + " (" + PACKAGE_NAME + " VARCHAR(100), "
            + PERMISSION + " VARCHAR(100), "
            + ANOMALY_REASON + " VARCHAR(200), "
            + FROM_USER + " NUMERIC, "
            + "PRIMARY KEY (" + PACKAGE_NAME + ", " + PERMISSION + ", " + ANOMALY_REASON + ", " + FROM_USER + "));";


     static final String CREATE_PLAY_STORE_APP_INFOS = "CREATE TABLE " + PLAY_STORE_APP_INFOS
            + " (" + PACKAGE_NAME + " VARCHAR(100), "
            + APP_TITLE + " VARCHAR(100), "
            + ICON_URL + " VARCHAR(500), "
            + "PRIMARY KEY (" + PACKAGE_NAME + "));";
}
