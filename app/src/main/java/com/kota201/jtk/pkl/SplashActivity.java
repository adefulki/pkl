package com.kota201.jtk.pkl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import butterknife.BindString;

/**
 * Created by AdeFulki on 5/27/2017.
 */

public class SplashActivity extends Activity{
    private static int SPLASH_TIME_OUT = 2000;

    @BindString(R.string.my_prefs) String my_prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                            SharedPreferences prefs = getSharedPreferences(my_prefs, MODE_PRIVATE);
                            String restoredText = prefs.getString("text", null);
                            if (restoredText != null) {
                                int role = prefs.getInt("role", 0);
                                if (role == 0){
                                    //tampilan pedagang
                                }else if (role == 1){
                                    //tampilan pembeli
                                }
                            }else {
                                startActivity(new Intent(SplashActivity.this, LokasiPedagangActivity.class));
                                finish();
                            }

                    }
                }, SPLASH_TIME_OUT);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void createDialog() {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(this);
        errorDialog.setTitle("Error");
        errorDialog.setMessage("No internet connection.");
        errorDialog.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                        dialog.dismiss();
                    }
                });

        errorDialog.create();
    }


}
