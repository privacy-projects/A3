package com.op_dfat;

import android.content.Context;
import java.util.ArrayList;

/**
 * Created by Marvin Duchmann
 */

class RuleSet {
    //Definition kritischer Resourcen
    private static ArrayList<String> criticalRecources; static {
        {
            criticalRecources = new ArrayList<>();
            criticalRecources.add("OP_RECORD_AUDIO");
            criticalRecources.add("OP_CAMERA");
            criticalRecources.add("OP_FINE_LOCATION");
            criticalRecources.add("OP_READ_CONTACTS");
            criticalRecources.add("OP_COARSE_LOCATION");
        }
    };

    static ArrayList<String> checkForAnomaly(String appName, String resource, long count, int appState, int screenState, int screenOrientation, int closeToObject, double scanDuration, Context context){
        ArrayList<String> results = new ArrayList<>();
        String anomalyText = "";
        //RULESETS
        //Bedingungen für Anomalien prüfen und Anomalietext konkatinieren
        //Prüfbedingung (Whatsapp und Microfon gibt Anomalie)
        if ((appName.equals("com.whatsapp")) && (resource.equals("OP_RECORD_AUDIO"))) {
            anomalyText += "A test anomaly, thrown whenever you use the microphone in whatsapp";
        }
        //App im Hintergrund, kritische Resource
        if ((appState == 1 || appState == 0) && criticalRecources.contains(resource)){
            anomalyText += "App was in Background but accessed crit. Resource";
        }

        //if (!Utility.isSystemApp(context, appName) && !Utility.isAppPreLoaded(context, appName) && (screenOrientation < -3) && (screenState == 0) && criticalRecources.contains(resource)){
        if ((screenOrientation < -3) && (screenState == 0) && criticalRecources.contains(resource)){
            anomalyText += "The App accessed a critical resource, while the display was off and facing downwards";
        }
        if ((criticalRecources.contains(resource)) && (screenState == 0) && !(closeToObject == 0) && !(resource.equals("RECORD_AUDIO"))){
            anomalyText += "Screen was off and critical resource was accessed";
        }
        if (resource.equals("ACTIVATE_VPN")){
            anomalyText += "The App activated a VPN, your Internet traffic may not be save";
        }
        //if (appState < 1 && !Utility.isSystemApp(context, appName) && !Utility.isAppPreLoaded(context, appName)){
        if (appState < 1){
            anomalyText += "This App accessed a resource while in background or inactive";
        }
        //Text leer --> keine Anomalie gefunden
        if (anomalyText.equals("")) {
            anomalyText += "No anomalous access at that time";
            results.add("0");
        } else {
            results.add("1");
        }
        results.add(anomalyText);
        // 1 für Anomalie, 0 für keine Anomalie + Anomalietext
        return results;
    }
}
