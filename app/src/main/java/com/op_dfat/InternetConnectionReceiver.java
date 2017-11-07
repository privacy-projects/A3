package com.op_dfat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Created by Arrigo Paterno
 */

public class InternetConnectionReceiver extends BroadcastReceiver {

    // callback, which is called whenever the internet connection changes
    @Override
    public void onReceive(Context context, Intent intent) {
        // send locally saved data to server, when valid internet connection is available
        if (Utility.networking(context)) {
            sendSavedData(context);
        }
    }

    void sendSavedData (final Context context) {

        final SqliteDBHelper sqliteDBHelper = SqliteDBHelper.getInstance(context);

        if (sqliteDBHelper.entries("SELECT * FROM " + SqliteDBStructure.SAVE_SCAN) > 0) {
                // TODO: We need to review this code.
//        if (sqliteDBHelper.entries("SELECT * FROM " + SqliteDBStructure.SAVE_SCAN) > 0 ||
//                sqliteDBHelper.entries("SELECT * FROM " + SqliteDBStructure.CREATE_SAVE_ANOMALIES) > 0) {

            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... voids) {
                    // try send raw data
                    int rawData = sendRawData();
                    // try send found anomalies
                    int anomalies = sendAnomalies();

                    // delete everything in both DBs, if send to server was successful
                    if (rawData > 0) {
                        sqliteDBHelper.query("DELETE FROM " + SqliteDBStructure.SAVE_SCAN);
                    }
                    if (anomalies > 0) {
                        sqliteDBHelper.query("DELETE FROM " + SqliteDBStructure.SAVE_ANOMALIES);
                    }

                    return rawData > 0 || anomalies > 0;
                }

                @Override
                protected void onPostExecute(Boolean dataSend) {
                    if (dataSend) {
                        Toast.makeText(context, context.getResources().getString(R.string.dataSendToServer), Toast.LENGTH_SHORT).show();
                    }
                }

                // send all the saved raw data to the server
                private int sendRawData () {
                    Cursor cursor = sqliteDBHelper.get_data("SELECT * FROM " + SqliteDBStructure.SAVE_SCAN);

                    if (cursor != null) {
                        // if the local DB is empty return 0
                        if (cursor.getCount() == 0) {
                            return 0;
                        }

                        cursor.moveToFirst();

                        while (!cursor.isAfterLast()) {
                            MyClient.POST_rawdata(cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PACKAGE_NAME)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PERMISSION)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.ACCESS_TIME)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.APP_STATE)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.SCREEN_STATE)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.SCREEN_ORIENTATION)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.CLOSE_TO_OBJECT)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.SCAN_TIME)));
                            cursor.moveToNext();
                        }

                        // if not, return 1
                        return 1;
                    } else {
                        // if there is some sort of error, return -1
                        return -1;
                    }
                }

                private int sendAnomalies () {
                    Cursor cursor = sqliteDBHelper.get_data("SELECT * FROM " + SqliteDBStructure.SAVE_ANOMALIES);

                    if (cursor != null) {
                        // if the local DB is empty return 0
                        if (cursor.getCount() == 0) {
                            return 0;
                        }

                        cursor.moveToFirst();
                        // send them to the server
                        while (!cursor.isAfterLast()) {
                            MyClient.POST_Anomaly(cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PACKAGE_NAME)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PERMISSION)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.ANOMALY_REASON)),
                                    cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.FROM_USER)));
                            cursor.moveToNext();
                        }

                        // if not, return 1
                        return 1;
                    } else {
                        // if there is some sort of error, return -1
                        return -1;
                    }
                }
            }.execute();
        }
    }
}
