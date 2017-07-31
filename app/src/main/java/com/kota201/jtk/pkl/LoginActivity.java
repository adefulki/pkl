package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.developers.smartytoast.SmartyToast;
import com.kota201.jtk.pkl.restful.PostMethod;
import com.kota201.jtk.pkl.service.NetworkChangeReceiver;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null;
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.inputNoPonsel) EditText inputNoPonsel;
    @BindView(R.id.inputPassword) EditText inputPassword;
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.linkSignup) View linkSignup;

    Boolean statusValid;
    ProgressDialog progressDialog;
    int role;
    private String id;
    private String idDagangan;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        ButterKnife.bind(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        linkSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        if (mAuthTask != null) {
            return;
        }

        btnLogin.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Autentikasi...");
        progressDialog.show();

        String mNoPonsel = inputNoPonsel.getText().toString();
        String mPassword = inputPassword.getText().toString();

        mAuthTask = new UserLoginTask(mNoPonsel, md5(mPassword));
        mAuthTask.execute((Void) null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    public void onLoginSuccess() {
        String noPonsel = inputNoPonsel.getText().toString();
        SmartyToast.makeText(getBaseContext(),"Login Berhasil",SmartyToast.LENGTH_SHORT,SmartyToast.DONE);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String uId, String registrationId) {
                Log.i("test-userid", "User:" + uId);
                userId = uId;
                if (registrationId != null)
                    Log.i("test-registrationid", "registrationId:" + registrationId);

            }
        });
        if(role == 1){
            //jika pedagang

            editUserIdPedagang();

            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE).edit();
            editor.putInt("role",1);
            editor.putString("noPonsel",noPonsel);


            GetIdPedagangTask getIdPedagangTask = (GetIdPedagangTask) new GetIdPedagangTask().execute(inputNoPonsel.getText().toString());
            try {
                id = getIdPedagangTask.get();
                editor.putString("id", id);

                idDagangan = new getIdDagangnTask(id).execute().get();
                editor.putString("idDagangan", idDagangan);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            editor.apply();

            Intent intent = new Intent(LoginActivity.this, null);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            //jika pembeli

            editUserIdPembeli();

            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE).edit();
            editor.putInt("role",2);
            editor.putString("noPonsel",noPonsel);

            GetIdPembeliTask getIdPembeliTask = (GetIdPembeliTask) new GetIdPembeliTask().execute(inputNoPonsel.getText().toString());
            try {
                id = getIdPembeliTask.get();
                editor.putString("id", id);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            editor.apply();

            Intent intent = new Intent(LoginActivity.this, LokasiPedagangMemberActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        btnLogin.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        SmartyToast.makeText(getBaseContext(),"Login Gagal",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
        btnLogin.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String noPonsel = inputNoPonsel.getText().toString();
        String password = inputPassword.getText().toString();

        if (noPonsel.isEmpty() || !Patterns.PHONE.matcher(noPonsel).matches()) {
            inputNoPonsel.setError("Nomor ponsel tidak sesuai");
            valid = false;
        } else {
            inputNoPonsel.setError(null);
        }

        if (password.isEmpty()) {
            inputPassword.setError("Password tidak sesuai");
            valid = false;
        } else if (password.length() < 4) {
            inputPassword.setError("Password tidak kurang dari 6 karakter");
            valid = false;
        }else if(password.length() > 18) {
            inputPassword.setError("Password tidak lebih dari 18 karakter");
            valid = false;
        }else{
            inputPassword.setError(null);
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

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mNoPonsel;
        private final String mPassword;

        UserLoginTask(String noPonsel, String password) {
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

            if(!NetworkChangeReceiver.isNetworkAvailable(getBaseContext()))
                return false;

            JSONObject dataToSend = null;
            try {
                dataToSend = new JSONObject()
                        .put("noPonsel", mNoPonsel)
                        .put("password", mPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            assert dataToSend != null;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            //Create request object
            Request request = new Request.Builder()
                    .url("http://carmate.id/index.php/Autentikasi_controller/login")
                    .post(RequestBody.create(JSON, dataToSend.toString()))
                    .build();

            OkHttpClient client = new OkHttpClient();
            //Make the request
            JSONObject Jobject = null;
            try {
                Response responses = client.newCall(request).execute();
                if (!responses.isSuccessful())
                    throw new IOException("Unexpected code" + responses.toString());

                String jsonData = responses.body().string();
                Jobject = new JSONObject(jsonData);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Getting JSON Array node
            assert Jobject != null;
            JSONObject contacts = null;
            try {
                contacts = Jobject.getJSONObject("login");
                statusValid = contacts.getBoolean("statusValid");
                Log.i("asik",statusValid.toString());
                role = contacts.getInt("role");
                Log.i("asik", String.valueOf(role));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            progressDialog.dismiss();

            if (success && statusValid) {
                onLoginSuccess();
            } else {
                onLoginFailed();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
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

    class getIdDagangnTask extends AsyncTask<Void, Void, String> {

        private final String mId;

        getIdDagangnTask(String id) {

            mId = id;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {

                JSONObject dataToSend = null;
                try {
                    dataToSend = new JSONObject()
                            .put("idPedagang", mId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                assert dataToSend != null;

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                //Create request object
                Request request = new Request.Builder()
                        .url("http://carmate.id/index.php/Dagangan_controller/getIdDaganganByIdPedagang")
                        .post(RequestBody.create(JSON, dataToSend.toString()))
                        .build();

                OkHttpClient okClient = new OkHttpClient();
                //Make the request
                String jsonData;
                JSONObject Jobject;
                try {
                    Response response = okClient.newCall(request).execute();
                    jsonData = response.body().string();
                    Jobject = new JSONObject(jsonData);
                    return Jobject.getString("idDagangan");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("asik", "selesai request");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void editUserIdPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("userIdPembeli", userId);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pembeli_controller/editUserIdPembeli",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void editUserIdPedagang(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPedagang", id)
                    .put("userIdPedagang", userId);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pedagang_controller/editUserIdPedagang",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
