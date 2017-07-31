package com.kota201.jtk.pkl.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kota201.jtk.pkl.DetailPedagangMemberActivity;
import com.kota201.jtk.pkl.R;
import com.kota201.jtk.pkl.model.Pedagang;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class TentangDetailPedagangMemberFragment extends Fragment{
    @BindView(R.id.fotoPedagang)
    ImageView fotoPedagang;
    @BindView(R.id.namaPedagang)
    TextView namaPedagang;
    @BindView(R.id.alamatPedagang)
    TextView alamatPedagang;
    @BindView(R.id.noPonselPedagang)
    TextView noPonselPedagang;
    @BindView(R.id.emailPedagang)
    TextView emailPedagang;

    private Pedagang pedagang;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DetailPedagangMemberActivity activity = (DetailPedagangMemberActivity) getActivity();
        pedagang = activity.getPedagang();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tentang_detail_pedagang, container, false);
        ButterKnife.bind(this, view);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://carmate.id/assets/image/" + pedagang.getFotoPedagang(), fotoPedagang);
        namaPedagang.setText(pedagang.getNamaPedagang());
        alamatPedagang.setText(pedagang.getAlamatPedagang());
        noPonselPedagang.setText(pedagang.getNoPonselPedagang());
        emailPedagang.setText(pedagang.getEmailPedagang());
        return view;
    }
}
