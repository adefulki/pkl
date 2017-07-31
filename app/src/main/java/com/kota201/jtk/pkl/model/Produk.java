package com.kota201.jtk.pkl.model;

import java.util.ArrayList;

/**
 * Created by AdeFulki on 7/20/2017.
 */

public class Produk {
    private ArrayList<Penilaian> listPenilaian;
    private String idProduk;
    private String namaProduk;
    private String deskripsiProduk;
    private String hargaProduk;
    private String satuanProduk;
    private String fotoProduk;
    private int meanPenilaianProduk;
    private int countPenilaianProduk;

    public String getIdProduk() {
        return idProduk;
    }

    public void setIdProduk(String idProduk) {
        this.idProduk = idProduk;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public String getDeskripsiProduk() {
        return deskripsiProduk;
    }

    public void setDeskripsiProduk(String deskripsiProduk) {
        this.deskripsiProduk = deskripsiProduk;
    }

    public String getHargaProduk() {
        return hargaProduk;
    }

    public void setHargaProduk(String hargaProduk) {
        this.hargaProduk = hargaProduk;
    }

    public String getSatuanProduk() {
        return satuanProduk;
    }

    public void setSatuanProduk(String satuanProduk) {
        this.satuanProduk = satuanProduk;
    }

    public String getFotoProduk() {
        return fotoProduk;
    }

    public void setFotoProduk(String fotoProduk) {
        this.fotoProduk = fotoProduk;
    }

    public int getMeanPenilaianProduk() {
        return meanPenilaianProduk;
    }

    public void setMeanPenilaianProduk(int meanPenilaianProduk) {
        this.meanPenilaianProduk = meanPenilaianProduk;
    }

    public int getCountPenilaianProduk() {
        return countPenilaianProduk;
    }

    public void setCountPenilaianProduk(int countPenilaianProduk) {
        this.countPenilaianProduk = countPenilaianProduk;
    }

    public ArrayList<Penilaian> getListPenilaian() {
        return listPenilaian;
    }

    public void setListPenilaian(ArrayList<Penilaian> listPenilaian) {
        this.listPenilaian = listPenilaian;
    }
}
