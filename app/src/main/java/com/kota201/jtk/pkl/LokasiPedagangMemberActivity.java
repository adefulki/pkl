package com.kota201.jtk.pkl;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.developers.smartytoast.SmartyToast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kota201.jtk.pkl.model.Dagangan;
import com.kota201.jtk.pkl.restful.PostMethod;
import com.kota201.jtk.pkl.service.NetworkChangeReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.AndroidLoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LokasiPedagangMemberActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ClusterManager.OnClusterClickListener<Dagangan>,
        ClusterManager.OnClusterInfoWindowClickListener<Dagangan>,
        ClusterManager.OnClusterItemClickListener<Dagangan>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Dagangan>{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private MenuItem searchItem;
    private SearchRecentSuggestions suggestions;
    private SearchView searchView;
    private LocationManager locationManager;
    private GoogleMap mMap;
    private int filter = 1;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private UiSettings mUiSettings;
    private ClusterManager<Dagangan> mClusterManager;
    private static final org.slf4j.Logger log;
    private BroadcastReceiver mNetworkReceiver;
    ProgressDialog progressDialog;
    private Dagangan clickedClusterItem;
    private ImageLoader imageLoader;
    private Handler handler;
    private Runnable runnable;
    private String userId;
    private String id;

    static {
        AndroidLoggerFactory.configureDefaultLogger(LokasiPedagangMemberActivity.class.getPackage());
        log = LoggerFactory.getLogger(LokasiPedagangMemberActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.info("onCreate() intent:{}", getIntent());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lokasi_pedagang_member);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SharedPreferences prefs = getSharedPreferences(String.valueOf(R.string.my_prefs), MODE_PRIVATE);
        id = prefs.getString("id", null);

        //Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //Broadcast Receiver

        mNetworkReceiver = new NetworkChangeReceiver();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //Google Maps
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Permission
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        //Search
        suggestions = new SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY,
                SuggestionProvider.MODE);

        suggestions.clearHistory();

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        log.debug("onCreateOptionsMenu() searchManager: {}", searchManager);

        searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setIconifiedByDefault(true);
        searchView.setMaxWidth(1000);

        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView
                .findViewById(android.support.v7.appcompat.R.id.search_src_text);

        // Collapse the search menu when the user hits the back key
        searchAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                log.trace("onFocusChange(): " + hasFocus);
                if (!hasFocus)
                    showSearch(false);
            }
        });

        try {
            // This sets the cursor
            // resource ID to 0 or @null
            // which will make it visible
            // on white background
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");

            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchAutoComplete, 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //image loader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(new ImageLoaderConfiguration.Builder(this).build());
        L.disableLogging();
    }

    //-------------------- START Navigation --------------------//
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_beranda) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_berlangganan) {
            startActivity(new Intent(LokasiPedagangMemberActivity.this, null));
        } else if (id == R.id.nav_obrolan) {
            startActivity(new Intent(LokasiPedagangMemberActivity.this, PenilaianProduk.class));
        } else if (id == R.id.nav_penilaian) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("Pindai QrCode Pedagang");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        }else if (id == R.id.nav_pengaturan_akun) {
            startActivity(new Intent(LokasiPedagangMemberActivity.this, SettingAkunPembeli.class));
        }else if (id == R.id.nav_keluar) {

            userId = "";
            editUserIdPembeli();

            SharedPreferences settings = getSharedPreferences(String.valueOf(R.string.my_prefs), Context.MODE_PRIVATE);
            settings.edit().clear().apply();
            Intent intent = new Intent(LokasiPedagangMemberActivity.this, LokasiPedagangActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
    //-------------------- END Navigation --------------------//

    //-------------------- START Google Maps --------------------//
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setIndoorLevelPickerEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setAllGesturesEnabled(true);

        // Add a cluster markers
        mClusterManager = new ClusterManager<Dagangan>(this, mMap);
        mClusterManager.setRenderer(new DaganganRenderer());
        final CameraPosition[] mPreviousCameraPosition = {null};
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                CameraPosition position = mMap.getCameraPosition();
                if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                    mPreviousCameraPosition[0] = mMap.getCameraPosition();
                    mClusterManager.cluster();
                }
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        mClusterManager
                .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Dagangan>() {
                    @Override
                    public boolean onClusterItemClick(Dagangan dagangan) {
                        clickedClusterItem = dagangan;
                        return false;
                    }
                });

        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(
                new MyCustomAdapterForItems());

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if (NetworkChangeReceiver.isNetworkAvailable(getBaseContext())) {
                    try {
                        new getDagangan().execute().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(runnable);

    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);

        super.onDestroy();
    }

    //-------------------- START Permission Access Fine Location --------------------//
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Dagangan> cluster) {

    }

    @Override
    public boolean onClusterItemClick(Dagangan dagangan) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Dagangan dagangan) {
        Intent intent = new Intent(LokasiPedagangMemberActivity.this, DetailPedagangMemberActivity.class);
        intent.putExtra("idDagangan", dagangan.getIdDagangan());
        startActivity(intent);
    }
    //-------------------- END Permission Access Fine Location --------------------//

    public class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyCustomAdapterForItems() {
            myContentsView = getLayoutInflater().inflate(
                    R.layout.info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub

            TextView mNamaDagangan = (TextView) myContentsView.findViewById(R.id.namaDagangan);
            RatingBar mNilaiPenilaian = (RatingBar) myContentsView.findViewById(R.id.nilaiPenilaian);
            TextView mJumlahPenilaian = (TextView) myContentsView.findViewById(R.id.jumlahPenilaian);
            final ImageView mFotoDagangan = (ImageView) myContentsView.findViewById(R.id.fotoDagangan);

            if (clickedClusterItem != null) {
                mNamaDagangan.setText(clickedClusterItem.getNamaDagangan());
                mNilaiPenilaian.setRating(clickedClusterItem.getMeanPenilaianDagangan());
                mJumlahPenilaian.setText("("+ clickedClusterItem.getCountPenilaianDagangan()+")");
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.default3)
                        .showImageForEmptyUri(R.drawable.default3)
                        .showImageOnFail(R.drawable.default3)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .build();
                imageLoader.displayImage("http://carmate.id/assets/image/" + clickedClusterItem.getFotoDagangan(), mFotoDagangan, options, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        mFotoDagangan.setImageBitmap(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {

                    }
                });
            }
            myContentsView.setBackgroundResource(R.drawable.rounded_corner);
            return myContentsView;
        }
    }

    //-------------------- START Marker Cluster --------------------//
    private class DaganganRenderer extends DefaultClusterRenderer<Dagangan> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mSingleImageView;
        private final ImageView mSingleStar;
        private final ImageView mSingleIcon;
        private final RelativeLayout mSingleFrame;


        private final ImageView mClusterImageView;
        private final ImageView mClusterStar;
        private final TextView mClusterTextView;
        private final int mDimension;

        public DaganganRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
            mClusterTextView = (TextView) multiProfile.findViewById(R.id.text);
            mClusterStar = (ImageView) multiProfile.findViewById(R.id.star);

            View singleProfile = getLayoutInflater().inflate(R.layout.single_profile, null);
            mIconGenerator.setContentView(singleProfile);
            mSingleImageView = (ImageView) singleProfile.findViewById(R.id.image);
            mSingleStar = (ImageView) singleProfile.findViewById(R.id.star);
            mSingleFrame = (RelativeLayout) singleProfile.findViewById(R.id.frame);
            mSingleIcon = (ImageView) singleProfile.findViewById(R.id.icon);
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
        }

        @Override
        protected void onBeforeClusterItemRendered(final Dagangan dagangan, MarkerOptions markerOptions) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.default3)
                    .showImageForEmptyUri(R.drawable.default3)
                    .showImageOnFail(R.drawable.default3)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();
            imageLoader.displayImage("http://carmate.id/assets/image/" + dagangan.getFotoDagangan(), mSingleImageView, options, new ImageLoadingListener() {

                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mSingleImageView.setImageBitmap(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {

                }
            });

            imageLoader.displayImage("http://carmate.id/assets/image/" + dagangan.getFotoDagangan(), mSingleImageView);
            mSingleStar.setVisibility(View.INVISIBLE);
            if (dagangan.getStatusRecommendation() == Boolean.TRUE)
                mSingleStar.setVisibility(View.VISIBLE);

            if (dagangan.getTipeDagangan() == Boolean.FALSE) {
                mSingleIcon.setImageResource(R.drawable.icon_diam);
                if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                    mSingleFrame.setBackgroundResource(R.color.colorOnline);
                else mSingleFrame.setBackgroundResource(R.color.colorOffline);
            } else {
                mSingleIcon.setImageResource(R.drawable.icon_berjalan);
                mSingleFrame.setBackgroundResource(R.color.colorOnline);
            }
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Dagangan> cluster, MarkerOptions markerOptions) {
            int i = 0;
            mClusterStar.setVisibility(View.INVISIBLE);
            for (Dagangan p : cluster.getItems()) {
                if (p.getStatusRecommendation() == Boolean.TRUE)
                    mClusterStar.setVisibility(View.VISIBLE);
                i++;
            }
            mClusterTextView.setText(String.valueOf(i));

            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Dagangan> cluster) {
        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public class getDagangan extends AsyncTask<Void, Void, ArrayList<Dagangan>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mClusterManager.clearItems();
            Log.i("test","clear");
            Log.i("test",mClusterManager.getClusterMarkerCollection().getMarkers().toString());
        }

        @Override
        protected ArrayList<Dagangan> doInBackground(Void... voids) {
            ArrayList<Dagangan> listDagangan = new ArrayList<>();
            OkHttpClient client;
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            client=okBuilder.build();
            Request.Builder builder = new Request.Builder();
            builder.cacheControl(new CacheControl.Builder().noCache().noStore().build());
            builder.url("http://carmate.id/index.php/Dagangan_controller/getAllDaganganLocation");
            Request request = builder.build();

            String jsonData = null;
            JSONArray Jobject = null;

            try {
                Response response = client.newCall(request).execute();
                jsonData = response.body().string();
                Jobject = new JSONArray(jsonData);

                for (int i = 0; i < Jobject.length(); i++) {
                    JSONObject c = null;
                    Dagangan dagangan = new Dagangan();
                    c = Jobject.getJSONObject(i);
                    dagangan.setIdDagangan(c.getString("idDagangan"));
                    dagangan.setNamaDagangan(c.getString("namaDagangan"));
                    dagangan.setFotoDagangan(c.getString("fotoDagangan"));
                    dagangan.setLatDagangan(c.getDouble("latDagangan"));
                    Log.i("test-lat", String.valueOf(c.getDouble("latDagangan")));
                    dagangan.setLngDagangan(c.getDouble("lngDagangan"));
                    Log.i("test-lng", String.valueOf(c.getDouble("lngDagangan")));
                    dagangan.setMeanPenilaianDagangan((int) c.getDouble("meanPenilaianDagangan"));
                    dagangan.setCountPenilaianDagangan(c.getInt("countPenilaianDagangan"));
                    dagangan.setStatusRecommendation(c.getBoolean("statusRecommendation"));
                    dagangan.setStatusBerjualan(c.getBoolean("statusBerjualan"));
                    dagangan.setTipeDagangan(c.getBoolean("tipeDagangan"));
                    listDagangan.add(dagangan);
                }

                response.body().close();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return listDagangan;
        }

        @Override
        protected void onPostExecute(ArrayList<Dagangan> listDagangan) {
            super.onPostExecute(listDagangan);
            for (Dagangan dagangan : listDagangan) {
                if (dagangan.getTipeDagangan() == Boolean.FALSE) {
                    if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                        mClusterManager.addItem(dagangan);
                    else {
                        mClusterManager.addItem(dagangan);
                    }
                } else {
                    if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                        mClusterManager.addItem(dagangan);
                }
            }
            mClusterManager.cluster();
            Log.i("test","berhasil "+listDagangan.toString());
            Log.i("test",mClusterManager.getClusterMarkerCollection().getMarkers().toString());
        }
    }
    //-------------------- END Marker Cluster --------------------//

    //-------------------- END Google Maps --------------------//

    //-------------------- START Search --------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        searchItem = menu.add(android.R.string.search_go);

        searchItem.setIcon(R.drawable.ic_search_white);

        MenuItemCompat.setActionView(searchItem, searchView);

        MenuItemCompat.setShowAsAction(searchItem,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);


        getMenuInflater().inflate(R.menu.filter, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log.warn("onNewIntent() :{}", intent);
        showSearch(false);
        Bundle extras = intent.getExtras();
        String query = String.valueOf(extras.get(SearchManager.QUERY));
        suggestions.saveRecentQuery(query, "");

        intent = new Intent(LokasiPedagangMemberActivity.this, ResultSearchActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("filter", filter);
        startActivity(intent);
    }

    protected void showSearch(boolean visible) {
        if (visible)
            MenuItemCompat.expandActionView(searchItem);
        else
            MenuItemCompat.collapseActionView(searchItem);
    }

    /**
     * Called when the hardware search button is pressed
     */
    @Override
    public boolean onSearchRequested() {
        log.trace("onSearchRequested();");
        showSearch(true);

        // dont show the built-in search dialog
        return super.onSearchRequested();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.radioBtnDagangan:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                filter = 1;
                Log.i("filter", "Dagangan");
                return true;
            case R.id.radioBtnProduk:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                filter = 2;
                Log.i("filter", "Produk");
                return true;
            case R.id.radioBtnPedagang:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                filter = 3;
                Log.i("filter", "Pedagang");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //-------------------- END Search --------------------//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()!=null) {
                Log.i("test-resultQR", result.getContents());
                if (checkDagangan(result.getContents())) {
                    String idDagangan = result.getContents();
                    Log.i("test-resultQR", "berhasil");
                    Intent intent = new Intent(LokasiPedagangMemberActivity.this, PilihProduk.class);
                    intent.putExtra("idDagangan", idDagangan);
                    startActivity(intent);
                } else {
                    SmartyToast.makeText(getApplicationContext(), "QrCode Tidak Valid", SmartyToast.LENGTH_SHORT, SmartyToast.ERROR);
                }
            }
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
}
