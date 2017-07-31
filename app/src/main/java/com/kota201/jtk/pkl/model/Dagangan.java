package com.kota201.jtk.pkl.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by AdeFulki on 5/29/2017.
 */

public class Dagangan implements ClusterItem {
    private String idDagangan;
    private String namaDagangan;
    private String deskripsiDagangan;
    private String fotoDagangan;
    private Double latDagangan;
    private Double lngDagangan;
    private int meanPenilaianDagangan;
    private int countPenilaianDagangan;
    private Boolean statusRecommendation;
    private Boolean statusBerjualan;
    private Boolean tipeDagangan;
    private int countPelanggan;
    private Boolean statusBerlangganan;

    public String getIdDagangan() {
        return idDagangan;
    }

    public void setIdDagangan(String idDagangan) {
        this.idDagangan = idDagangan;
    }

    public String getNamaDagangan() {
        return namaDagangan;
    }

    public void setNamaDagangan(String namaDagangan) {
        this.namaDagangan = namaDagangan;
    }

    public String getDeskripsiDagangan() {
        return deskripsiDagangan;
    }

    public void setDeskripsiDagangan(String deskripsiDagangan) {
        this.deskripsiDagangan = deskripsiDagangan;
    }

    public String getFotoDagangan() {
        return fotoDagangan;
    }

    public void setFotoDagangan(String fotoDagangan) {
        this.fotoDagangan = fotoDagangan;
    }

    public Double getLatDagangan() {
        return latDagangan;
    }

    public void setLatDagangan(Double latDagangan) {
        this.latDagangan = latDagangan;
    }

    public Double getLngDagangan() {
        return lngDagangan;
    }

    public void setLngDagangan(Double lngDagangan) {
        this.lngDagangan = lngDagangan;
    }

    public int getMeanPenilaianDagangan() {
        return meanPenilaianDagangan;
    }

    public void setMeanPenilaianDagangan(int meanPenilaianDagangan) {
        this.meanPenilaianDagangan = meanPenilaianDagangan;
    }

    public int getCountPenilaianDagangan() {
        return countPenilaianDagangan;
    }

    public void setCountPenilaianDagangan(int countPenilaianDagangan) {
        this.countPenilaianDagangan = countPenilaianDagangan;
    }

    public Boolean getStatusRecommendation() {
        return statusRecommendation;
    }

    public void setStatusRecommendation(Boolean statusRecommendation) {
        this.statusRecommendation = statusRecommendation;
    }

    public Boolean getStatusBerjualan() {
        return statusBerjualan;
    }

    public void setStatusBerjualan(Boolean statusBerjualan) {
        this.statusBerjualan = statusBerjualan;
    }

    public Boolean getTipeDagangan() {
        return tipeDagangan;
    }

    public void setTipeDagangan(Boolean tipeDagangan) {
        this.tipeDagangan = tipeDagangan;
    }

    public LatLng getPosition() {
        return new LatLng(latDagangan, lngDagangan);
    }

    public int getCountPelanggan() {
        return countPelanggan;
    }

    public void setCountPelanggan(int countPelanggan) {
        this.countPelanggan = countPelanggan;
    }

    public Boolean getStatusBerlangganan() {
        return statusBerlangganan;
    }

    public void setStatusBerlangganan(Boolean statusBerlangganan) {
        this.statusBerlangganan = statusBerlangganan;
    }
}
