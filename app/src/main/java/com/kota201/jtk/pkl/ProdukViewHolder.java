package com.kota201.jtk.pkl;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class ProdukViewHolder extends RecyclerView.ViewHolder{
    ImageView fotoProduk;
    TextView namaProduk;
    TextView deskripsiProduk;
    TextView hargaProduk;
    TextView satuanProduk;
    ImageButton editButton;
    public ProdukViewHolder(View itemView) {
        super(itemView);
        fotoProduk = (ImageView) itemView.findViewById(R.id.fotoProduk);
        namaProduk= (TextView) itemView.findViewById(R.id.namaProduk);
        deskripsiProduk= (TextView) itemView.findViewById(R.id.deskripsiProduk);
        hargaProduk= (TextView) itemView.findViewById(R.id.hargaProduk);
        satuanProduk= (TextView) itemView.findViewById(R.id.satuanProduk);
        editButton= (ImageButton) itemView.findViewById(R.id.editButton);
    }
}
