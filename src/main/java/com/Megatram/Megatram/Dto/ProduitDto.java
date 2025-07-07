package com.Megatram.Megatram.Dto;

import com.Megatram.Megatram.Entity.Produit;

public class ProduitDto {

    private Long id;
    private String nom;
    private String ref;
    private int qte;
    private double prix;
    private String codeBarre;
    private Long categorieId;
    private Long entrepotId;
    private int qteMin;

    private String categorieNom;
    private String entrepotNom;


    public ProduitDto() {
        // constructeur vide requis
    }

    public ProduitDto(Produit produit) {
        this.id = produit.getId();
        this.nom = produit.getNom();
        this.ref = produit.getRef();
        this.qte = produit.getQte();
        this.qteMin = produit.getQteMin();
        this.prix = produit.getPrix();
        this.codeBarre = produit.getCodeBarre();
        this.categorieNom = produit.getCategorie() != null ? produit.getCategorie().getNom() : null;
        this.categorieId = produit.getCategorie() != null ? produit.getCategorie().getId() : null;
        this.entrepotNom = produit.getEntrepot() != null ? produit.getEntrepot().getNom() : null;
        this.entrepotId = produit.getEntrepot() != null ? produit.getEntrepot().getId() : null;
    }
    // Getters et setters

    public Long getEntrepotId() {
        return entrepotId;
    }

    public int getQteMin() {
        return qteMin;
    }

    public void setQteMin(int qteMin) {
        this.qteMin = qteMin;
    }

    public void setEntrepotId(Long entrepotId) {
        this.entrepotId = entrepotId;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
    }

    public String getEntrepotNom() {
        return entrepotNom;
    }

    public void setEntrepotNom(String entrepotNom) {
        this.entrepotNom = entrepotNom;
    }
}
