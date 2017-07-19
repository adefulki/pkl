package com.kota201.jtk.pkl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.schibstedspain.leku.LocationPickerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import it.sauronsoftware.ftp4j.FTPClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AdeFulki on 7/19/2017.
 */

public class SettingLokasiPedagang extends AppCompatActivity{

    private String id;
    private Double latitude;
    private Double longitude;
    private String address;
    private String postalcode;
    private String idDagangan;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        try {
            idDagangan = new getIdDagangnTask(id).execute().get();
            prefs.edit().putString("idDagangan", idDagangan);
            prefs.edit().apply();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (ActivityCompat.checkSelfPermission(SettingLokasiPedagang.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SettingLokasiPedagang.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        Intent intent = new LocationPickerActivity.Builder()
                .withLocation(latitude, longitude)
                .shouldReturnOkOnBackPressed()
                .withSatelliteViewHidden()
                .build(getApplicationContext());

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
                Log.d("LATITUDE****", String.valueOf(latitude));
                longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
                Log.d("LONGITUDE****", String.valueOf(longitude));
                address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
                Log.d("ADDRESS****", String.valueOf(address));
                postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE);
                Log.d("POSTALCODE****", String.valueOf(postalcode));

                new updateLokasiTask(idDagangan, latitude, longitude).execute();

                startActivity(new Intent(SettingLokasiPedagang.this, SettingProdukDagangan.class));
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                startActivity(new Intent(SettingLokasiPedagang.this, null));
            }
        }
    }

    class updateLokasiTask extends AsyncTask<Void, Void, Void> {

        private final String mIdDagangan;
        private final Double mLatDagangan;
        private final Double mLngDagangan;

        updateLokasiTask(String idDagangan, Double latDagangan, Double lngDagangan) {

            mIdDagangan = idDagangan;
            mLatDagangan = latDagangan;
            mLngDagangan = lngDagangan;
        }

        @Override
        protected Void doInBackground(Void... params) {
            FTPClient client = new FTPClient();

            try {
                JSONObject dataToSend = null;
                try {
                    dataToSend = new JSONObject()
                            .put("idDagangan", mIdDagangan)
                            .put("namaDagangan", mLatDagangan)
                            .put("deskripsiDagangan", mLngDagangan);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Dagangan_controller/updateLokasiDagangan")
                        .post(RequestBody.create(JSON, dataToSend.toString()))
                        .build();

                OkHttpClient okClient = new OkHttpClient();
                //Make the request
                try {
                    okClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("asik", "selesai request");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class getIdDagangnTask extends AsyncTask<Void, Void, String> {

        private final String mId;

        getIdDagangnTask(String id) {

            mId = id;
        }

        @Override
        protected String doInBackground(Void... params) {
            FTPClient client = new FTPClient();

            try {

                JSONObject dataToSend = null;
                try {
                    dataToSend = new JSONObject()
                            .put("idPedagang", mId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Dagangan_controller/getIdDaganganByIdPedagang")
                        .post(RequestBody.create(JSON, dataToSend.toString()))
                        .build();

                OkHttpClient okClient = new OkHttpClient();
                //Make the request
                String jsonData;
                JSONObject Jobject;
                try {
                    Response response = okClient.newCall(request).execute();
                    jsonData = response.body().string();
                    Jobject = new JSONObject(jsonData);
                    return Jobject.getString("idDagangan");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("asik", "selesai request");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
