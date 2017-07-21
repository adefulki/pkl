package com.kota201.jtk.pkl;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kota201.jtk.pkl.model.Produk;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class ProdukAdapter extends RecyclerView.Adapter<ProdukViewHolder>{

    ArrayList<Produk> listProduk = new ArrayList<>();
    private final Context context;
    private final JSONObject mJsonObj;

    LayoutInflater inflater;
    public ProdukAdapter(Context context, JSONObject jsonObj) {
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.mJsonObj = jsonObj;
        addItem();
    }

    @Override
    public ProdukViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflater.inflate(R.layout.item_produk, parent, false);
        ProdukViewHolder viewHolder=new ProdukViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ProdukViewHolder holder, int position) {
        Produk produk = listProduk.get(position);
        holder.namaProduk.setText(produk.getNamaProduk());
        holder.deskripsiProduk.setText(produk.getDeskripsiProduk());
        holder.hargaProduk.setText(produk.getHargaProduk());
        holder.satuanProduk.setText(produk.getSatuanProduk());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://carmate.id/assets/image/" + produk.getFotoProduk(), holder.fotoProduk);
        holder.editButton.setOnClickListener(editClickListener);
    }

    View.OnClickListener editClickListener=new View.OnClickListener() {
        @BindView(R.id.fotoProduk)
        ImageView fotoProduk;
        @BindView(R.id.namaProduk)
        TextView namaProduk;
        @BindView(R.id.deskripsiProduk)
        TextView deskripsiProduk;
        @BindView(R.id.hargaProduk)
        TextView hargaProduk;
        @BindView(R.id.satuanProduk)
        TextView satuanProduk;
        @Override
        public void onClick(View v) {

            View layout = inflater.inflate(R.layout.dialog_produk, (ViewGroup) v.findViewById(R.id.container));
            ButterKnife.bind(this,layout);

            //Building dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(layout);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //save info where you want it
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
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
