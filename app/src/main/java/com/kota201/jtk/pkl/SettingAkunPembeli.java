package com.kota201.jtk.pkl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.developers.smartytoast.SmartyToast;
import com.iceteck.silicompressorr.SiliCompressor;
import com.kota201.jtk.pkl.model.Pembeli;
import com.kota201.jtk.pkl.restful.PostMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sauronsoftware.ftp4j.FTPClient;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by AdeFulki on 7/22/2017.
 */

public class SettingAkunPembeli extends AppCompatActivity implements IPickResult {
    @BindView(R.id.inputFotoPembeli)
    ImageView inputFotoPembeli;
    @BindView(R.id.inputNamaPembeli)
    EditText inputNamaPembeli;
    @BindView(R.id.inputEmailPembeli)
    EditText inputEmailPembeli;
    @BindView(R.id.inputNoPonselPembeli)
    EditText inputNoPonselPembeli;
    @BindView(R.id.inputPasswordLamaPembeli)
    EditText inputPasswordLamaPembeli;
    @BindView(R.id.inputPasswordBaruPembeli)
    EditText inputPasswordBaruPembeli;
    @BindView(R.id.inputAlamatPembeli)
    EditText inputAlamatPembeli;
    @BindView(R.id.btnEditNamaPembeli)
    ImageButton btnEditNamaPembeli;
    @BindView(R.id.btnEditEmailPembeli)
    ImageButton btnEditEmailPembeli;
    @BindView(R.id.btnEditNoPonselPembeli)
    ImageButton btnEditNoPonselPembeli;
    @BindView(R.id.btnEditPasswordPembeli)
    ImageButton btnEditPasswordPembeli;
    @BindView(R.id.btnEditPasswordBaruPembeli)
    ImageButton btnEditPasswordBaruPembeli;
    @BindView(R.id.btnEditAlamatPembeli)
    ImageButton btnEditAlamatPembeli;
    @BindView(R.id.passwordBaruLayout)
    LinearLayout passwordBaruLayout;

    private String id;
    private Pembeli pembeli;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_akun_pembeli);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pengaturan Akun Pembeli");

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        pembeli = new Pembeli();

        getInfo();

        inputFotoPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickSetup setup = new PickSetup()
                        .setTitle("Pilih")
                        .setCancelText("Batal")
                        .setCameraButtonText("Kamera")
                        .setGalleryButtonText("Gallery");
                PickImageDialog.build(setup)
                        .show(SettingAkunPembeli.this);
            }
        });

        btnEditNamaPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputNamaPembeli.isFocusable()){
                    if(!validasiNama()){
                        failed();
                        return;
                    }
                    inputNamaPembeli.setFocusable(false);
                    inputNamaPembeli.setFocusableInTouchMode(false);
                    editNamaPembeli();
                    btnEditNamaPembeli.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputNamaPembeli.setFocusable(true);
                    inputNamaPembeli.setFocusableInTouchMode(true);
                    btnEditNamaPembeli.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditEmailPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputEmailPembeli.isFocusable()){
                    inputEmailPembeli.setFocusable(false);
                    inputEmailPembeli.setFocusableInTouchMode(false);
                    editEmailPembeli();
                    btnEditEmailPembeli.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputEmailPembeli.setFocusable(true);
                    inputEmailPembeli.setFocusableInTouchMode(true);
                    btnEditEmailPembeli.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditNoPonselPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputNoPonselPembeli.isFocusable()){
                    if(!validasiNoPonsel()){
                        failed();
                        return;
                    }
                    inputNoPonselPembeli.setFocusable(false);
                    inputNoPonselPembeli.setFocusableInTouchMode(false);
                    editNoPonselPembeli();
                    startActivity(new Intent(SettingAkunPembeli.this, EditVerifikasiActivity.class));
                    btnEditNoPonselPembeli.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputNoPonselPembeli.setFocusable(true);
                    inputNoPonselPembeli.setFocusableInTouchMode(true);
                    btnEditNoPonselPembeli.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditAlamatPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputAlamatPembeli.isFocusable()){
                    inputAlamatPembeli.setFocusable(false);
                    inputAlamatPembeli.setFocusableInTouchMode(false);
                    editAlamatPembeli();
                    btnEditAlamatPembeli.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputAlamatPembeli.setFocusable(true);
                    inputAlamatPembeli.setFocusableInTouchMode(true);
                    btnEditAlamatPembeli.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditPasswordPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEditPasswordPembeli.setVisibility(View.GONE);
                inputPasswordLamaPembeli.setVisibility(View.VISIBLE);
                passwordBaruLayout.setVisibility(View.VISIBLE);
            }
        });

        btnEditPasswordBaruPembeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validasiPassword()){
                    failed();
                    return;
                }
                btnEditPasswordPembeli.setVisibility(View.VISIBLE);
                inputPasswordLamaPembeli.setVisibility(View.GONE);
                passwordBaruLayout.setVisibility(View.GONE);
                editPasswordPembeli();
            }
        });
    }

    public void getInfo(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/getPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            JSONObject Jobject = new JSONObject(postMethod.get());
            pembeli.setNamaPembeli(Jobject.getString("namaPembeli"));
            pembeli.setIdPembeli(Jobject.getString("idPembeli"));
            pembeli.setNoPonselPembeli(Jobject.getString("noPonselPembeli"));
            pembeli.setAlamatPembeli(Jobject.getString("alamatPembeli"));
            pembeli.setEmailPembeli(Jobject.getString("emailPembeli"));
            pembeli.setFotoPembeli(Jobject.getString("fotoPembeli"));

            inputNamaPembeli.setText(pembeli.getNamaPembeli());
            inputAlamatPembeli.setText(pembeli.getAlamatPembeli());
            inputEmailPembeli.setText(pembeli.getEmailPembeli());
            inputNoPonselPembeli.setText(pembeli.getNoPonselPembeli());
            inputNoPonselPembeli.setText(pembeli.getNoPonselPembeli());
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics())))
                    .build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage("http://carmate.id/assets/image/" + pembeli.getFotoPembeli(), inputFotoPembeli, options);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Boolean cekPassword(){
        JSONObject dataToSend = null;
        Boolean status = true;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("passwordPembeli", md5(inputPasswordLamaPembeli.getText().toString()));
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/checkPasswordPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            JSONObject Jobject = new JSONObject(postMethod.get());
            status = Jobject.getBoolean("statusValidPassword");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            status = false;
        }

        return status;
    }

    public void editNamaPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("namaPembeli", inputNamaPembeli.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/editNamaPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editEmailPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("emailPembeli", inputEmailPembeli.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/editEmailPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editNoPonselPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("noPonselPembeli", inputNoPonselPembeli.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/editNoPonselPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editAlamatPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("alamatPembeli", inputAlamatPembeli.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/editAlamatPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editPasswordPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("passwordPembeli", md5(inputPasswordBaruPembeli.getText().toString()));
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/editPasswordPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPickResult(PickResult pickResult) {
       new editFotoPembeli(
               "ftp.carmate.id",
               "pkl@carmate.id",
               "Kam1selalu1",
               "assets/image/",
               id,
               pickResult.getUri()).execute();
        getInfo();
    }

    class editFotoPembeli extends AsyncTask<Void, Void, String> {

        private final String mHost;
        private final String mUsername;
        private final String mPassword;
        private final String mDir;
        private final String mIdPembeli;
        private final Uri mImageUri;

        editFotoPembeli(String host, String username, String password, String dir, String idPembeli, Uri imageUri) {
            mHost = host;
            mUsername = username;
            mPassword = password;
            mDir = dir;
            mIdPembeli = idPembeli;
            mImageUri = imageUri;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }

            FTPClient client = new FTPClient();

            try {
                client.connect(mHost,21);
                client.login(mUsername, mPassword);
                client.setType(FTPClient.TYPE_BINARY);
                client.changeDirectory(mDir);
                Bitmap imageBitmap = SiliCompressor.with(getBaseContext()).getCompressBitmap(String.valueOf(mImageUri));
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

                Log.i("test-image","uploaded");

                try {
                    // Simulate network access.
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return null;
                }

                JSONObject dataToSend = null;
                try {
                    dataToSend = new JSONObject()
                            .put("idPembeli", mIdPembeli)
                            .put("fotoPembeli", name+".jpg");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i("test-json","created");

                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Pembeli_controller/editFotoPembeli")
                        .post(RequestBody.create(JSON, dataToSend.toString()))
                        .build();

                OkHttpClient okClient = new OkHttpClient();
                //Make the request
                try {
                    okClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("test-call","selesai request");

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
    }

    public boolean validasiNoPonsel() {
        boolean valid = true;
        String noPonsel = inputNoPonselPembeli.getText().toString();

        if (noPonsel.isEmpty() || !Patterns.PHONE.matcher(noPonsel).matches()) {
            inputNoPonselPembeli.setError("Nomor ponsel tidak sesuai");
            valid = false;
        } else {
            inputNoPonselPembeli.setError(null);
        }

        return valid;
    }

    public boolean validasiNama() {
        boolean valid = true;
        String nama = inputNamaPembeli.getText().toString();

        if (nama.isEmpty()) {
            inputNamaPembeli.setError("Nama tidak boleh kosong");
            valid = false;
        }

        return valid;
    }

    public boolean validasiPassword() {
        boolean valid = true;
        String passwordLama = inputPasswordLamaPembeli.getText().toString();
        String passwordBaru = inputPasswordBaruPembeli.getText().toString();


        if (!cekPassword()) {
            inputPasswordLamaPembeli.setError("Password tidak valid");
            valid = false;
        }

        if (passwordBaru.isEmpty()) {
            inputPasswordBaruPembeli.setError("Masukan password");
            valid = false;
        } else if (passwordBaru.length() < 4) {
            inputPasswordBaruPembeli.setError("Password tidak kurang dari 6 karakter");
            valid = false;
        }else if(passwordBaru.length() > 18) {
            inputPasswordBaruPembeli.setError("Password tidak lebih dari 18 karakter");
            valid = false;
        }

        return valid;
    }

    public void failed() {
        SmartyToast.makeText(getApplicationContext(),"Gagal",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
