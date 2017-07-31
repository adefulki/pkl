package com.kota201.jtk.pkl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.kota201.jtk.pkl.restful.PostMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/22/2017.
 */

public class PenilaianProduk extends AppCompatActivity {
    @BindView(R.id.namaProduk)
    TextView mNamaProduk;
    @BindView(R.id.inputNilaiPenilaian)
    RatingBar inputNilaiPenilaian;
    @BindView(R.id.inputDeskripsiPenilaian)
    EditText inputDeskripsiPenilaian;
    @BindView(R.id.fotoProduk)
    ImageView mFotoProduk;
    @BindView(R.id.btnMenilai)
    Button btnMenilai;

    private String idProduk;
    private String namaProduk;
    private String fotoProduk;
    private String id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.penilaian_produk);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Penilaian Produk");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idProduk = extras.getString("idProduk");
            namaProduk = extras.getString("namaProduk");
            fotoProduk = extras.getString("fotoProduk");
            Log.i("test-result",idProduk);
            Log.i("test-result",namaProduk);
            Log.i("test-result",fotoProduk);
        }

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics())))
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://carmate.id/assets/image/" + fotoProduk, mFotoProduk, options);
        mNamaProduk.setText(namaProduk);

        btnMenilai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNamaPembeli();
                Intent intent = new Intent(PenilaianProduk.this, LokasiPedagangMemberActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    public void editNamaPembeli(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("idProduk", idProduk)
                    .put("nilaiPenilaian", inputNilaiPenilaian.getRating())
                    .put("deskripsiPenilaian", inputDeskripsiPenilaian.getText());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Penilaian_controller/penilaianProduk",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
