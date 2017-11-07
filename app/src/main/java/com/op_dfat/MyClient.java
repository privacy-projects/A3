package com.op_dfat;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/*
 * Author: Daniel Mattes
 */

class MyClient {

    private static String serverURL = "http://dfat.cs.hs-rm.de:47381/";

    private static ArrayList<ArrayList<String>> blacklist = null;
    private static ArrayList<BlackListFetchedListener> blackListFetchedListeners = new ArrayList<>();

    static void subscribe(BlackListFetchedListener blackListFetchedListener) {
        if (!blackListFetchedListeners.contains(blackListFetchedListener)) {
            blackListFetchedListeners.add(blackListFetchedListener);
        }
    }

    static String POST_rawdata(String app_name, String opid_name, String time_stamp, String app_running, String screen_state, String screen_orientation, String close_to_object, String up_Time){

        JSONObject json = new JSONObject();
        try {
            json.put("app_name", app_name);
            json.put("opid_name", opid_name);
            json.put("time_stamp", time_stamp);
            json.put("app_running", app_running);
            json.put("screen_state", screen_state);
            json.put("screen_orientation", screen_orientation);
            json.put("close_to_object", close_to_object);
            json.put("up_Time", up_Time);

            // TODO
            // Write to log
            Utility.appendLog("-> rawdata.php: " + json.toString());
//            new SendEntry().execute(serverURL+"rawdata.php", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return "POST failed: exception building JSON";
        }
        return "POST failed: exception building JSON";
        //        return json.toString();
    }

    static String POST_Anomaly(String app_name, String resource, String reason, String reported){

        JSONObject json = new JSONObject();
        try {
            json.put("app_name", app_name);
            json.put("resource", resource);
            json.put("reason", reason);
            json.put("reported", reported);

            // TODO
            // Write to log
            Utility.appendLog("-> anomaly.php: " + json.toString());

//            new SendEntry().execute(serverURL + "anomaly.php", json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            return "POST failed: exception building JSON";
        }
        return "POST failed: exception building JSON";
//        return json.toString();
    }

    static String POST_Name(String appName){
        JSONObject json = new JSONObject();
        try {
            json.put("app_name", appName);

            // TODO
            // Write to log
            Utility.appendLog("-> storeNames.php: " + json.toString());

//            new SendEntry().execute( serverURL + "storeNames.php", json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            return "POST failed: exception building JSON";
        }
        return "POST failed: exception building JSON";
//        return json.toString();
    }

    static String updateBlacklist(){
        try{
            // TODO
            // Write to log
            Utility.appendLog("-> updateBlacklist.php");
//            new UpdateData().execute(serverURL + "updateBlacklist.php");
        }catch(Exception e){
            e.printStackTrace();
            return "Update failed.";
        }
        return "done.";
//        return "Update completed.";
    }

    static String fetchBlacklist(){
        try{
            // TODO
            Utility.appendLog("-> getBlacklist.php");
//            new FetchBlacklist().execute( serverURL+"getBlacklist.php");
        }catch(Exception e){
            e.printStackTrace();
            return "Couldn't fetch Blacklist";
        }
        return "done";
//        return "Fetch completed.";
    }

    static ArrayList<ArrayList<String>> getBlacklist(){
        return blacklist != null ? blacklist : new ArrayList<ArrayList<String>>();
    }

    //calls the script at the given URL
    private static class UpdateData extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params){
            HttpURLConnection conn = null;

            try{
                // Write to log
                Utility.appendLog("UpdateData: " + params[0]);

                URL url;
                url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();

                return "Done";
            } catch(IOException e){
                e.printStackTrace();
            }finally{
                if(conn != null){
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    //sends a post-request to the URL in params[0] with parameters from params[1]
    private static class SendEntry extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                // Write to log
                Utility.appendLog("SendEntry: \n" + params[0] + "\n" + params[1]);

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoOutput(true);

                DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
                dos.writeBytes("PostData=" + params[1]);
                dos.flush();
                dos.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    //fetches the blacklist from the server at the given URL
    private static class FetchBlacklist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                // Write to log
                Utility.appendLog("FetchBlacklist: \n" + params[0]);

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoOutput(true);

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
                data = data.replaceAll("[{\\}\"]","");

                ArrayList<ArrayList<String>> result = new ArrayList<>();
                String[] apps = data.split(",");
                String[] tmp;
                ArrayList<String> tmpArray;
                for (String app : apps) {
                    tmp = app.split(":");
                    tmpArray = new ArrayList<>();
                    tmpArray.add(tmp[0]);
                    tmpArray.add(tmp[1]);
                    result.add(tmpArray);
                }
                blacklist = result;

                // Write to log
                Utility.appendLog("FetchBlacklist (result): \n" + result.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return "done";
        }

        //calls all subscribed BlackListFetchedListeners
        @Override
        protected void onPostExecute(String result) {
            if (result.equals("done")) {
                for (BlackListFetchedListener listener : blackListFetchedListeners) {
                    listener.onBlackListFetched();
                }
            }
        }
    }

    interface BlackListFetchedListener {
        void onBlackListFetched();
    }
}
