package com.kota201.jtk.pkl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kota201.jtk.pkl.fragment.PenilaianPedagangFragment;
import com.kota201.jtk.pkl.fragment.StatistikFragment;
import com.kota201.jtk.pkl.model.Dagangan;
import com.kota201.jtk.pkl.model.Pedagang;
import com.kota201.jtk.pkl.model.Penilaian;
import com.kota201.jtk.pkl.model.Produk;
import com.kota201.jtk.pkl.restful.PostMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class PedagangMainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.fotoDagangan)
    ImageView fotoDagangan;
    @BindView(R.id.namaDagangan)
    TextView namaDagangan;
    @BindView(R.id.deskripsiDagangan)
    TextView deskripsiDagangan;
    @BindView(R.id.statusBerjualan)
    Switch statusBerjualan;
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
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private String idDagangan;
    private String id;
    private Dagangan dagangan;
    private Pedagang pedagang;
    private ArrayList<Produk> listProduk;
    private int[] tabIcons = {
            R.drawable.ic_insert_chart_white,
            R.drawable.ic_stars
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pedagang);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        //Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //Show "back" button
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dagangan = new Dagangan();
        pedagang = new Pedagang();
        listProduk = new ArrayList<>();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);
        idDagangan = prefs.getString("idDagangan", null);

        //image loader
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(this).build());
        L.disableLogging();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics())))
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("drawable://" + R.drawable.default3, fotoDagangan, options);

        getDetail();

        imageLoader.displayImage("http://carmate.id/assets/image/" + dagangan.getFotoDagangan(), fotoDagangan, options);
        namaDagangan.setText(dagangan.getNamaDagangan());
        deskripsiDagangan.setText(dagangan.getDeskripsiDagangan());
        pelanggan.setText(String.valueOf(dagangan.getCountPelanggan()));
        if(dagangan.getStatusBerjualan())
            statusBerjualan.setChecked(true);
        else
            statusBerjualan.setChecked(false);

        statusBerjualan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editStatusBerjualan(isChecked);
            }
        });

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    public void getDetail(){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", idDagangan)
                    .put("idPembeli", "");
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
        adapter.addFrag(new StatistikFragment(), "STATISTIK");
        adapter.addFrag(new PenilaianPedagangFragment(), "PENILAIAN");
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
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_beranda) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_login) {
        } else if (id == R.id.nav_registrasi) {
        } else if (id == R.id.nav_tentang) {
            startActivity(new Intent(PedagangMainActivity.this, SettingAkunPedagang.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void editStatusBerjualan(Boolean isChecked){
        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("idDagangan", idDagangan)
                    .put("statusBerjualan", isChecked);
            assert dataToSend != null;
            PostMethod postMethod = (PostMethod) new PostMethod().execute(
                    "http://carmate.id/index.php/Dagangan_controller/changeStatusBerjualan",
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
