package com.kota201.jtk.pkl;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.kota201.jtk.pkl.model.Dagangan;
import com.kota201.jtk.pkl.restful.GetImageMethod;
import com.kota201.jtk.pkl.restful.GetMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LokasiPedagangActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ClusterManager.OnClusterClickListener<Dagangan>{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private LocationManager locationManager;
    private GoogleMap mMap;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private UiSettings mUiSettings;
    private ClusterManager<Dagangan> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lokasi_pedagang);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        //Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

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
    }

    //-------------------- START Options Menu --------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lokasi_pedagang, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //-------------------- END Options Menu --------------------//

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
        } else if (id == R.id.nav_tentang){

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
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener(){
            @Override
            public void onCameraIdle(){

                CameraPosition position = mMap.getCameraPosition();
                if(mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom !=position.zoom){
                    mPreviousCameraPosition[0] = mMap.getCameraPosition();
                    mClusterManager.cluster();
                }
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        addItems();
        mClusterManager.cluster();
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
    //-------------------- END Permission Access Fine Location --------------------//

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
        protected void onBeforeClusterItemRendered(Dagangan dagangan, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            GetImageMethod getImageMethod = new GetImageMethod();

            byte[] image = new byte[0];
            try{
                image = getImageMethod.execute("http://carmate.id/assets/image/"+dagangan.getFotoDagangan()+".jpg").get();
                if (image != null && image.length > 0){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    mSingleImageView.setImageBitmap(bitmap);
                }

                if (dagangan.getStatusRecommendation() == Boolean.FALSE)
                    mSingleStar.setVisibility(View.INVISIBLE);
                else mSingleStar.setVisibility(View.VISIBLE);

                if ( dagangan.getTipeDagangan() == Boolean.FALSE){
                    mSingleIcon.setImageResource(R.drawable.icon_diam);
                    if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                        mSingleFrame.setBackgroundResource(R.color.colorOnline);
                    else mSingleFrame.setBackgroundResource(R.color.colorOffline);
                }else{
                    mSingleIcon.setImageResource(R.drawable.icon_berjalan);
                    mSingleFrame.setBackgroundResource(R.color.colorOnline);
                }


            }catch (Exception e){
                e.printStackTrace();
            }

            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(dagangan.getNamaDagangan());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Dagangan> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;
            int counterRecomendation = 0;
            for (Dagangan p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;

                GetImageMethod getImageMethod = new GetImageMethod();

                byte[] image = new byte[0];
                try{
                    image = getImageMethod.execute("http://carmate.id/assets/image/"+p.getFotoDagangan()+".jpg").get();
                    if (image != null && image.length > 0){
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        drawable.setBounds(0, 0, width, height);
                        profilePhotos.add(drawable);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (p.getStatusRecommendation() == Boolean.TRUE)
                    counterRecomendation++;

            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            if (counterRecomendation > 0)
                mClusterStar.setVisibility(View.VISIBLE);
            mClusterImageView.setImageDrawable(multiDrawable);
            mClusterTextView.setText(String.valueOf(profilePhotos.size()));

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
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().getNamaDagangan();
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

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

        for (Dagangan dagangan: listDagangan) {
            if(dagangan.getTipeDagangan() == Boolean.FALSE){
                if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                    mClusterManager.addItem(dagangan);
                else{
                    mClusterManager.addItem(dagangan);
                }
            } else{
                if (dagangan.getStatusBerjualan() == Boolean.TRUE)
                    mClusterManager.addItem(dagangan);
            }

        }
    }

    public ArrayList<Dagangan> getDaganganLocation(){
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
                dagangan.setMeanPenilaianDagangan(c.getDouble("meanPenilaianDagangan"));
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
}
