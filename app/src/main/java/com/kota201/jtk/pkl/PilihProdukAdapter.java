package com.kota201.jtk.pkl;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kota201.jtk.pkl.model.Produk;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class PilihProdukAdapter extends RecyclerView.Adapter<PilihProdukViewHolder>{

    ArrayList<Produk> listProduk = new ArrayList<>();
    private final Context context;
    private final JSONObject mJsonObj;

    LayoutInflater inflater;
    public PilihProdukAdapter(Context context, JSONObject jsonObj) {
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.mJsonObj = jsonObj;
        addItem();
    }

    @Override
    public PilihProdukViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflater.inflate(R.layout.item_pilih_produk, parent, false);
        PilihProdukViewHolder viewHolder=new PilihProdukViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PilihProdukViewHolder holder, int position) {
        Produk produk = listProduk.get(position);
        holder.namaProduk.setText(produk.getNamaProduk());
        holder.deskripsiProduk.setText(produk.getDeskripsiProduk());
        holder.hargaProduk.setText(produk.getHargaProduk());
        holder.satuanProduk.setText(produk.getSatuanProduk());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, inflater.getContext().getResources().getDisplayMetrics())))
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://carmate.id/assets/image/" + produk.getFotoProduk(), holder.fotoProduk, options);
        holder.cardView.setOnClickListener(clickListener);
        holder.cardView.setTag(holder);
    }

    View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //member aksi saat cardview diklik berdasarkan posisi tertentu
            PilihProdukViewHolder vholder = (PilihProdukViewHolder) v.getTag();
            int position = vholder.getPosition();
            Produk produk = listProduk.get(position);
            Intent intent;
            intent = new Intent(context, PenilaianProduk.class);
            intent.putExtra("idProduk", produk.getIdProduk());
            intent.putExtra("namaProduk", produk.getNamaProduk());
            intent.putExtra("fotoProduk", produk.getFotoProduk());
            context.startActivity(intent);
        }
    };

    @Override
    public int getItemCount() {
        return listProduk.size();
    }

    public void addItem(){
        assert mJsonObj != null;
        getProduk postMethod = (getProduk) new getProduk().execute(
                "http://carmate.id/index.php/Produk_controller/getProduk",
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
            Produk produk = new Produk();
            try {
                c = Jobject.getJSONObject(i);
                produk.setIdProduk(c.getString("idProduk"));
                produk.setNamaProduk(c.getString("namaProduk"));
                produk.setDeskripsiProduk(c.getString("deskripsiProduk"));
                produk.setHargaProduk(c.getString("hargaProduk"));
                produk.setSatuanProduk(c.getString("satuanProduk"));
                produk.setFotoProduk(c.getString("fotoProduk"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listProduk.add(produk);
        }
    }

    public class getProduk extends AsyncTask<String, Void, String> {

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
