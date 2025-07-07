package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;


@Entity
public class LigneReapprovisionnement  {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Reapprovisionnement reapprovisionnement;

    @ManyToOne
    private Produit produit;

    private int qteAjoutee;
    private String entrepotNom;




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reapprovisionnement getReapprovisionnement() {
        return reapprovisionnement;
    }

    public void setReapprovisionnement(Reapprovisionnement reapprovisionnement) {
        this.reapprovisionnement = reapprovisionnement;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQteAjoutee() {
        return qteAjoutee;
    }

    public void setQteAjoutee(int qteAjoutee) {
        this.qteAjoutee = qteAjoutee;
    }

    public String getEntrepotNom() {
        return entrepotNom;
    }

    public void setEntrepotNom(String entrepotNom) {
        this.entrepotNom = entrepotNom;
    }
}
