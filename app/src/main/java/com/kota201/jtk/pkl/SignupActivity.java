package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.developers.smartytoast.SmartyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private UserSignupPedagangTask mAuthPedagangTask = null;
    private UserSignupPembeliTask mAuthPembeliTask = null;
    @BindView(R.id.inputNoPonsel) EditText inputNoPonsel;
    @BindView(R.id.inputPassword) EditText inputPassword;
    @BindView(R.id.inputReEnterPassword) EditText inputeReEnterPassword;
    @BindView(R.id.btnSignup) Button btnSignup;
    @BindView(R.id.linkLogin) TextView linkLogin;
    @BindView(R.id.radioGroupRole) SegmentedGroup radioGroupRole;
    @BindView(R.id.radioBtnPedagang) RadioButton radioBtnPedagang;
    @BindView(R.id.radioBtnPembeli) RadioButton radioBtnPembeli;
    @BindView(R.id.viewRole) TextView viewRole;

    ProgressDialog progressDialog;
    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup);
        ButterKnife.bind(this);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        radioGroupRole.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                View radioButton = radioGroupRole.findViewById(i);
                int index = radioGroupRole.indexOfChild(radioButton);
                switch (index) {
                    case 0:
                            viewRole.setText(R.string.definisi_pedagang);
                        break;
                    case 1:
                            viewRole.setText(R.string.definisi_pembeli);
                        break;
                }
            }
        });
        radioBtnPedagang.setChecked(true);
    }

    public void signup() {
        Log.d(TAG, "Registrasi");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        btnSignup.setEnabled(false);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Membuat akun...");
        progressDialog.show();

        String noPonsel = inputNoPonsel.getText().toString();
        String password = inputPassword.getText().toString();

        if(radioBtnPedagang.isChecked()){
            Log.i("asik","pedagang");
            mAuthPedagangTask = new UserSignupPedagangTask(noPonsel, md5(password));
            mAuthPedagangTask.execute((Void) null);
        }else if(radioBtnPembeli.isChecked()){
            Log.i("asik","pembeli");
            mAuthPembeliTask = new UserSignupPembeliTask(noPonsel, md5(password));
            mAuthPembeliTask.execute((Void) null);
        }
    }

    public void onSignupSuccess() {
        SmartyToast.makeText(getBaseContext(),"Registrasi Berhasil",SmartyToast.LENGTH_SHORT,SmartyToast.DONE);
        btnSignup.setEnabled(true);
        SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE).edit();
        editor.putString("noPonsel", inputNoPonsel.getText().toString());
        Intent i = new Intent(getApplicationContext(), VerifikasiActivity.class);
        if(radioBtnPedagang.isChecked()){
            GetIdPedagangTask getIdPedagangTask = (GetIdPedagangTask) new GetIdPedagangTask().execute(inputNoPonsel.getText().toString());
            try {
                id = getIdPedagangTask.get();
                editor.putString("id", id);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            editor.putInt("role", 1);
        }else if(radioBtnPembeli.isChecked()){
            GetIdPembeliTask getIdPembeliTask = (GetIdPembeliTask) new GetIdPembeliTask().execute(inputNoPonsel.getText().toString());
            try {
                id = getIdPembeliTask.get();
                editor.putString("id", id);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            editor.putInt("role", 2);
        }
        editor.apply();
        startActivity(i);
        finish();
    }

    public void onSignupFailed() {
        SmartyToast.makeText(getBaseContext(),"Registrasi Gagal",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
        btnSignup.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String noPonsel = inputNoPonsel.getText().toString();
        String password = inputPassword.getText().toString();
        String reEnterPassword = inputeReEnterPassword.getText().toString();

        if (noPonsel.isEmpty() || !Patterns.PHONE.matcher(noPonsel).matches()) {
            inputNoPonsel.setError("Nomor ponsel tidak sesuai");
            valid = false;
        } else {
            inputNoPonsel.setError(null);
        }

        if (password.isEmpty()) {
            inputPassword.setError("Masukan password");
            valid = false;
        } else if (password.length() < 4) {
            inputPassword.setError("Password tidak kurang dari 6 karakter");
            valid = false;
        }else if(password.length() > 18) {
            inputPassword.setError("Password tidak lebih dari 18 karakter");
            valid = false;
        }else if (!(password.equals(reEnterPassword))) {
            inputeReEnterPassword.setError("Password tidak sesuai");
            valid = false;
        }else{
            inputPassword.setError(null);
        }

        if (reEnterPassword.isEmpty()) {
            inputeReEnterPassword.setError("Masukan password");
            valid = false;
        } else if (!(reEnterPassword.equals(password))) {
            inputeReEnterPassword.setError("Password tidak sesuai");
            valid = false;
        }else{
            inputeReEnterPassword.setError(null);
        }

        return valid;
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

    public class UserSignupPembeliTask extends AsyncTask<Void, Void, Boolean> {

        private final String mNoPonsel;
        private final String mPassword;

        UserSignupPembeliTask(String noPonsel, String password) {
            mNoPonsel = noPonsel;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            JSONObject dataToSend = null;
            try {
                dataToSend = new JSONObject()
                        .put("noPonselPembeli", mNoPonsel)
                        .put("passwordPembeli", mPassword);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert dataToSend != null;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            //Create request object
            Request request = new Request.Builder()
                    .url("http://carmate.id/index.php/Pembeli_controller/addPembeli")
                    .post(RequestBody.create(JSON, dataToSend.toString()))
                    .build();

            OkHttpClient client = new OkHttpClient();
            //Make the request
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("asik","selesai request");
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.i("asik","masuk post execute");
            mAuthPedagangTask = null;
            progressDialog.dismiss();

            if (success) {
                onSignupSuccess();
            } else {
                onSignupFailed();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthPedagangTask = null;
            progressDialog.dismiss();
        }
    }

    public class UserSignupPedagangTask extends AsyncTask<Void, Void, Boolean> {

        private final String mNoPonsel;
        private final String mPassword;

        UserSignupPedagangTask(String noPonsel, String password) {
            mNoPonsel = noPonsel;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            JSONObject dataToSend = null;

            try {
                dataToSend = new JSONObject()
                        .put("noPonselPedagang", mNoPonsel)
                        .put("passwordPedagang", mPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert dataToSend != null;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            //Create request object
            Request request = new Request.Builder()
                    .url("http://carmate.id/index.php/Pedagang_controller/addPedagang")
                    .post(RequestBody.create(JSON, dataToSend.toString()))
                    .build();

            OkHttpClient client = new OkHttpClient();
            //Make the request
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.i("asik","masuk post execute");
            mAuthPembeliTask = null;
            progressDialog.dismiss();

            if (success) {
                onSignupSuccess();
            } else {
                onSignupFailed();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthPembeliTask = null;
            progressDialog.dismiss();
        }
    }

    public class GetIdPedagangTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            JSONObject dataToSend = null;

            try {
                dataToSend = new JSONObject()
                        .put("noPonselPedagang", params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert dataToSend != null;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            //Create request object
            Request request = new Request.Builder()
                    .url("http://carmate.id/index.php/Pedagang_controller/getIdPedagangByNoPonselPedagang")
                    .post(RequestBody.create(JSON, dataToSend.toString()))
                    .build();

            OkHttpClient client = new OkHttpClient();
            //Make the request
            String jsonData = null;
            JSONObject Jobject = null;
            try {
                Response response = client.newCall(request).execute();
                jsonData = response.body().string();
                Jobject = new JSONObject(jsonData);
                return Jobject.getString("idPedagang");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class GetIdPembeliTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            JSONObject dataToSend = null;

            try {
                dataToSend = new JSONObject()
                        .put("noPonselPembeli", params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert dataToSend != null;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            //Create request object
            Request request = new Request.Builder()
                    .url("http://carmate.id/index.php/Pembeli_controller/getIdPembeliByNoPonselPembeli")
                    .post(RequestBody.create(JSON, dataToSend.toString()))
                    .build();

            OkHttpClient client = new OkHttpClient();
            //Make the request
            String jsonData = null;
            JSONObject Jobject = null;
            try {
                Response response = client.newCall(request).execute();
                jsonData = response.body().string();
                Jobject = new JSONObject(jsonData);
                return Jobject.getString("idPembeli");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}