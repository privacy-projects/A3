package com.op_dfat;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arrigo Paterno
 */

class BlackListReceiver implements MyClient.BlackListFetchedListener {

    private static BlackListReceiver blackListReceiver = null;
    private static boolean isFetched = false;
    private static boolean isFetching = false;
    private static List<Apps> blackList = new ArrayList<>();
    Context context;

    private BlackListReceiver(Context context) {
        this.context = context;
    }

    static void setupBlackList (Context context) {
        if (blackListReceiver == null) {
            // setup black list receiver and subscribe to MyClient to get the info when the black list has been fetched
            blackListReceiver = new BlackListReceiver(context);
            MyClient.subscribe(blackListReceiver);
        }
        // fetch the black list
        if (Utility.networking(context)) {
            if (!isFetching) {
                setFetched(false);
                setIsFetching(true);
                MyClient.fetchBlacklist();
            } else {
                Toast.makeText(context, R.string.blackListReceiver_fetchingBlackList, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, R.string.blackListReceiver_errorFetching, Toast.LENGTH_SHORT).show();
        }
    }

    private static void loadBlackListInfo() {

        new AsyncTask<Void, Void, List<Apps>>() {

            @Override
            protected List<Apps> doInBackground(Void... voids) {
                // get the now fetched black list
                ArrayList<ArrayList<String>> apps = MyClient.getBlacklist();

                if (apps != null && !apps.isEmpty()) {
                    String packageName;
                    String score;

                    // fills the app_data objects with packageName, appName, appIcon and the blacklistScore matching to the packageName. The rest is not necessary in this case
                    for (int i = 0; i < apps.size(); i++) {

                        packageName = apps.get(i).get(0);
                        score = apps.get(i).get(1);

                        boolean alreadyInBlackList = false;

                        if (blackList != null) {
                            for (Apps app : blackList) {
                                if (app.packageName.equals(packageName)) {
                                    alreadyInBlackList = true;
                                    break;
                                }
                            }
                        }

                        if (alreadyInBlackList) {
                            continue;
                        }

                        IconLoader.Data data = IconLoader.loadingPlayStoreData(packageName);
                        String appName = data.appName;
                        String icon = data.iconURL;

                        blackList.add(new Apps(icon, appName, packageName, score));
                    }
                }
                return blackList;
            }

            @Override
            protected void onPostExecute(List<Apps> appsList) {
                blackList = appsList;
                setIsFetching(false);
                setFetched(true);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static synchronized void setFetched(boolean value) {
        isFetched = value;
    }
    private static synchronized void setIsFetching(boolean value) {
        isFetching = value;
    }

    static List<Apps> getBlackList() {
        return blackList;
    }

    static boolean isFetched() {
        return isFetched;
    }
    static boolean isFetching() {
        return isFetching;
    }

    @Override
    public void onBlackListFetched() {
        loadBlackListInfo();
    }
}
