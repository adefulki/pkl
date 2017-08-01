package com.kota201.jtk.pkl.fragment;

/**
 * Created by Syam on 22/07/2017.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.kota201.jtk.pkl.R;

import java.util.ArrayList;
import java.util.List;

public class StatistikFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    BarChart chart,chart2 ;
    ArrayList<BarEntry> dataBar,dataBar2 ;
    ArrayList<String> namaBar,namaBar2 ;
    BarDataSet Bardataset,Bardataset2 ;
    BarData BARDATA,BARDATA2 ;

    public StatistikFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_statistik, container, false);

        Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        Spinner spinner2 = (Spinner) v.findViewById(R.id.spinner2);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> pengunjung = new ArrayList<String>();
        pengunjung.add("Perhari");
        pengunjung.add("Perbulan");
        pengunjung.add("Pertahun");

        spinner2.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> pelanggan = new ArrayList<String>();
        pelanggan.add("Perhari");
        pelanggan.add("Perbulan");
        pelanggan.add("Pertahun");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, pengunjung);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, pelanggan);

        // Drop down layout style - list view with radio button
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // mengambil data spinner
        spinner.setAdapter(dataAdapter);
        spinner2.setAdapter(dataAdapter2);

        // grafik untuk pengunjung
        chart = (BarChart) v.findViewById(R.id.chart1);
        dataBar = new ArrayList<>();
        namaBar = new ArrayList<String>();
        isiDataPengunjung();
        namaDataPengunjung();
        Bardataset = new BarDataSet(dataBar, "Pengunjung");
        BARDATA = new BarData(namaBar, Bardataset);
        Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chart.setData(BARDATA);
        chart.animateY(5000);


        //grafik untuk pelanggan
        chart2 = (BarChart) v.findViewById(R.id.chart2);
        dataBar2 = new ArrayList<>();
        namaBar2 = new ArrayList<String>();
        isiDataPlanggan();
        namaDataPelanggan();
        Bardataset2 = new BarDataSet(dataBar2, "Pelanggan");
        BARDATA2 = new BarData(namaBar2, Bardataset2);
        Bardataset2.setColors(ColorTemplate.COLORFUL_COLORS);
        chart2.setData(BARDATA2);
        chart2.animateY(5000);

        return v;
    }

    public void isiDataPengunjung(){

        dataBar.add(new BarEntry(2f, 0));
        dataBar.add(new BarEntry(4f, 1));
        dataBar.add(new BarEntry(6f, 2));
        dataBar.add(new BarEntry(8f, 3));
        dataBar.add(new BarEntry(7f, 4));
        dataBar.add(new BarEntry(3f, 5));

    }

    public void namaDataPengunjung(){

        namaBar.add("Januari");
        namaBar.add("Februari");
        namaBar.add("Maret");
        namaBar.add("April");
        namaBar.add("Mei");
        namaBar.add("Juni");

    }

    public void isiDataPlanggan(){

        dataBar2.add(new BarEntry(6f, 0));
        dataBar2.add(new BarEntry(3f, 1));
        dataBar2.add(new BarEntry(4f, 2));
        dataBar2.add(new BarEntry(7f, 3));
        dataBar2.add(new BarEntry(3f, 4));
        dataBar2.add(new BarEntry(2f, 5));

    }

    public void namaDataPelanggan(){

        namaBar2.add("Januari");
        namaBar2.add("Februari");
        namaBar2.add("Maret");
        namaBar2.add("April");
        namaBar2.add("Mei");
        namaBar2.add("Juni");

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
}
