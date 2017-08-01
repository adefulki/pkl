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
import com.kota201.jtk.pkl.model.Pedagang;
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

public class SettingAkunPedagang extends AppCompatActivity implements IPickResult {
    @BindView(R.id.inputFotoPedagang)
    ImageView inputFotoPedagang;
    @BindView(R.id.inputNamaPedagang)
    EditText inputNamaPedagang;
    @BindView(R.id.inputEmailPedagang)
    EditText inputEmailPedagang;
    @BindView(R.id.inputNoPonselPedagang)
    EditText inputNoPonselPedagang;
    @BindView(R.id.inputPasswordLamaPedagang)
    EditText inputPasswordLamaPedagang;
    @BindView(R.id.inputPasswordBaruPedagang)
    EditText inputPasswordBaruPedagang;
    @BindView(R.id.inputAlamatPedagang)
    EditText inputAlamatPedagang;
    @BindView(R.id.btnEditNamaPedagang)
    ImageButton btnEditNamaPedagang;
    @BindView(R.id.btnEditEmailPedagang)
    ImageButton btnEditEmailPedagang;
    @BindView(R.id.btnEditNoPonselPedagang)
    ImageButton btnEditNoPonselPedagang;
    @BindView(R.id.btnEditPasswordPedagang)
    ImageButton btnEditPasswordPedagang;
    @BindView(R.id.btnEditPasswordBaruPedagang)
    ImageButton btnEditPasswordBaruPedagang;
    @BindView(R.id.btnEditAlamatPedagang)
    ImageButton btnEditAlamatPedagang;
    @BindView(R.id.passwordBaruLayout)
    LinearLayout passwordBaruLayout;

    private String id;
    private Pedagang pedagang;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_akun_pedagang);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pengaturan Akun Pedagang");

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        pedagang = new Pedagang();

        getInfo();

        inputFotoPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickSetup setup = new PickSetup()
                        .setTitle("Pilih")
                        .setCancelText("Batal")
                        .setCameraButtonText("Kamera")
                        .setGalleryButtonText("Gallery");
                PickImageDialog.build(setup)
                        .show(SettingAkunPedagang.this);
            }
        });

        btnEditNamaPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputNamaPedagang.isFocusable()){
                    if(!validasiNama()){
                        failed();
                        return;
                    }
                    inputNamaPedagang.setFocusable(false);
                    inputNamaPedagang.setFocusableInTouchMode(false);
                    editNamaPedagang();
                    btnEditNamaPedagang.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputNamaPedagang.setFocusable(true);
                    inputNamaPedagang.setFocusableInTouchMode(true);
                    btnEditNamaPedagang.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditEmailPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputEmailPedagang.isFocusable()){
                    inputEmailPedagang.setFocusable(false);
                    inputEmailPedagang.setFocusableInTouchMode(false);
                    editEmailPedagang();
                    btnEditEmailPedagang.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputEmailPedagang.setFocusable(true);
                    inputEmailPedagang.setFocusableInTouchMode(true);
                    btnEditEmailPedagang.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditNoPonselPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputNoPonselPedagang.isFocusable()){
                    if(!validasiNoPonsel()){
                        failed();
                        return;
                    }
                    inputNoPonselPedagang.setFocusable(false);
                    inputNoPonselPedagang.setFocusableInTouchMode(false);
                    editNoPonselPedagang();
                    startActivity(new Intent(SettingAkunPedagang.this, EditVerifikasiActivity.class));
                    btnEditNoPonselPedagang.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputNoPonselPedagang.setFocusable(true);
                    inputNoPonselPedagang.setFocusableInTouchMode(true);
                    btnEditNoPonselPedagang.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditAlamatPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputAlamatPedagang.isFocusable()){
                    inputAlamatPedagang.setFocusable(false);
                    inputAlamatPedagang.setFocusableInTouchMode(false);
                    editAlamatPedagang();
                    btnEditAlamatPedagang.setImageResource(R.drawable.ic_edit_primary);
                }else{
                    inputAlamatPedagang.setFocusable(true);
                    inputAlamatPedagang.setFocusableInTouchMode(true);
                    btnEditAlamatPedagang.setImageResource(R.drawable.ic_check_primary);
                }
            }
        });

        btnEditPasswordPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEditPasswordPedagang.setVisibility(View.GONE);
                inputPasswordLamaPedagang.setVisibility(View.VISIBLE);
                passwordBaruLayout.setVisibility(View.VISIBLE);
            }
        });

        btnEditPasswordBaruPedagang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validasiPassword()){
                    failed();
                    return;
                }
                btnEditPasswordPedagang.setVisibility(View.VISIBLE);
                inputPasswordLamaPedagang.setVisibility(View.GONE);
                passwordBaruLayout.setVisibility(View.GONE);
                editPasswordPedagang();
            }
        });
    }

    public void getInfo(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/getPedagang",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            JSONObject Jobject = new JSONObject(postMethod.get());
            pedagang.setNamaPedagang(Jobject.getString("namaPedagang"));
            pedagang.setIdPedagang(Jobject.getString("idPedagang"));
            pedagang.setNoPonselPedagang(Jobject.getString("noPonselPedagang"));
            pedagang.setAlamatPedagang(Jobject.getString("alamatPedagang"));
            pedagang.setEmailPedagang(Jobject.getString("emailPedagang"));
            pedagang.setFotoPedagang(Jobject.getString("fotoPedagang"));

            inputNamaPedagang.setText(pedagang.getNamaPedagang());
            inputAlamatPedagang.setText(pedagang.getAlamatPedagang());
            inputEmailPedagang.setText(pedagang.getEmailPedagang());
            inputNoPonselPedagang.setText(pedagang.getNoPonselPedagang());
            inputNoPonselPedagang.setText(pedagang.getNoPonselPedagang());
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics())))
                    .build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage("http://carmate.id/assets/image/" + pedagang.getFotoPedagang(), inputFotoPedagang, options);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Boolean cekPassword(){
        JSONObject dataToSend = null;
        Boolean status = true;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("passwordPedagang", md5(inputPasswordLamaPedagang.getText().toString()));
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/checkPasswordPedagang",
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

    public void editNamaPedagang(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("namaPedagang", inputNamaPedagang.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/editNamaPedagang",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editEmailPedagang(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("emailPedagang", inputEmailPedagang.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/editEmailPedagang",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editNoPonselPedagang(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("noPonselPedagang", inputNoPonselPedagang.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/editNoPonselPedagang",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editAlamatPedagang(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("alamatPedagang", inputAlamatPedagang.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/editAlamatPedagang",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            getInfo();
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editPasswordPedagang(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("passwordPedagang", md5(inputPasswordBaruPedagang.getText().toString()));
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/editPasswordPedagang",
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
       new editFotoPedagang(
               "ftp.carmate.id",
               "pkl@carmate.id",
               "Kam1selalu1",
               "assets/image/",
               id,
               pickResult.getUri()).execute();
        getInfo();
    }

    class editFotoPedagang extends AsyncTask<Void, Void, String> {

        private final String mHost;
        private final String mUsername;
        private final String mPassword;
        private final String mDir;
        private final String mIdPedagang;
        private final Uri mImageUri;

        editFotoPedagang(String host, String username, String password, String dir, String idPedagang, Uri imageUri) {
            mHost = host;
            mUsername = username;
            mPassword = password;
            mDir = dir;
            mIdPedagang = idPedagang;
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
                            .put("idPedagang", mIdPedagang)
                            .put("fotoPedagang", name+".jpg");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i("test-json","created");

                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Pedagang_controller/editFotoPedagang")
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
        String noPonsel = inputNoPonselPedagang.getText().toString();

        if (noPonsel.isEmpty() || !Patterns.PHONE.matcher(noPonsel).matches()) {
            inputNoPonselPedagang.setError("Nomor ponsel tidak sesuai");
            valid = false;
        } else {
            inputNoPonselPedagang.setError(null);
        }

        return valid;
    }

    public boolean validasiNama() {
        boolean valid = true;
        String nama = inputNamaPedagang.getText().toString();

        if (nama.isEmpty()) {
            inputNamaPedagang.setError("Nama tidak boleh kosong");
            valid = false;
        }

        return valid;
    }

    public boolean validasiPassword() {
        boolean valid = true;
        String passwordLama = inputPasswordLamaPedagang.getText().toString();
        String passwordBaru = inputPasswordBaruPedagang.getText().toString();


        if (!cekPassword()) {
            inputPasswordLamaPedagang.setError("Password tidak valid");
            valid = false;
        }

        if (passwordBaru.isEmpty()) {
            inputPasswordBaruPedagang.setError("Masukan password");
            valid = false;
        } else if (passwordBaru.length() < 4) {
            inputPasswordBaruPedagang.setError("Password tidak kurang dari 6 karakter");
            valid = false;
        }else if(passwordBaru.length() > 18) {
            inputPasswordBaruPedagang.setError("Password tidak lebih dari 18 karakter");
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
            MessageDigest digest = MessageDigest.getInstance("MD5");
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
