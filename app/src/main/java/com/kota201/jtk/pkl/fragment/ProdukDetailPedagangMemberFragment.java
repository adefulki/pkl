package com.kota201.jtk.pkl.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kota201.jtk.pkl.DetailPedagangMemberActivity;
import com.kota201.jtk.pkl.DetailProdukAdapter;
import com.kota201.jtk.pkl.R;
import com.kota201.jtk.pkl.model.Produk;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class ProdukDetailPedagangMemberFragment extends Fragment{
    @BindView(R.id.recycler_view)
    RecyclerView viewRecycler;

    private ArrayList<Produk> listProduk = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private DetailProdukAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DetailPedagangMemberActivity activity = (DetailPedagangMemberActivity) getActivity();
        listProduk = activity.getProduk();
        Log.i("test-listproduk",listProduk.toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.produk_detail_pedagang, container, false);
        ButterKnife.bind(this, view);
        layoutInflater = inflater;
        adapter=new DetailProdukAdapter(layoutInflater,listProduk);
        viewRecycler.setAdapter(adapter);
        viewRecycler.setHasFixedSize(true);
        viewRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }
}
