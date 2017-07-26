package com.kota201.jtk.pkl;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.developers.smartytoast.SmartyToast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/19/2017.
 */

public class SettingProdukDagangan extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.addProduk)
    FloatingActionButton addProduk;

    private String id;
    private String idDagangan;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_produk_dagangan);

        ButterKnife.bind(this);

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(getBaseContext()).build());
        L.disableLogging();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pengaturan Produk");

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);
        idDagangan = prefs.getString("idDagangan", null);

        idDagangan = "nhkul5gd7891j";

        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", idDagangan);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert dataToSend != null;

        ProdukAdapter adapter=new ProdukAdapter(this,dataToSend);
        recyclerView.setAdapter(adapter);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addProduk.setOnClickListener(addClickListener);
    }

    View.OnClickListener addClickListener =new View.OnClickListener(){
        private ImageView fotoProduk;
        @Override
        public void onClick(View v) {

            imageLoader = ImageLoader.getInstance();
            imageLoader.init(new ImageLoaderConfiguration.Builder(getBaseContext()).build());
            L.disableLogging();

            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_produk, null);

            fotoProduk = (ImageView) layout.findViewById(R.id.fotoProduk);
            EditText namaProduk = (EditText) layout.findViewById(R.id.namaProduk);
            EditText deskripsiProduk = (EditText) layout.findViewById(R.id.deskripsiProduk);
            EditText hargaProduk = (EditText) layout.findViewById(R.id.hargaProduk);
            EditText satuanProduk = (EditText) layout.findViewById(R.id.satuanProduk);


            fotoProduk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PickSetup setup = new PickSetup()
                            .setTitle("Pilih")
                            .setCancelText("Batal")
                            .setCameraButtonText("Kamera")
                            .setGalleryButtonText("Gallery");
                    PickImageDialog.build(setup)
                            .setOnPickResult(new IPickResult() {
                                @Override
                                public void onPickResult(PickResult pickResult) {
                                    if (pickResult.getError() == null) {
                                        imageLoader.displayImage(String.valueOf(pickResult.getUri()), fotoProduk);
                                    } else {
                                        //Handle possible errors
                                        //TODO: do what you have to do with r.getError();
                                        SmartyToast.makeText(getBaseContext(),pickResult.getError().getMessage(),SmartyToast.LENGTH_SHORT,SmartyToast.ERROR);
                                    }
                                }})
                            .show((FragmentActivity) getBaseContext());
                }
            });

            //Building dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingProdukDagangan.this);
            builder.setView(layout);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //save info where you want it
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(getBaseContext().getResources()
                    .getColor(R.color.colorPrimaryDark));
            Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(getBaseContext().getResources()
                    .getColor(R.color.colorPrimaryDark));
        }
    };
}
