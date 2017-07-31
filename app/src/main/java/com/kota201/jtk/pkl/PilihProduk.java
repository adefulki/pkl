package com.kota201.jtk.pkl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/22/2017.
 */

public class PilihProduk extends AppCompatActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    String idDagangan;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pilih_produk);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pilih Produk");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idDagangan = extras.getString("idDagangan");
            Log.i("test-result",idDagangan);
        }

        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", idDagangan);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert dataToSend != null;

        PilihProdukAdapter adapter=new PilihProdukAdapter(this,dataToSend);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
