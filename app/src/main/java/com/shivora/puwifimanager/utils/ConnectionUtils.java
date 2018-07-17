package com.shivora.puwifimanager.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;

import com.andrognito.flashbar.Flashbar;

public class ConnectionUtils {


    private static final String TAG = ConnectionUtils.class.getSimpleName();
    private static final String SSID_PU_CAMPUS = "PU@CAMPUS";

    public static boolean isConnectedToPuWifi(Activity context){
        boolean connected = false;

        if (!haveConnectedWifi(context))
            return false;
        else {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED){
                String ssid = wifiInfo.getSSID();
                //Replace double quotes around SSID with null character
                ssid = ssid.replace("\"","");

                if (ssid.equals(SSID_PU_CAMPUS)){
                    connected = true;
                    Log.d(TAG, "isConnectedToPuWifi: "+connected);
                    return connected;
                }
            }
            Log.d(TAG, "isNotConnectedToPuWifi: "+connected);
            Flashbar.Builder flashbarBuilder = new Flashbar.Builder(context).gravity(Flashbar.Gravity.BOTTOM).duration(2000);
            flashbarBuilder.title("Not connected to "+SSID_PU_CAMPUS)
                    .message("Please connect to "+SSID_PU_CAMPUS)
                    .build().show();
            return connected;
        }
    }

    private static boolean haveConnectedWifi(Activity context){
        boolean haveConnectedWifi = false;
        Flashbar.Builder flashbarBuilder = new Flashbar.Builder(context).gravity(Flashbar.Gravity.BOTTOM).duration(2000);

        //Get connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get NetworkInfo of the active network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //Check if there's an active network and if active, it is a WiFi network.
        if (networkInfo!=null && networkInfo.getTypeName().equalsIgnoreCase("WIFI")){
            if (networkInfo.isConnected()){
                haveConnectedWifi = true;
            }
        }

        //Show error if wifi is not connected
        if (!haveConnectedWifi){
            flashbarBuilder
                    .title("Network not connected!")
                    .message("Please connect to WiFi")
                    .build()
                    .show();
        }

        Log.i(TAG, "haveConnectedWifi: "+haveConnectedWifi);
        return haveConnectedWifi;
    }
}
