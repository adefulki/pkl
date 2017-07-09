package com.kota201.jtk.pkl;

import android.app.Activity;
import android.content.Context;
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
                        if (isNetworkConnected() == true) {
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
                        }else{
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }, SPLASH_TIME_OUT);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
