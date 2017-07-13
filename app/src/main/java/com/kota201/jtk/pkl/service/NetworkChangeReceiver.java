package com.kota201.jtk.pkl.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.developers.smartytoast.SmartyToast;

/**
 * Created by AdeFulki on 7/13/2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (isNetworkAvailable(context)){
            SmartyToast.makeText(context,"Terhubung", SmartyToast.LENGTH_LONG,SmartyToast.CONNECTED);
        }else {
            SmartyToast.makeText(context,"Periksa koneksi internet",SmartyToast.LENGTH_LONG,SmartyToast.WARNING);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
