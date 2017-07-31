package com.kota201.jtk.pkl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.developers.smartytoast.SmartyToast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kota201.jtk.pkl.restful.PostMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by AdeFulki on 7/22/2017.
 */

public class QrReader extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Pindai QrCode Pedagang");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                //Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                Log.i("test-resultQR",result.getContents());
                if (checkDagangan(result.getContents())){
                    String idDagangan = result.getContents();
                    Log.i("test-resultQR","berhasil");
                    Intent intent = new Intent(QrReader.this, PilihProduk.class);
                    intent.putExtra("idDagangan", idDagangan);
                    finish();
                    startActivity(intent);
                }else {
                    SmartyToast.makeText(getApplicationContext(),"QrCode Tidak Valid",SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
                    finish();
                }
            }
        }
        else {
            finish();
        }
    }

    public Boolean checkDagangan(String id){
        Boolean status=false;
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", id);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Dagangan_controller/checkDagangan",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            JSONObject Jobject = new JSONObject(postMethod.get());
            status = Jobject.getBoolean("statusValidDagangan");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
