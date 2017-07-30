package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.developers.smartytoast.SmartyToast;
import com.iceteck.silicompressorr.SiliCompressor;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import faranjit.currency.edittext.CurrencyEditText;
import it.sauronsoftware.ftp4j.FTPClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by AdeFulki on 7/19/2017.
 */

public class SettingProdukDagangan extends AppCompatActivity {

    @BindView(R.id.viewRecycler)
    RecyclerView viewRecycler;
    @BindView(R.id.btnAddProduk)
    FloatingActionButton btnAddProduk;

    private String id;
    private String idDagangan;
    private ImageLoader imageLoader;
    private ProdukAdapter adapter;
    private Uri imageUri;
    private ImageView inputFotoProduk;
    private EditText inputNamaProduk;
    private EditText inputDeskripsiProduk;
    private CurrencyEditText inputHargaProduk;
    private EditText inputSatuanProduk;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_produk_dagangan);

        ButterKnife.bind(this);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(getBaseContext()).build());
        L.disableLogging();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pengaturan Produk");

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);
        idDagangan = prefs.getString("idDagangan", null);

        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", idDagangan);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert dataToSend != null;

        adapter=new ProdukAdapter(this,dataToSend);
        viewRecycler.setAdapter(adapter);

        viewRecycler.setHasFixedSize(true);
        viewRecycler.setLayoutManager(new LinearLayoutManager(this));

        btnAddProduk.setOnClickListener(addClickListener);
    }

    View.OnClickListener addClickListener =new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(new ImageLoaderConfiguration.Builder(getBaseContext()).build());
            L.disableLogging();

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_produk, null);
            inputFotoProduk = (ImageView) layout.findViewById(R.id.inputFotoProduk);
            inputNamaProduk = (EditText) layout.findViewById(R.id.inputNamaProduk);
            inputDeskripsiProduk = (EditText) layout.findViewById(R.id.inputDeskripsiProduk);
            inputHargaProduk = (CurrencyEditText) layout.findViewById(R.id.inputHargaProduk);
            inputSatuanProduk = (EditText) layout.findViewById(R.id.inputSatuanProduk);

            inputFotoProduk.setOnClickListener(new View.OnClickListener() {
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
                                        imageLoader.displayImage(String.valueOf(pickResult.getUri()), inputFotoProduk);
                                        imageUri = pickResult.getUri();
                                    } else {
                                        //Handle possible errors
                                        //TODO: do what you have to do with r.getError();
                                        SmartyToast.makeText(getBaseContext(),pickResult.getError().getMessage(),SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
                                    }
                                }})
                            .show(SettingProdukDagangan.this);
                }
            });

            //Building dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingProdukDagangan.this);
            builder.setView(layout);
            builder.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!validate()) {
                        failed();
                        return;
                    }
                    dialog.dismiss();
                    addProduk addProdukTask = null;
                    try {
                        addProdukTask = (addProduk) new addProduk("ftp.carmate.id",
                                "pkl@carmate.id",
                                "Kam1selalu1",
                                "assets/image/",
                                idDagangan,
                                inputNamaProduk.getText().toString(),
                                inputDeskripsiProduk.getText().toString(),
                                inputHargaProduk.getCurrencyText(),
                                inputSatuanProduk.getText().toString()).execute();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        Log.i("test",addProdukTask.get());
                    } catch (InterruptedException | ExecutionException e) {
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
            nbutton.setTextColor(getBaseContext().getResources()
                    .getColor(R.color.colorSecondary));
            Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(getBaseContext().getResources()
                    .getColor(R.color.colorSecondary));
        }
    };

    class addProduk extends AsyncTask<Void, Void, String> {

        private final String mHost;
        private final String mUsername;
        private final String mPassword;
        private final String mDir;
        private final String mIdDagangan;
        private final String mNamaProduk;
        private final String mDeskripsiProduk;
        private final String mHargaProduk;
        private final String mSatuanProduk;
        private ProgressDialog prg;

        addProduk(String host, String username, String password, String dir, String idDagangan, String namaProduk, String deskripsiProduk, String hargaProduk, String satuanProduk) {
            mHost = host;
            mUsername = username;
            mPassword = password;
            mDir = dir;
            mIdDagangan = idDagangan;
            mNamaProduk = namaProduk;
            mDeskripsiProduk = deskripsiProduk;
            mHargaProduk =hargaProduk;
            mSatuanProduk = satuanProduk;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            prg = new ProgressDialog(SettingProdukDagangan.this);
            prg.setMessage("Editing...");
            prg.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            FTPClient client = new FTPClient();

            try {
                client.connect(mHost,21);
                client.login(mUsername, mPassword);
                client.setType(FTPClient.TYPE_BINARY);
                client.changeDirectory(mDir);
                Bitmap imageBitmap = SiliCompressor.with(getBaseContext()).getCompressBitmap(String.valueOf(imageUri));
                File filesDir = getBaseContext().getFilesDir();
                SecureRandom random = new SecureRandom();
                String name = new BigInteger(130, random).toString(32);
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
                            .put("idDagangan", mIdDagangan)
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
                        .url("http://carmate.id/index.php/Produk_controller/addProduk")
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
            adapter.addItem();
            adapter.notifyDataSetChanged();
        }
    }

    public boolean validate() {
        boolean valid = true;
        String nama = inputNamaProduk.getText().toString();
        String satuan = inputSatuanProduk.getText().toString();
        String harga = null;
        try {
            harga = inputHargaProduk.getCurrencyText();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(nama.isEmpty()){
            inputNamaProduk.setError("Nama tidak boleh kosong");
            valid = false;
        }

        if(satuan.isEmpty()){
            inputSatuanProduk.setError("Satuan tidak boleh kosong");
            valid = false;
        }

        if(harga.isEmpty()){
            inputHargaProduk.setError("Harga tidak boleh kosong");
            valid = false;
        }

        return valid;
    }

    public void failed() {
        SmartyToast.makeText(getApplicationContext(),"Gagal",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.produk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.done){
            startActivity(new Intent(SettingProdukDagangan.this, null));
            finish();
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
