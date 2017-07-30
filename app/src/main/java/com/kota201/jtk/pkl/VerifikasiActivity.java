package com.kota201.jtk.pkl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.developers.smartytoast.SmartyToast;
import com.kota201.jtk.pkl.restful.PostMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/17/2017.
 */

public class VerifikasiActivity extends AppCompatActivity {
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.btnKirimUlang)
    Button btnKirimUlang;
    @BindView(R.id.inputKodeAkses)
    PinEntryEditText inputKodeAkses;
    @BindView(R.id.btnCekKodeAkses)
    Button btnCekKodeAkses;

    String noPonsel;
    String kodeAkses;
    int role;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.verifikasi);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Verifikasi Nomor Ponsel");

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        noPonsel = prefs.getString("noPonsel", null);
        role = prefs.getInt("role", 0);

        textView.setText("4 digit Kode Akses telah dikirimkan melalui SMS kepada nomor ponsel "+noPonsel+". Jika terjadi kesalahan Anda dapat meminta untuk mengirim ulang Kode Akses Anda");

        btnCekKodeAkses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Tahap","satu-1");
                cekKodeAkses();
            }
        });

        btnKirimUlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Tahap","satu-2");
                kirimUlangKodeAkses();
            }
        });

        if (inputKodeAkses != null) {
            inputKodeAkses.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    kodeAkses = str.toString();
                }
            });
        }
    }

    public void cekKodeAkses(){
        JSONObject dataToSend = null;
        Boolean statusValid;
        if(role==1){
            try {
                dataToSend = new JSONObject()
                        .put("noPonselPedagang", noPonsel)
                        .put("kodeAkses", kodeAkses);
                assert dataToSend != null;
                PostMethod postMethod = (PostMethod) new PostMethod().execute(
                        "http://carmate.id/index.php/Verifikasi_controller/checkKodeAksesPedagang",
                        dataToSend.toString()
                );
                JSONObject jObj = new JSONObject(postMethod.get());
                statusValid = jObj.getBoolean("statusValid");
                if (statusValid){
                    SmartyToast.makeText(getApplicationContext(),"Kode Akses valid",SmartyToast.LENGTH_SHORT,SmartyToast.DONE);
                    this.finish();
                    startActivity(new Intent(VerifikasiActivity.this, SettingAwalDagangan.class));
                }else{
                    SmartyToast.makeText(getApplicationContext(),"Kode Akses tidak valid",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
                    inputKodeAkses.setText(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else{
            Log.i("Tahap","Dua-1");
            try {
                dataToSend = new JSONObject()
                        .put("noPonselPembeli", noPonsel)
                        .put("kodeAkses", kodeAkses);
                assert dataToSend != null;
                PostMethod postMethod = (PostMethod) new PostMethod().execute(
                        "http://carmate.id/index.php/Verifikasi_controller/checkKodeAksesPembeli",
                        dataToSend.toString()
                );
                Log.i("Tahap",postMethod.get());
                JSONObject jObj = new JSONObject(postMethod.get());
                statusValid = jObj.getBoolean("statusValid");
                if (statusValid){
                    startActivity(new Intent(VerifikasiActivity.this, SettingAwalPembeliActivity.class));
                }else{
                    SmartyToast.makeText(getApplicationContext(),"Kode Akses salah",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
                    inputKodeAkses.setText(null);
                }
                Log.i("Tahap","TIGA-1");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void kirimUlangKodeAkses(){
        ProgressDialog progressDialog = new ProgressDialog(VerifikasiActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mengirim ulang Kode Akses...");
        progressDialog.show();
        JSONObject dataToSend = null;
        Boolean statusValid;
        if(role==1){
            try {
                dataToSend = new JSONObject()
                        .put("noPonselPedagang", noPonsel);
                assert dataToSend != null;
                new PostMethod().execute(
                        "http://carmate.id/index.php/Verifikasi_controller/recreateKodeAksesPedagang",
                        dataToSend.toString()
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Log.i("Tahap","Dua-2");
            try {
                dataToSend = new JSONObject()
                        .put("noPonselPembeli", noPonsel);
                assert dataToSend != null;
                new PostMethod().execute(
                        "http://carmate.id/index.php/Verifikasi_controller/recreateKodeAksesPembeli",
                        dataToSend.toString()
                );
                Log.i("Tahap","TIGA-2");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        progressDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
