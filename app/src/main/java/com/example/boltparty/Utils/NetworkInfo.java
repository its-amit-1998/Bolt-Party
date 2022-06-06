package com.example.boltparty;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkInfo {

    static final int WIFI_NETWORK = 1;
    static final int MOBILE_NETWORK = 2;
    static final int NOT_CONNECTED = 1000;

    public static int getNetworkStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return WIFI_NETWORK;
                case ConnectivityManager.TYPE_MOBILE:
                    return MOBILE_NETWORK;
            }
        }

        return NOT_CONNECTED;
    }
}
