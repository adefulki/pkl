package com.kota201.jtk.pkl;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kota201.jtk.pkl.model.Search;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AdeFulki on 7/14/2017.
 */

public class ResultAdapter extends  RecyclerView.Adapter<ResultViewHolder>{
    //deklarasi variable context
    ArrayList<Search> listSearch = new ArrayList<>();
    private final Context context;
    private final JSONObject mJsonObj;
    private int filter;

    LayoutInflater inflater;
    public ResultAdapter(Context context, JSONObject jsonObj) {
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.mJsonObj = jsonObj;
        addItem();
    }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflater.inflate(R.layout.item_result_search, parent, false);
        ResultViewHolder viewHolder=new ResultViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        Search search = listSearch.get(position);
        holder.tv1.setText(search.getNama());
        holder.tv2.setText(new DecimalFormat("##.##").format(search.getJarakHaversine()/1000) + " km");
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(context).build());
        switch (filter){
            case 1 : {
                imageLoader.displayImage("drawable://" + R.drawable.ic_dagangan, holder.imageView);
                break;
            }
            case 2 :{
                imageLoader.displayImage("drawable://" + R.drawable.ic_produk_food, holder.imageView);
                break;
            }
            case 3 :{
                imageLoader.displayImage("drawable://" + R.drawable.ic_pedagang, holder.imageView);
                break;
            }
            default:
                break;
        }

        imageLoader.displayImage("http://carmate.id/assets/image/" + search.getFoto(), holder.imageView2);

        holder.card_view.setOnClickListener(clickListener);
        holder.card_view.setTag(holder);

    }

    @Override
    public int getItemCount() {
        return listSearch.size();
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //member aksi saat cardview diklik berdasarkan posisi tertentu
            ResultViewHolder vholder = (ResultViewHolder) v.getTag();
            int position = vholder.getPosition();
            Search search = listSearch.get(position);
            Intent intent = new Intent(context, DetailPedagangActivity.class);
            intent.putExtra("idDagangan", search.getId());
            context.startActivity(intent);
        }
    };


    public void addItem(){
        assert mJsonObj != null;
        try {
            filter = mJsonObj.getInt("filter");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        searching postMethod = (searching) new searching().execute(
                "http://carmate.id/index.php/Pencarian_controller/searchKataKunci",
                mJsonObj.toString()
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
    }

    public class searching extends AsyncTask<String, Void, String> {

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
}
