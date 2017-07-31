package com.kota201.jtk.pkl;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kota201.jtk.pkl.model.Penilaian;
import com.kota201.jtk.pkl.model.Produk;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;

/**
 * Created by AdeFulki on 7/30/2017.
 */

public class DetailProdukAdapter extends RecyclerView.Adapter<DetailProdukViewHandler>{
    private ArrayList<Produk> listProduk;
    private ArrayList<Penilaian> listPenilaian;
    private LayoutInflater inflater;

    public DetailProdukAdapter(LayoutInflater layoutInflater, ArrayList<Produk> list){
        listProduk = list;
        inflater=layoutInflater;
    }

    @Override
    public DetailProdukViewHandler onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflater.inflate(R.layout.item_detail_produk, parent, false);
        DetailProdukViewHandler viewHolder=new DetailProdukViewHandler(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final DetailProdukViewHandler holder, int position) {
        Produk produk = listProduk.get(position);
        holder.namaProduk.setText(produk.getNamaProduk());
        holder.deskripsiProduk.setText(produk.getDeskripsiProduk());
        holder.hargaProduk.setText(produk.getHargaProduk());
        holder.satuanProduk.setText(produk.getSatuanProduk());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, inflater.getContext().getResources().getDisplayMetrics())))
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage("http://carmate.id/assets/image/" + produk.getFotoProduk(), holder.fotoProduk, options);
        holder.countPenilaianProduk.setText(String.valueOf(produk.getCountPenilaianProduk()));
        holder.meanPenilaianProduk.setText(String.valueOf(produk.getMeanPenilaianProduk()));

        listPenilaian = produk.getListPenilaian();
        int count1=0;
        int count2=0;
        int count3=0;
        int count4=0;
        int count5=0;
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
        holder.bintangSatu.setText(String.valueOf(count1));
        holder.bintangDua.setText(String.valueOf(count2));
        holder.bintangTiga.setText(String.valueOf(count3));
        holder.bintangEmpat.setText(String.valueOf(count4));
        holder.bintangLima.setText(String.valueOf(count5));

        holder.progressBarBintangSatu.setMax(100);
        holder.progressBarBintangDua.setMax(100);
        holder.progressBarBintangTiga.setMax(100);
        holder.progressBarBintangEmpat.setMax(100);
        holder.progressBarBintangLima.setMax(100);

        if (count1!=0)
        holder.progressBarBintangSatu.setProgress((count1/produk.getCountPenilaianProduk())*100);
        if (count2!=0)
        holder.progressBarBintangDua.setProgress((count2/produk.getCountPenilaianProduk())*100);
        if (count3!=0)
        holder.progressBarBintangTiga.setProgress((count3/produk.getCountPenilaianProduk())*100);
        if (count4!=0)
        holder.progressBarBintangEmpat.setProgress((count4/produk.getCountPenilaianProduk())*100);
        if (count5!=0)
        holder.progressBarBintangLima.setProgress((count5/produk.getCountPenilaianProduk())*100);



        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.meanPenilaianProduk.getVisibility() == View.GONE)
                    holder.meanPenilaianProduk.setVisibility(View.VISIBLE);
                else
                    holder.meanPenilaianProduk.setVisibility(View.GONE);
                if(holder.linearLayout2.getVisibility() == View.GONE)
                    holder.linearLayout2.setVisibility(View.VISIBLE);
                else
                    holder.linearLayout2.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listProduk.size();
    }
}
