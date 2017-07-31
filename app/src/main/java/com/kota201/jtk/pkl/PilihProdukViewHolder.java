package com.kota201.jtk.pkl;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class PilihProdukViewHolder extends RecyclerView.ViewHolder{
    @BindView(R.id.fotoProduk) ImageView fotoProduk;
    @BindView(R.id.namaProduk) TextView namaProduk;
    @BindView(R.id.deskripsiProduk) TextView deskripsiProduk;
    @BindView(R.id.hargaProduk) TextView hargaProduk;
    @BindView(R.id.satuanProduk) TextView satuanProduk;
    @BindView(R.id.card_view) CardView cardView;
    public PilihProdukViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}
