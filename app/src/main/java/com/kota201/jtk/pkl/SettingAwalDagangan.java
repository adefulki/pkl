package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.developers.smartytoast.SmartyToast;
import com.iceteck.silicompressorr.SiliCompressor;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ghyeok.stickyswitch.widget.StickySwitch;
import it.sauronsoftware.ftp4j.FTPClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by AdeFulki on 7/18/2017.
 */

public class SettingAwalDagangan extends AppCompatActivity implements IPickResult {

    @BindView(R.id.namaDagangan)
    EditText namaDagangan;
    @BindView(R.id.deskripsiDagangan)
    EditText deskripsiDagangan;
    @BindView(R.id.fotoDagangan)
    ImageView fotoDagangan;
    @BindView(R.id.tipeDagangan)
    StickySwitch tipeDagangan;
    @BindView(R.id.btnSelesai)
    Button btnSelesai;

    private String id;
    private ImageLoader imageLoader;
    private Uri imageUri;
    private ProgressDialog prg;
    private Boolean tipe;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_awal_dagangan);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pengaturan Awal Dagangan");

        prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        fotoDagangan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickSetup setup = new PickSetup()
                        .setTitle("Pilih")
                        .setCancelText("Batal")
                        .setCameraButtonText("Kamera")
                        .setGalleryButtonText("Gallery");
                PickImageDialog.build(setup)
                        .show(SettingAwalDagangan.this);
            }
        });

        tipeDagangan.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NotNull StickySwitch.Direction direction, @NotNull String text) {
                if (direction.name().equals("RIGHT")){
                    tipe = true;
                }else{
                    tipe = false;
                }
            }
        });

        btnSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    failed();
                    return;
                }

                btnSelesai.setEnabled(false);

                uploadTask uploadTask = (uploadTask) new uploadTask(
                        "ftp.carmate.id",
                        "pkl@carmate.id",
                        "Kam1selalu1",
                        "assets/image/",
                        id,
                        namaDagangan.getText().toString(),
                        deskripsiDagangan.getText().toString(),
                        tipe).execute();
                try {
                    Log.i("test",uploadTask.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        //image loader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(this).build());
        L.disableLogging();
    }

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            imageLoader.displayImage(String.valueOf(pickResult.getUri()), fotoDagangan);
            imageUri = pickResult.getUri();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            SmartyToast.makeText(getApplicationContext(),pickResult.getError().getMessage(),SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
        }
    }

    class uploadTask extends AsyncTask<Void, Void, String> {

        private final String mHost;
        private final String mUsername;
        private final String mPassword;
        private final String mDir;
        private final String mIdPedagang;
        private final String mNamaDagangan;
        private final String mDeskripsiDagangan;
        private final Boolean mTipeDagangan;

        uploadTask(String host, String username, String password, String dir, String idPedagang, String namaDagangan, String deskripsiDagangan, Boolean tipeDagangan) {
            mHost = host;
            mUsername = username;
            mPassword = password;
            mDir = dir;
            mIdPedagang = idPedagang;
            mNamaDagangan = namaDagangan;
            mDeskripsiDagangan = deskripsiDagangan;
            mTipeDagangan = tipeDagangan;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            prg = new ProgressDialog(SettingAwalDagangan.this);
            prg.setMessage("Uploading...");
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
                File imageFile = new File(filesDir, name + ".jpg");

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

                JSONObject dataToSend = null;
                try {
                    dataToSend = new JSONObject()
                            .put("idPedagang", mIdPedagang)
                            .put("namaDagangan", mNamaDagangan)
                            .put("deskripsiDagangan", mDeskripsiDagangan)
                            .put("fotoDagangan", name+".jpg")
                            .put("tipeDagangan", mTipeDagangan);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Dagangan_controller/addDagangan")
                        .post(RequestBody.create(JSON, dataToSend.toString()))
                        .build();

                OkHttpClient okClient = new OkHttpClient();
                //Make the request
                try {
                    okClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("asik","selesai request");

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
            btnSelesai.setEnabled(true);
            if (!tipe){
                startActivity(new Intent(SettingAwalDagangan.this, SettingLokasiPedagang.class));
            }else
                startActivity(new Intent(SettingAwalDagangan.this, null));
        }
    }

    public boolean validate() {
        boolean valid = true;
        String nama = namaDagangan.getText().toString();

        if(nama.isEmpty()){
            namaDagangan.setError("Nama tidak boleh kosong");
            valid = false;
        }

        return valid;
    }

    public void failed() {
        Toast.makeText(getBaseContext(), "gagal", Toast.LENGTH_LONG).show();
        btnSelesai.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
