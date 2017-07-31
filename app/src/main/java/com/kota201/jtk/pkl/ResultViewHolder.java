package com.kota201.jtk.pkl;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by AdeFulki on 7/14/2017.
 */

public class ResultViewHolder extends RecyclerView.ViewHolder {
    TextView tv1,tv2;
    ImageView imageView, imageView2;
    CardView card_view;

    public ResultViewHolder(View itemView) {
        super(itemView);
        tv1= (TextView) itemView.findViewById(R.id.daftar_judul);
        tv2= (TextView) itemView.findViewById(R.id.daftar_deskripsi);
        imageView= (ImageView) itemView.findViewById(R.id.daftar_icon);
        imageView2= (ImageView) itemView.findViewById(R.id.daftar_icon2);
        card_view=(CardView) itemView.findViewById(R.id.card_view);
    }
}