package com.kota201.jtk.pkl.model;

/**
 * Created by AdeFulki on 7/31/2017.
 */

public class Penilaian {
    private String idPenilaian;
    private String idPembeli;
    private int nilaiPenilaian;
    private String deskripsiPenilaian;

    public String getIdPenilaian() {
        return idPenilaian;
    }

    public void setIdPenilaian(String idPenilaian) {
        this.idPenilaian = idPenilaian;
    }

    public String getIdPembeli() {
        return idPembeli;
    }

    public void setIdPembeli(String idPembeli) {
        this.idPembeli = idPembeli;
    }

    public int getNilaiPenilaian() {
        return nilaiPenilaian;
    }

    public void setNilaiPenilaian(int nilaiPenilaian) {
        this.nilaiPenilaian = nilaiPenilaian;
    }

    public String getDeskripsiPenilaian() {
        return deskripsiPenilaian;
    }

    public void setDeskripsiPenilaian(String deskripsiPenilaian) {
        this.deskripsiPenilaian = deskripsiPenilaian;
    }
}
