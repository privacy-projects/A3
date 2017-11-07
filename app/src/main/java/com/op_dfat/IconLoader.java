package com.op_dfat;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import de.halfreal.googleplayscraper.api.GooglePlayApi;
import de.halfreal.googleplayscraper.api.HumanRequestBehavior;
import de.halfreal.googleplayscraper.model.App;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Arrigo Paterno
 */

class IconLoader {

    // list which holds the black list data
    private static List<Data> playStoreData = new ArrayList<>();

    static Data loadingPlayStoreData(final String packageName) {

        // first check, whether the requested app is already cached in playStoreData
        for (Data data : playStoreData) {
            if (data.packageName.equals(packageName)) {
                return data;
            }
        }

        // scrap the play store for the requested app title and the app icon url...
        final String[] url;
        url = new String[]{""};
        final String[] name;
        name = new String[]{""};
        final Data[] temp = new Data[1];

        GooglePlayApi googlePlayApi = new GooglePlayApi(new HumanRequestBehavior());
        rx.Observable<List<App>> observable = googlePlayApi.search(packageName, "de", "de", 3);

        Subscription sub = observable.filter(new Func1<List<App>, Boolean>() {
            @Override
            public Boolean call(List<App> apps) {
                for (App app:apps) {
                    if (app.getAppId().contains(packageName)) {
                        return true;
                    }
                }
                return false;
            }
        }).subscribe(new Action1<List<App>>() {
            @Override
            public void call(List<App> apps) {
                for (App app: apps) {
                    if (app.getAppId().contains(packageName)) {
                        // get the necessary information store them in a Data object and add them to the playStoreData list
                        url[0] = app.getIcon();
                        name[0] = app.getTitle();
                        temp[0] = new Data(packageName, app.getTitle(), app.getIcon().startsWith("http")?app.getIcon():"http:" +app.getIcon());
                        if (!playStoreData.contains(temp[0])) {
                            playStoreData.add(temp[0]);
                        }
                        break;
                    }
                }
            }
        });
        // return the data object, which contains the information about icon and title
        sub.unsubscribe();
        return temp[0];
    }

    // method, which stores the information in a local DB
    static void savePlayStoreData(Context context) {

        SqliteDBHelper sqliteDBHelper = SqliteDBHelper.getInstance(context);

        for (Data data: playStoreData) {
            String packageName = data.packageName;
            String appTitle = data.appName;
            String iconURL = data.iconURL;
            sqliteDBHelper.query("INSERT OR REPLACE INTO " + SqliteDBStructure.PLAY_STORE_APP_INFOS + " VALUES (" + "'" + packageName + "'" +", " + "'" + appTitle +  "'" + ", " +  "'" + iconURL + "'" + ");");
        }

    }

    // method, which loads the latest black list information from a local DB an caches it in playStoreData
    static void loadPlayStoreData(Context context) {

        if (playStoreData == null || playStoreData.isEmpty()) {
            Cursor cursor = SqliteDBHelper.getInstance(context).get_data("SELECT * FROM " + SqliteDBStructure.PLAY_STORE_APP_INFOS);

            if (cursor != null) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Data tmp = new Data(cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.PACKAGE_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.APP_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(SqliteDBStructure.ICON_URL)));

                    playStoreData.add(tmp);
                    cursor.moveToNext();
                }
            }
        }
    }

    static class Data {
        public String packageName;
        public String appName;
        String iconURL;

        Data(String packageName, String appName, String iconURL) {
            this.packageName = packageName;
            this.appName = appName;
            this.iconURL = iconURL;
        }
    }

}
