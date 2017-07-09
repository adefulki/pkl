package com.kota201.jtk.pkl.restful;

import android.os.AsyncTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by AdeFulki on 6/8/2017.
 */

//params[0] = url
public class GetMethod extends AsyncTask<String, Void, String> {

    OkHttpClient client = new OkHttpClient();

    protected String doInBackground(String... params) {

        Request.Builder builder = new Request.Builder();
        builder.url(params[0]);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}