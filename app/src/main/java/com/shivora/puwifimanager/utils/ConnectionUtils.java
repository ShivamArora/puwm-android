package com.shivora.puwifimanager.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;

import com.andrognito.flashbar.Flashbar;

public class ConnectionUtils {


    public static final String TAG = ConnectionUtils.class.getSimpleName();

    public static boolean isConnectedToPuWifi(Activity context){
        Log.i(TAG,haveConnectedWifi(context)+"");
        if (!haveConnectedWifi(context))
            return false;
        else {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        }
        return false;
    }

    private static boolean haveConnectedWifi(Activity context){
        boolean haveConnectedWifi = false;
        Flashbar.Builder flashbarBuilder = new Flashbar.Builder(context).gravity(Flashbar.Gravity.BOTTOM).duration(2000);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //Check if there's an active network and if active, it is a WiFi network.
        if (networkInfo!=null && networkInfo.getTypeName().equalsIgnoreCase("WIFI")){
            if (networkInfo.isConnected()){
                haveConnectedWifi = true;
            }
        }

        if (!haveConnectedWifi){
            //Show "Not connected to WiFi error"
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
