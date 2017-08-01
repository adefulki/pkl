package com.kota201.jtk.pkl.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.kota201.jtk.pkl.PedagangMainActivity;
import com.kota201.jtk.pkl.R;
import com.kota201.jtk.pkl.model.Dagangan;
import com.kota201.jtk.pkl.model.Penilaian;
import com.kota201.jtk.pkl.model.Produk;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class PenilaianPedagangFragment extends Fragment{
    @BindView(R.id.meanPenilaianDagangan)
    TextView meanPenilaianDagangan;
    @BindView(R.id.countPenilaianDagangan)
    TextView countPenilaianDagangan;
    @BindView(R.id.progressBarBintangSatu)
    RoundCornerProgressBar progressBarBintangSatu;
    @BindView(R.id.progressBarBintangDua)
    RoundCornerProgressBar progressBarBintangDua;
    @BindView(R.id.progressBarBintangTiga)
    RoundCornerProgressBar progressBarBintangTiga;
    @BindView(R.id.progressBarBintangEmpat)
    RoundCornerProgressBar progressBarBintangEmpat;
    @BindView(R.id.progressBarBintangLima)
    RoundCornerProgressBar progressBarBintangLima;
    @BindView(R.id.bintangSatu) TextView bintangSatu;
    @BindView(R.id.bintangDua) TextView bintangDua;
    @BindView(R.id.bintangTiga) TextView bintangTiga;
    @BindView(R.id.bintangEmpat) TextView bintangEmpat;
    @BindView(R.id.bintangLima) TextView bintangLima;

    private Dagangan dagangan;
    private ArrayList<Produk> listProduk;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PedagangMainActivity activity = (PedagangMainActivity) getActivity();
        dagangan = activity.getDagangan();
        listProduk = activity.getProduk();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.penilaian_detail_pedagang, container, false);
        ButterKnife.bind(this, view);

        meanPenilaianDagangan.setText(String.valueOf(dagangan.getMeanPenilaianDagangan()));
        countPenilaianDagangan.setText(String.valueOf(dagangan.getCountPenilaianDagangan()));

        int count1=0;
        int count2=0;
        int count3=0;
        int count4=0;
        int count5=0;
        for (Produk produk : listProduk){
            ArrayList<Penilaian> listPenilaian; 
            listPenilaian = produk.getListPenilaian();
            for (Penilaian penilaian:listPenilaian) {
                if (penilaian.getNilaiPenilaian()==1)
                    count1++;
                if (penilaian.getNilaiPenilaian()==2)
                    count2++;
                if (penilaian.getNilaiPenilaian()==3)
                    count3++;
                if (penilaian.getNilaiPenilaian()==4)
                    count4++;
                if (penilaian.getNilaiPenilaian()==5)
                    count5++;
            }
        }

        
        bintangSatu.setText(String.valueOf(count1));
        bintangDua.setText(String.valueOf(count2));
        bintangTiga.setText(String.valueOf(count3));
        bintangEmpat.setText(String.valueOf(count4));
        bintangLima.setText(String.valueOf(count5));

        if (count1!=0)
            progressBarBintangSatu.setProgress((count1/dagangan.getCountPenilaianDagangan())*100);
        if (count2!=0)
            progressBarBintangDua.setProgress((count2/dagangan.getCountPenilaianDagangan())*100);
        if (count3!=0)
            progressBarBintangTiga.setProgress((count3/dagangan.getCountPenilaianDagangan())*100);
        if (count4!=0)
            progressBarBintangEmpat.setProgress((count4/dagangan.getCountPenilaianDagangan())*100);
        if (count5!=0)
            progressBarBintangLima.setProgress((count5/dagangan.getCountPenilaianDagangan())*100);

        return view;
    }
}
