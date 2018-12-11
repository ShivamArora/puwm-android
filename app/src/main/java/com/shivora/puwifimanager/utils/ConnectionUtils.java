package com.shivora.puwifimanager.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.IpSecTransform;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


import com.andrognito.flashbar.Flashbar;
import com.shivora.puwifimanager.R;

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
                if (isPieAndAbove()){
                    connected = true;
                    return connected;
                }
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
            Flashbar.Builder flashbarBuilder = new Flashbar.Builder(context).gravity(Flashbar.Gravity.BOTTOM).duration(2000).backgroundColorRes(R.color.chuck_colorAccent);
            flashbarBuilder.title("Not connected to "+SSID_PU_CAMPUS)
                    .message("Please connect to "+SSID_PU_CAMPUS)
                    .build().show();
            return connected;
        }
    }

    private static boolean isPieAndAbove() {
        return Build.VERSION.SDK_INT>= Build.VERSION_CODES.P;
    }

    private static boolean haveConnectedWifi(Activity context){
        boolean haveConnectedWifi = false;
        Flashbar.Builder flashbarBuilder = new Flashbar.Builder(context).gravity(Flashbar.Gravity.BOTTOM).duration(2000).backgroundColorRes(R.color.chuck_colorAccent);

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
                    .message("Please connect to WiFi and turn off mobile data if facing problems")
                    .build()
                    .show();
        }

        Log.i(TAG, "haveConnectedWifi: "+haveConnectedWifi);
        return haveConnectedWifi;
    }
}
