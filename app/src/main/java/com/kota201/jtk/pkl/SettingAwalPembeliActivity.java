package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sauronsoftware.ftp4j.FTPClient;

/**
 * Created by AdeFulki on 7/17/2017.
 */

public class SettingAwalPembeliActivity extends AppCompatActivity implements IPickResult{

    @BindView(R.id.namaPembeli)
    EditText namaPembeli;
    @BindView(R.id.alamatPembeli)
    EditText alamatPembeli;
    @BindView(R.id.fotoPembeli)
    ImageView fotoPembeli;
    @BindView(R.id.btnSelesai)
    Button btnSelesai;

    private ImageLoader imageLoader;
    private ProgressDialog prg;
    private Uri imageUri;
    private String imagePath;
    private Bitmap imageBitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_awal_pembeli);
        ButterKnife.bind(this);

        fotoPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickSetup setup = new PickSetup()
                        .setTitle("Pilih")
                        .setCancelText("Batal")
                        .setCameraButtonText("Kamera")
                        .setGalleryButtonText("Gallery");
                PickImageDialog.build(setup)
                        .show(SettingAwalPembeliActivity.this);
            }
        });

        btnSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadTask async=new uploadTask();
                async.execute("ftp.carmate.id","pkl@carmate.id","Kam1selalu1","assets/image/");
                try {
                    Log.i("test",async.get());
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
            //If you want the Uri.
            //Mandatory to refresh image from Uri.
            //getImageView().setImageURI(null);

            //Setting the real returned image.
            //fotoPembeli.setImageURI(pickResult.getUri());
            imageLoader.displayImage(String.valueOf(pickResult.getUri()), fotoPembeli);
            imageUri = pickResult.getUri();
            //If you want the Bitmap.
             imageBitmap = pickResult.getBitmap();

            //Image path
            imagePath = pickResult.getPath();
        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            SmartyToast.makeText(getApplicationContext(),pickResult.getError().getMessage(),SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
        }
    }

    class uploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            prg = new ProgressDialog(SettingAwalPembeliActivity.this);
            prg.setMessage("Uploading...");
            prg.show();
        }
        @Override
        protected String doInBackground(String... params) {
            FTPClient client = new FTPClient();

            try {

                client.connect(params[0],21);
                client.login(params[1], params[2]);
                client.setType(FTPClient.TYPE_BINARY);
                client.changeDirectory(params[3]);
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
        }
    }
}
