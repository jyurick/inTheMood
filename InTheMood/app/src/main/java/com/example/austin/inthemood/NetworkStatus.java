package com.example.austin.inthemood;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by annaholowaychuk on 2017-04-01.
 */

//Code for this class was taked and modified from http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
// on April 1, 2017

/**
 * Class provides functionality for telling if the device is online or not
 */
public class NetworkStatus {
    private static NetworkStatus instance = new NetworkStatus();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    /**
     * Instantiates NetworkStatus class
     * @param ctx context from the activity requesting the class
     * @return instance of class
     */
    public static NetworkStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    /**
     * Returns whether or not the device is connected to the internet.
     * @return connected True is device is online, False if not
     */
    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

}

