package com.kota201.jtk.pkl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.kota201.jtk.pkl.model.Search;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/12/2017.
 */


public class ResultSearchActivity extends AppCompatActivity{
    ArrayList<Search> listSearch = new ArrayList<>();

    private String query;
    private int filter;
    private RecyclerView recyclerView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_search);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hasil Pencarian");

        recyclerView= (RecyclerView) findViewById(R.id.recycler_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            query = extras.getString("query");
            filter = extras.getInt("filter");
            Log.i("query",query);
            Log.i("filter", String.valueOf(filter));
            String userQuery = extras.getString("userQuery");

        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
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
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        JSONObject dataToSend = null;
        try {
            dataToSend = new JSONObject()
                    .put("kataKunci", query)
                    .put("latPembeli", latitude)
                    .put("lngPembeli", longitude)
                    .put("filter", filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert dataToSend != null;

        ResultAdapter adapter=new ResultAdapter(this,dataToSend);
        recyclerView.setAdapter(adapter);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
