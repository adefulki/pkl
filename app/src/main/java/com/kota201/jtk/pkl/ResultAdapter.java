package com.kota201.jtk.pkl;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kota201.jtk.pkl.model.Search;
import com.kota201.jtk.pkl.restful.PostMethod;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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

        holder.tv1.setOnClickListener(clickListener);
        holder.tv2.setOnClickListener(clickListener);
        holder.imageView.setOnClickListener(clickListener);
        holder.imageView2.setOnClickListener(clickListener);
        holder.tv1.setTag(holder);
        holder.imageView.setTag(holder);

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
            Toast.makeText(context, "Menu ini berada di posisi " + position, Toast.LENGTH_LONG).show();
        }
    };


    public void addItem(){
        assert mJsonObj != null;
        try {
            filter = mJsonObj.getInt("filter");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PostMethod postMethod = (PostMethod) new PostMethod().execute(
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
}
