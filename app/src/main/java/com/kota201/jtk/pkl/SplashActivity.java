package com.kota201.jtk.pkl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kota201.jtk.pkl.service.UserLocationUpdate;
import com.onesignal.OneSignal;

/**
 * Created by AdeFulki on 5/27/2017.
 */

public class SplashActivity extends Activity{
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        startService(new Intent(this, UserLocationUpdate.class));

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
                        int role = prefs.getInt("role", 0);
                        Log.i("test-role",String.valueOf(role));
                        if (role == 1){
                            //tampilan pedagang
                        }else if (role == 2){
                            startActivity(new Intent(SplashActivity.this, LokasiPedagangMemberActivity.class));
                            finish();
                            //tampilan pembeli
                        } else {
                            startActivity(new Intent(SplashActivity.this, LokasiPedagangActivity.class));
                            finish();
                        }

                    }
                }, SPLASH_TIME_OUT);
    }
}
