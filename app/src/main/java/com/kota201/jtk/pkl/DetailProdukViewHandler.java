package com.kota201.jtk.pkl;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/30/2017.
 */

public class DetailProdukViewHandler extends RecyclerView.ViewHolder {

    @BindView(R.id.fotoProduk) ImageView fotoProduk;
    @BindView(R.id.namaProduk) TextView namaProduk;
    @BindView(R.id.deskripsiProduk) TextView deskripsiProduk;
    @BindView(R.id.hargaProduk) TextView hargaProduk;
    @BindView(R.id.satuanProduk) TextView satuanProduk;
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
    @BindView(R.id.meanPenilaianProduk) TextView meanPenilaianProduk;
    @BindView(R.id.countPenilaianProduk) TextView countPenilaianProduk;
    @BindView(R.id.linearLayout3)
    LinearLayout linearLayout;
    @BindView(R.id.barPenilaianProduk) LinearLayout linearLayout2;

    public DetailProdukViewHandler(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}
