package com.kota201.jtk.pkl.model;

/**
 * Created by AdeFulki on 7/12/2017.
 */

public class Search {
    private String id;
    private String idDagangan;
    private String nama;
    private String foto;
    private Double jarakHaversine;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Double getJarakHaversine() {
        return jarakHaversine;
    }

    public void setJarakHaversine(Double jarakHaversine) {
        this.jarakHaversine = jarakHaversine;
    }

    public String getIdDagangan() {
        return idDagangan;
    }

    public void setIdDagangan(String idDagangan) {
        this.idDagangan = idDagangan;
    }
}
