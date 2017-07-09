package com.kota201.jtk.pkl.restful;

import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AdeFulki on 6/8/2017.
 */

//params[0] = url
//params[1] = string Json
public class PostMethod extends AsyncTask<String, Void, String> {

    OkHttpClient client = new OkHttpClient();

    @Override
    protected String doInBackground(String... params) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        //Create request object
        Request request = new Request.Builder()
                .url(params[0])
                .post(RequestBody.create(JSON, params[1]))
                .build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
