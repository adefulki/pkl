package com.kota201.jtk.pkl.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kota201.jtk.pkl.R;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class TentangDetailPedagangFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.produk_detail_pedagang, container, false);
    }
}
