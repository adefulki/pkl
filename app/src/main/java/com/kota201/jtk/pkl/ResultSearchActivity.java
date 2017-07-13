package com.kota201.jtk.pkl;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kota201.jtk.pkl.model.Search;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AdeFulki on 7/12/2017.
 */


public class ResultSearchActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    ArrayList<Search> listSearch = new ArrayList<>();

    private String query;
    private int filter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.result_search);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            query = extras.getString("query");
            filter = extras.getInt("filter");
            Log.i("query",query);
            Log.i("filter", String.valueOf(filter));
            String userQuery = extras.getString("userQuery");

        }

        JSONObject dataToSend = null;

        try {
            dataToSend = new JSONObject()
                    .put("kataKunci", query)
                    .put("latPembeli", "-6.9423305")
                    .put("lngPembeli", "107.6466958")
                    .put("filter",filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert dataToSend != null;
        PostMethod postMethod = (PostMethod) new PostMethod().execute(
                "http://carmate.id/index.php/Pencarian_controller/searchKataKunci",
                dataToSend.toString()
                );
        String jsonData = null;
        JSONArray Jobject = null;

        try {
            jsonData = postMethod.get();
            Jobject = new JSONArray(jsonData);
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < Jobject.length(); i++) {
            JSONObject c = null;
            Search search = new Search();
            try {
                c = Jobject.getJSONObject(i);
                search.setId(c.getString("id"));
                search.setNama(c.getString("nama"));
                search.setFoto(c.getString("foto"));
                search.setJarakHaversine(c.getDouble("jarakHaversine"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listSearch.add(search);
        }

        final ListView listview = (ListView) findViewById(R.id.listview);
        final ArrayList<String> list = new ArrayList<String>();
        for (Search search:listSearch
             ) {
            list.add(search.getNama());

        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
    }

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

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
