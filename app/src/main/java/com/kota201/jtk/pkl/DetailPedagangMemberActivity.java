package com.kota201.jtk.pkl;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.developers.smartytoast.SmartyToast;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kota201.jtk.pkl.fragment.PenilaianDetailPedagangMemberFragment;
import com.kota201.jtk.pkl.fragment.ProdukDetailPedagangMemberFragment;
import com.kota201.jtk.pkl.fragment.TentangDetailPedagangMemberFragment;
import com.kota201.jtk.pkl.model.Dagangan;
import com.kota201.jtk.pkl.model.Pedagang;
import com.kota201.jtk.pkl.model.Penilaian;
import com.kota201.jtk.pkl.model.Produk;
import com.kota201.jtk.pkl.restful.PostMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class DetailPedagangMemberActivity extends AppCompatActivity implements OnMapReadyCallback{

    @BindView(R.id.fotoDagangan)
    ImageView fotoDagangan;
    @BindView(R.id.btnBerlangganan)
    FloatingActionButton btnBerlangganan;
    @BindView(R.id.btnObrolan)
    FloatingActionButton btnObrolan;
    @BindView(R.id.btnPemberitahuan)
    FloatingActionButton btnPemberitahuan;
    @BindView(R.id.namaDagangan)
    TextView namaDagangan;
    @BindView(R.id.deskripsiDagangan)
    TextView deskripsiDagangan;
    @BindView(R.id.statusBerjualan)
    TextView statusBerjualan;
    @BindView(R.id.pelanggan)
    TextView pelanggan;
    @BindView(R.id.mengunjungi)
    TextView mengunjungi;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    private String id;
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private String idDagangan;
    private Dagangan dagangan;
    private Pedagang pedagang;
    private ArrayList<Produk> listProduk;
    private LayoutInflater inflater;
    private int[] tabIcons = {
            R.drawable.ic_restaurant,
            R.drawable.ic_person_white,
            R.drawable.ic_stars
    };
    private SegmentedGroup radioGroupPemberitahuan;
    private RadioButton radioBtnOn;
    private RadioButton radioBtnOff;
    private EditText inputJarakPemberitahuan;
    private Boolean status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_pedagang);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        //Show "back" button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        dagangan = new Dagangan();
        pedagang = new Pedagang();
        listProduk = new ArrayList<>();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idDagangan = extras.getString("idDagangan");
            Log.i("test-idDagangan",idDagangan);
        }

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics())))
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("drawable://" + R.drawable.default3, fotoDagangan, options);

        getDetail();

        imageLoader.displayImage("http://carmate.id/assets/image/" + dagangan.getFotoDagangan(), fotoDagangan, options);
        namaDagangan.setText(dagangan.getNamaDagangan());
        deskripsiDagangan.setText(dagangan.getDeskripsiDagangan());
        pelanggan.setText(String.valueOf(dagangan.getCountPelanggan()));
        if (dagangan.getStatusBerjualan()){
            statusBerjualan.setText("Sedang Berjualan");
            statusBerjualan.setBackgroundColor(getResources().getColor(R.color.colorOnline));
        }else {
            statusBerjualan.setText("Tidak Berjualan");
            statusBerjualan.setBackgroundColor(getResources().getColor(R.color.colorOffline));
        }

        if (!dagangan.getStatusBerlangganan()){
            btnBerlangganan.setColorNormal(getResources().getColor(R.color.colorOffline));
        }else{
            btnBerlangganan.setColorNormal(getResources().getColor(R.color.colorOnline));
        }

        if (!dagangan.getStatusNotifikasi()){
            btnPemberitahuan.setColorNormal(getResources().getColor(R.color.colorOffline));
        }else{
            btnPemberitahuan.setColorNormal(getResources().getColor(R.color.colorOnline));
        }

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        btnBerlangganan.setOnClickListener(berlangganan);
        btnObrolan.setOnClickListener(mustLogin);
        btnPemberitahuan.setOnClickListener(pemberitahuan);
    }

    View.OnClickListener mustLogin = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
            startActivity(new Intent(DetailPedagangMemberActivity.this, LoginActivity.class));
        }
    };

    View.OnClickListener berlangganan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (dagangan.getStatusBerlangganan()){
                deleteBerlangganan();
                dagangan.setStatusBerlangganan(false);
                btnBerlangganan.setColorNormal(getResources().getColor(R.color.colorOffline));
            }else{
                addBerlangganan();
                dagangan.setStatusBerlangganan(true);
                btnBerlangganan.setColorNormal(getResources().getColor(R.color.colorOnline));
            }
        }
    };

    View.OnClickListener pemberitahuan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (dagangan.getStatusBerlangganan()){
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DetailPedagangMemberActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_pemberitahuan, null);
                radioGroupPemberitahuan = (SegmentedGroup) dialogView.findViewById(R.id.radioGroupPemberitahuan);
                radioBtnOn = (RadioButton) dialogView.findViewById(R.id.radioBtnOn);
                radioBtnOff = (RadioButton) dialogView.findViewById(R.id.radioBtnOff);
                inputJarakPemberitahuan = (EditText) dialogView.findViewById(R.id.inputJarakPemberitahuan);
                if (dagangan.getStatusNotifikasi()){
                    radioBtnOn.setChecked(true);
                    status = true;
                }else {
                    radioBtnOff.setChecked(true);
                    status = false;
                }
                dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (radioBtnOn.isChecked()){
                            status = true;
                            btnPemberitahuan.setColorNormal(getResources().getColor(R.color.colorOnline));
                            dagangan.setStatusNotifikasi(true);

                        }else {
                            status = false;
                            btnPemberitahuan.setColorNormal(getResources().getColor(R.color.colorOffline));
                            dagangan.setStatusNotifikasi(false);
                        }
                        editPemberitahuan();
                    }
                });
                dialogBuilder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setView(dialogView);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(getResources()
                        .getColor(R.color.colorSecondary));
                Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(getResources()
                        .getColor(R.color.colorSecondary));
            }else{
                SmartyToast.makeText(getApplicationContext(),"Berlangganan Terlebih Dahulu",SmartyToast.LENGTH_SHORT,SmartyToast.WARNING);
            }

        }
    };

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    public void getDetail(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", idDagangan)
                    .put("idPembeli", id);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Dagangan_controller/getDetailDagangan",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
            JSONObject Jobject = new JSONObject(postMethod.get());
            dagangan.setIdDagangan(Jobject.getString("idDagangan"));
            dagangan.setNamaDagangan(Jobject.getString("namaDagangan"));
            dagangan.setDeskripsiDagangan(Jobject.getString("deskripsiDagangan"));
            dagangan.setFotoDagangan(Jobject.getString("fotoDagangan"));
            dagangan.setLatDagangan(Jobject.getDouble("latDagangan"));
            dagangan.setLngDagangan(Jobject.getDouble("lngDagangan"));
            dagangan.setTipeDagangan(Jobject.getBoolean("tipeDagangan"));
            dagangan.setStatusBerjualan(Jobject.getBoolean("statusBerjualan"));
            dagangan.setCountPelanggan(Jobject.getInt("countPelanggan"));
            dagangan.setStatusBerlangganan(Jobject.getBoolean("statusBerlangganan"));
            dagangan.setStatusNotifikasi(Jobject.getBoolean("statusNotifikasi"));
            dagangan.setMeanPenilaianDagangan((int) Jobject.getDouble("meanPenilaianDagangan"));
            dagangan.setCountPenilaianDagangan(Jobject.getInt("countPenilaianDagangan"));
            Log.i("test","selesai parsing dagangan");
            pedagang.setIdPedagang(Jobject.getString("idPedagang"));
            pedagang.setNamaPedagang(Jobject.getString("namaPedagang"));
            pedagang.setEmailPedagang(Jobject.getString("emailPedagang"));
            pedagang.setNoPonselPedagang(Jobject.getString("noPonselPedagang"));
            pedagang.setAlamatPedagang(Jobject.getString("alamatPedagang"));
            pedagang.setFotoPedagang(Jobject.getString("fotoPedagang"));
            Log.i("test","selesai parsing pedagang");
            JSONArray Jarray = Jobject.getJSONArray("produk");
            for (int i = 0; i < Jarray.length(); i++) {
                JSONObject c = null;
                Produk produk = new Produk();
                c = Jarray.getJSONObject(i);
                produk.setIdProduk(c.getString("idProduk"));
                produk.setNamaProduk(c.getString("namaProduk"));
                produk.setDeskripsiProduk(c.getString("deskripsiProduk"));
                produk.setFotoProduk(c.getString("fotoProduk"));
                produk.setHargaProduk(c.getString("hargaProduk"));
                produk.setSatuanProduk(c.getString("satuanProduk"));
                produk.setMeanPenilaianProduk(c.getInt("meanPenilaianProduk"));
                produk.setCountPenilaianProduk(c.getInt("countPenilaianProduk"));
                JSONArray Jarray2 = c.getJSONArray("penilaian");
                ArrayList<Penilaian> listPenilaian = new ArrayList<>();
                for (int j = 0; j < Jarray2.length(); j++) {
                    JSONObject c2 = null;
                    Penilaian penilaian = new Penilaian();
                    c2 = Jarray2.getJSONObject(j);
                    penilaian.setIdPenilaian(c2.getString("idPenilaian"));
                    penilaian.setDeskripsiPenilaian(c2.getString("deskripsiPenilaian"));
                    penilaian.setIdPembeli(c2.getString("idPembeli"));
                    penilaian.setNilaiPenilaian((int) c2.getDouble("nilaiPenilaian"));
                    listPenilaian.add(penilaian);
                }
                Log.i("test","selesai parsing penilaian");
                produk.setListPenilaian(listPenilaian);
                listProduk.add(produk);
            }
            Log.i("test","selesai parsing produk");
            Log.i("Tahap","TIGA-1");
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ProdukDetailPedagangMemberFragment(), "PRODUK");
        adapter.addFrag(new TentangDetailPedagangMemberFragment(), "TENTANG");
        adapter.addFrag(new PenilaianDetailPedagangMemberFragment(), "PENILAIAN");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setAllGesturesEnabled(false);
        LatLng latLng = new LatLng(dagangan.getLatDagangan(), dagangan.getLngDagangan());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMap.addMarker(new MarkerOptions()
                .position(latLng));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.share){
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editPemberitahuan(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("idDagangan", dagangan.getIdDagangan())
                    .put("jarakNotifikasi", inputJarakPemberitahuan.getText())
                    .put("statusNotifikasi", status);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Notifikasi_controller/changeStatusNotifikasi",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void addBerlangganan(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("idDagangan", dagangan.getIdDagangan());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pelanggan_controller/addPelanggan",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void deleteBerlangganan(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idPembeli", id)
                    .put("idDagangan", dagangan.getIdDagangan());
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Pelanggan_controller/removePelanggan",
                    dataToSend.toString()
            );
            Log.i("Tahap",postMethod.get());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Produk> getProduk(){
        return listProduk;
    }

    public Dagangan getDagangan(){
        return dagangan;
    }

    public Pedagang getPedagang(){
        return pedagang;
    }
}
