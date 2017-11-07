package com.op_dfat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Arrigo Paterno
 */

public class SqliteDBHelper extends SQLiteOpenHelper {

    // static reference to this instance of the class
    private static SqliteDBHelper sqliteDBHelper_instance = null;
    // reference to the SQliteDatabase itself
    private SQLiteDatabase sqLiteDatabase = null;

    // private constructor. DB is initialized through a factory method
    private SqliteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // tables for the raw data
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_DATA_AGGREGATE);
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_INFO_AT_ACCESSTIME);
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_APPS_LOGGED_AT_SCAN_TIME);
        // tables for the data mining
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_DATA_ANALYZING);
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_SCAN_TIMES_LOGGED);
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_SCANNED_APPS);
        // tables for saving scans which could not be evaluated
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_SAVE_SCAN);
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_SAVE_ANOMALIES);
        // for the images from the play store
        sqLiteDatabase.execSQL(SqliteDBStructure.CREATE_PLAY_STORE_APP_INFOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // drop every existing table...
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.DATA_AGGREGATE);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.INFO_AT_ACCESSTIME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.APPS_LOGGED_AT_SCAN_TIME);

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.DATA_ANALYZING);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.SCAN_TIMES_LOGGED);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.SCANNED_APPS);

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.SAVE_SCAN);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.SAVE_ANOMALIES);

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SqliteDBStructure.PLAY_STORE_APP_INFOS);
            // ...and (re)create the updated versions
            onCreate(sqLiteDatabase);
        }
    }

    // factory method, which initializes this class
    public static synchronized SqliteDBHelper getInstance(Context ctx) {
        // only one instance of this class can exists
        if (sqliteDBHelper_instance == null) {
            sqliteDBHelper_instance = new SqliteDBHelper(ctx, SqliteDBStructure.DATABASE_NAME, null, SqliteDBStructure.VERSION);
        }
        return sqliteDBHelper_instance;
    }

    // get instance of the readable database
    synchronized SQLiteDatabase openSqlDatabaseReadable() {
        return (sqLiteDatabase == null? sqLiteDatabase = getReadableDatabase(): sqLiteDatabase);
    }

    // get instance of the writable database
    private synchronized SQLiteDatabase openSqlDatabaseWritable() {
        return (sqLiteDatabase == null? sqLiteDatabase = getWritableDatabase(): sqLiteDatabase);
    }

    // insert data into the DB
    void insertIntoDataAggregate(String packageName, int permission, Date accessTime, int appState) {
        // Gets the data repository in write mode
        SQLiteDatabase db = openSqlDatabaseWritable();

        db.beginTransaction();
        try {
            // Create a new map of values, where column names are the keys
            ContentValues contentValues = new ContentValues();

            contentValues.put(SqliteDBStructure.PACKAGE_NAME, packageName);
            contentValues.put(SqliteDBStructure.PERMISSION, permission);
            contentValues.put(SqliteDBStructure.ACCESS_TIME, Utility.standardDateTime(accessTime));
            contentValues.put(SqliteDBStructure.APP_STATE, appState);
            // insert this data set into the DB or replace it idf it is already in the database
            db.insertWithOnConflict(SqliteDBStructure.DATA_AGGREGATE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

            // clear the map
            contentValues.clear();
            contentValues.put(SqliteDBStructure.PACKAGE_NAME, packageName);

            db.insertWithOnConflict(SqliteDBStructure.APPS_LOGGED_AT_SCAN_TIME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // insert data into the DB
    void insertIntoInfoAtAccessTime(Date accessTime, int screenState, float screenOrientation, int closeToObject) {
        // Gets the data repository in write mode
        SQLiteDatabase db = openSqlDatabaseWritable();

        db.beginTransaction();
        try {
            // Create a new map of values, where column names are the keys
            ContentValues contentValues = new ContentValues();

            contentValues.put(SqliteDBStructure.ACCESS_TIME, Utility.standardDateTime(accessTime));
            contentValues.put(SqliteDBStructure.SCREEN_STATE, screenState);
            contentValues.put(SqliteDBStructure.SCREEN_ORIENTATION, screenOrientation);
            contentValues.put(SqliteDBStructure.CLOSE_TO_OBJECT, closeToObject);

            db.insertWithOnConflict(SqliteDBStructure.INFO_AT_ACCESSTIME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void insertIntoDataAnalyzingBulk(ArrayList<Datamining.DataInfo> temp) {
        // Gets the data repository in write mode
        SQLiteDatabase db = openSqlDatabaseWritable();

        db.beginTransaction();
        try {
            // Create a new map of values, where column names are the keys
            ContentValues contentValues = new ContentValues();

            for (Datamining.DataInfo info: temp) {
                // add all values
                contentValues.put(SqliteDBStructure.PACKAGE_NAME, info.packageName);
                contentValues.put(SqliteDBStructure.PERMISSION, info.permission);
                contentValues.put(SqliteDBStructure.ACCESS_TIME, info.accessTime);
                contentValues.put(SqliteDBStructure.COUNT, info.permissionCount);
                contentValues.put(SqliteDBStructure.IS_ANOMALOUS, info.isAnomalous);
                contentValues.put(SqliteDBStructure.ANOMALY_REASON, info.anomalyReason);
                contentValues.put(SqliteDBStructure.SCAN_TIME, info.scanTime);
                contentValues.put(SqliteDBStructure.REPORTED, info.reported);

                // insert into db
                db.insertWithOnConflict(SqliteDBStructure.DATA_ANALYZING, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void insertIntoScanTimesLogged(Date scanTime) {
        // Gets the data repository in write mode
        SQLiteDatabase db = openSqlDatabaseWritable();

        db.beginTransaction();
        try {
            // Create a new map of values, where column names are the keys
            ContentValues contentValues = new ContentValues();
            contentValues.put(SqliteDBStructure.SCAN_TIME, Utility.standardDateTime(scanTime));

            db.insertWithOnConflict(SqliteDBStructure.SCAN_TIMES_LOGGED, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void insertIntoScannedAppsBulk(ArrayList<Datamining.DataInfo> tmp) {
        // Gets the data repository in write mode
        SQLiteDatabase db = openSqlDatabaseWritable();

        db.beginTransaction();
        try {
            for (Datamining.DataInfo info: tmp) {
                // Create a new map of values, where column names are the keys
                ContentValues contentValues = new ContentValues();

                contentValues.put(SqliteDBStructure.PACKAGE_NAME, info.packageName);
                contentValues.put(SqliteDBStructure.SCAN_TIME, info.scanTime);

                db.insertWithOnConflict(SqliteDBStructure.SCANNED_APPS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // method, which returns a cursor object with the data
    // specified in the query
    // NOTE: Only use SELECT Statements, which return data (NO UPDATE, INSERT etc)
    Cursor get_data(String query) {

        SQLiteDatabase db = openSqlDatabaseReadable();

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null) {
                return db.rawQuery(query, null);
            } else {
                return null;
            }
        }
    }

    // get the amount of rows in the DB specified by a sql query
    int entries(String query) {
        int result;

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null) {
                result = cursor.getCount();
            } else {
                return 0;
            }
        }
        return result;
    }

    //Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
    public void query(String query) {
        // Gets the data repository in write mode
        SQLiteDatabase db = openSqlDatabaseWritable();
        // insert into database
        db.beginTransaction();
        db.execSQL(query);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Runs the provided SQL and returns an array list:
    // DO NOT CALL THIS METHOD, IF YOU MAKE A SELECT ON MULTIPLE COLUMNS
    ArrayList query_select_list(String query) {
        // Gets the data repository in read mode
        SQLiteDatabase db = openSqlDatabaseReadable();
        // select from database
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, null);
        db.setTransactionSuccessful();
        db.endTransaction();

        if (cursor != null)
            return cursorToList(cursor);
        else
            try {
                throw new SQLException();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return null;
    }

    @SuppressWarnings("unchecked")
    private ArrayList cursorToList(Cursor cursor) {
        ArrayList<String> result = new ArrayList();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                result.add(cursor.getString(0));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    // delete the entries of the raw data tables after successful data evaluation (data mining)
    void deleteRawDataTables () {
        query("DELETE FROM " + SqliteDBStructure.DATA_AGGREGATE);
        query("DELETE FROM " + SqliteDBStructure.INFO_AT_ACCESSTIME);
        query("DELETE FROM " + SqliteDBStructure.APPS_LOGGED_AT_SCAN_TIME);
    }

    // method, which deletes older scan entries, depending on the set scan deletion time
    void deleteOlderScans () {
        // delete scan unless the scan deletion index is 3 (equivalent to never)
        if (ScanSettings.indexScanDeletion != 3) {
            Date date = new Date((System.currentTimeMillis() - (ScanSettings.scanDeletionInMS[ScanSettings.indexScanDeletion])));
            query("DELETE FROM " + SqliteDBStructure.DATA_ANALYZING + " WHERE " + SqliteDBStructure.SCAN_TIME + " <= " + "DATETIME(" + "'" + Utility.standardDateTime(date) + "'" + ")");
            query("DELETE FROM " + SqliteDBStructure.SCAN_TIMES_LOGGED + " WHERE " + SqliteDBStructure.SCAN_TIME + " <= " + "DATETIME(" + "'" + Utility.standardDateTime(date) + "'" + ")");
        }
    }
}