package com.kota201.jtk.pkl;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.kota201.jtk.pkl.model.Dagangan;
import com.kota201.jtk.pkl.restful.GetMethod;
import com.kota201.jtk.pkl.service.NetworkChangeReceiver;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.willy.ratingbar.ScaleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.AndroidLoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LokasiPedagangActivity extends AppCompatActivity implements
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
    private Handler handler;
    private Runnable runnable;
    private BroadcastReceiver mNetworkReceiver;
    ProgressDialog progressDialog;
    private Dagangan clickedClusterItem;
    private ImageLoader imageLoader;

    static {
        AndroidLoggerFactory.configureDefaultLogger(LokasiPedagangActivity.class.getPackage());
        log = LoggerFactory.getLogger(LokasiPedagangActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.info("onCreate() intent:{}", getIntent());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lokasi_pedagang);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
    }

    //-------------------- START Navigation --------------------//
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_beranda) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_login) {
            startActivity(new Intent(LokasiPedagangActivity.this, LoginActivity.class));
        } else if (id == R.id.nav_registrasi) {
            startActivity(new Intent(LokasiPedagangActivity.this, SignupActivity.class));
        } else if (id == R.id.nav_tentang) {

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

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (NetworkChangeReceiver.isNetworkAvailable(getBaseContext())) {
                    mClusterManager.clearItems();
                    addItems();
                    mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(
                            new MyCustomAdapterForItems());
                    mClusterManager.cluster();
                }
            }
        };
        handler.postDelayed(runnable, 10000);
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
            ScaleRatingBar mNilaiPenilaian = (ScaleRatingBar) myContentsView.findViewById(R.id.nilaiPenilaian);
            TextView mJumlahPenilaian = (TextView) myContentsView.findViewById(R.id.jumlahPenilaian);
            ImageView mFotoDagangan = (ImageView) myContentsView.findViewById(R.id.fotoDagangan);

            if (clickedClusterItem != null) {
                mNamaDagangan.setText(clickedClusterItem.getNamaDagangan());
                mNilaiPenilaian.setRating(clickedClusterItem.getMeanPenilaianDagangan());
                mNilaiPenilaian.setNumStars(5);
                mNilaiPenilaian.setTouchable(false);
                mJumlahPenilaian.setText("("+ clickedClusterItem.getCountPenilaianDagangan()+")");
                imageLoader.displayImage("http://carmate.id/assets/image/" + clickedClusterItem.getFotoDagangan(), mFotoDagangan);
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
        private final FrameLayout mSingleFrame;


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
            mSingleFrame = (FrameLayout) singleProfile.findViewById(R.id.frame);
            mSingleIcon = (ImageView) singleProfile.findViewById(R.id.icon);
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
        }

        @Override
        protected void onBeforeClusterItemRendered(final Dagangan dagangan, MarkerOptions markerOptions) {
            imageLoader.displayImage("http://carmate.id/assets/image/" + dagangan.getFotoDagangan(), mSingleImageView);
            if (dagangan.getStatusRecommendation() == Boolean.FALSE)
                mSingleStar.setVisibility(View.INVISIBLE);
            else mSingleStar.setVisibility(View.VISIBLE);

            if (dagangan.getTipeDagangan() == Boolean.FALSE) {
                imageLoader.displayImage("drawable://" + R.drawable.icon_diam, mSingleIcon);
                if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                    mSingleFrame.setBackgroundResource(R.color.colorOnline);
                else mSingleFrame.setBackgroundResource(R.color.colorOffline);
            } else {
                imageLoader.displayImage("drawable://" + R.drawable.icon_berjalan, mSingleIcon);
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

    private void addItems() {
        ArrayList<Dagangan> listDagangan = new ArrayList<>();
        listDagangan = getDaganganLocation();

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
    }

    public ArrayList<Dagangan> getDaganganLocation() {
        ArrayList<Dagangan> listDagangan = new ArrayList<>();

        GetMethod getMethod = (GetMethod) new GetMethod().execute(
                "http://carmate.id/index.php/Dagangan_controller/getAllDaganganLocation");
        String jsonData = null;
        JSONArray Jobject = null;

        try {
            jsonData = getMethod.get();
            Jobject = new JSONArray(jsonData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < Jobject.length(); i++) {
            JSONObject c = null;
            Dagangan dagangan = new Dagangan();
            try {
                c = Jobject.getJSONObject(i);
                dagangan.setIdDagangan(c.getString("idDagangan"));
                dagangan.setNamaDagangan(c.getString("namaDagangan"));
                dagangan.setFotoDagangan(c.getString("fotoDagangan"));
                dagangan.setLatDagangan(c.getDouble("latDagangan"));
                dagangan.setLngDagangan(c.getDouble("lngDagangan"));
                dagangan.setMeanPenilaianDagangan((int) c.getDouble("meanPenilaianDagangan"));
                dagangan.setCountPenilaianDagangan(c.getInt("countPenilaianDagangan"));
                dagangan.setStatusRecommendation(c.getBoolean("statusRecommendation"));
                dagangan.setStatusBerjualan(c.getBoolean("statusBerjualan"));
                dagangan.setTipeDagangan(c.getBoolean("tipeDagangan"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            listDagangan.add(dagangan);
        }


        return listDagangan;
    }
    //-------------------- END Marker Cluster --------------------//

    //-------------------- END Google Maps --------------------//

    //-------------------- START Search --------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        searchItem = menu.add(android.R.string.search_go);

        searchItem.setIcon(R.drawable.ic_search);

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

        intent = new Intent(LokasiPedagangActivity.this, ResultSearchActivity.class);
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

}
