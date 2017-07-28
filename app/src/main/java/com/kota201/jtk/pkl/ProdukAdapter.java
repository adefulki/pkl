package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.developers.smartytoast.SmartyToast;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kota201.jtk.pkl.model.Produk;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import it.sauronsoftware.ftp4j.FTPClient;
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
    private ImageView fotoProduk;
    private ImageLoader imageLoader;
    private Uri imageUri;

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
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics())))
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://carmate.id/assets/image/" + produk.getFotoProduk(), holder.fotoProduk, options);
        holder.editButton.setOnClickListener(editClickListener);
        holder.editButton.setTag(holder);
    }

    View.OnClickListener editClickListener =new View.OnClickListener(){
        Boolean statusEditFoto;
        @Override
        public void onClick(View v) {
            ProdukViewHolder vholder = (ProdukViewHolder) v.getTag();
            int position = vholder.getPosition();

            statusEditFoto = false;

            imageLoader = ImageLoader.getInstance();
            imageLoader.init(new ImageLoaderConfiguration.Builder(context).build());
            L.disableLogging();

            final View layout = inflater.inflate(R.layout.dialog_produk, (ViewGroup) v.findViewById(R.id.container));
            fotoProduk = (ImageView) layout.findViewById(R.id.fotoProduk);
            final EditText namaProduk = (EditText) layout.findViewById(R.id.namaProduk);
            final EditText deskripsiProduk = (EditText) layout.findViewById(R.id.deskripsiProduk);
            final EditText hargaProduk = (EditText) layout.findViewById(R.id.hargaProduk);
            final EditText satuanProduk = (EditText) layout.findViewById(R.id.satuanProduk);


            fotoProduk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PickSetup setup = new PickSetup()
                            .setTitle("Pilih")
                            .setCancelText("Batal")
                            .setCameraButtonText("Kamera")
                            .setGalleryButtonText("Gallery");
                    PickImageDialog.build(setup)
                            .setOnPickResult(new IPickResult() {
                                @Override
                                public void onPickResult(PickResult pickResult) {
                                    if (pickResult.getError() == null) {
                                        statusEditFoto = true;
                                        imageLoader.displayImage(String.valueOf(pickResult.getUri()), fotoProduk);
                                        imageUri = pickResult.getUri();
                                    } else {
                                        //Handle possible errors
                                        //TODO: do what you have to do with r.getError();
                                        SmartyToast.makeText(context,pickResult.getError().getMessage(),SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
                                    }
                                }})
                            .show((FragmentActivity) context);
                }
            });

            final Produk produk = listProduk.get(position);
            imageLoader.displayImage("http://carmate.id/assets/image/" + produk.getFotoProduk(), fotoProduk);

            namaProduk.setText(produk.getNamaProduk());
            deskripsiProduk.setText(produk.getDeskripsiProduk());
            hargaProduk.setText(produk.getHargaProduk());
            satuanProduk.setText(produk.getSatuanProduk());

            //Building dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(layout);
            builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    editProduk editProdukTask = (editProduk) new editProduk("ftp.carmate.id",
                            "pkl@carmate.id",
                            "Kam1selalu1",
                            "assets/image/",
                            produk.getIdProduk(),
                            namaProduk.getText().toString(),
                            deskripsiProduk.getText().toString(),
                            hargaProduk.getText().toString(),
                            satuanProduk.getText().toString(),
                            produk.getFotoProduk(),
                            statusEditFoto).execute();
                    try {
                        Log.i("test",editProdukTask.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(context.getResources()
                    .getColor(R.color.colorSecondary));
            Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(context.getResources()
                    .getColor(R.color.colorSecondary));
        }
    };

    @Override
    public int getItemCount() {
        return listProduk.size();
    }

    public void addItem(){
        listProduk.clear();
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

    class editProduk extends AsyncTask<Void, Void, String> {

        private final String mHost;
        private final String mUsername;
        private final String mPassword;
        private final String mDir;
        private final String mIdProduk;
        private final String mNamaProduk;
        private final String mDeskripsiProduk;
        private final String mHargaProduk;
        private final String mSatuanProduk;
        private final String mfotoProduk;
        private final Boolean mStatusEditFoto;
        private ProgressDialog prg;

        editProduk(String host, String username, String password, String dir, String idProduk, String namaProduk, String deskripsiProduk, String hargaProduk, String satuanProduk, String fotoProduk, Boolean statusEditFoto) {
            mHost = host;
            mUsername = username;
            mPassword = password;
            mDir = dir;
            mIdProduk = idProduk;
            mNamaProduk = namaProduk;
            mDeskripsiProduk = deskripsiProduk;
            mHargaProduk =hargaProduk;
            mSatuanProduk = satuanProduk;
            mfotoProduk = fotoProduk;
            mStatusEditFoto = statusEditFoto;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            prg = new ProgressDialog(context);
            prg.setMessage("Editing...");
            prg.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            FTPClient client = new FTPClient();

            try {
                String name = mfotoProduk;
                if (mStatusEditFoto){
                    client.connect(mHost,21);
                    client.login(mUsername, mPassword);
                    client.setType(FTPClient.TYPE_BINARY);
                    client.changeDirectory(mDir);
                    Bitmap imageBitmap = SiliCompressor.with(context).getCompressBitmap(String.valueOf(imageUri));
                    File filesDir = context.getFilesDir();
                    SecureRandom random = new SecureRandom();
                    name = new BigInteger(130, random).toString(32);
                    name = name+".jpg";
                    File imageFile = new File(filesDir, name);

                    OutputStream os;
                    try {
                        os = new FileOutputStream(imageFile);
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                    }
                    client.upload(imageFile);
                }

                try {
                    // Simulate network access.
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return null;
                }

                Log.i("test","sudah upload");

                JSONObject dataToSend = null;
                try {
                    dataToSend = new JSONObject()
                            .put("idProduk", mIdProduk)
                            .put("namaProduk", mNamaProduk)
                            .put("deskripsiProduk", mDeskripsiProduk)
                            .put("fotoProduk", name)
                            .put("hargaProduk", mHargaProduk)
                            .put("satuanProduk", mSatuanProduk);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("test","sudah membuat json");
                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Produk_controller/editProdukDagangan")
                        .post(RequestBody.create(JSON, dataToSend.toString()))
                        .build();

                OkHttpClient okClient = new OkHttpClient();
                //Make the request
                try {
                    okClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("test","selesai request");

                return new String("Upload Successful");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    client.disconnect(true);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                String t="Failure : " + e.getLocalizedMessage();
                return t;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            prg.dismiss();
            addItem();
            ProdukAdapter.this.notifyDataSetChanged();
        }
    }
}
