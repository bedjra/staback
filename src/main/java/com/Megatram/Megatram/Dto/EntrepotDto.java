package com.Megatram.Megatram.Dto;

public class EntrepotDto {

    private Long id;
    private String nom;
    private String ref;
    private int quantite;
    private double valeurVente;

    // Getters / Setters

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

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getValeurVente() {
        return valeurVente;
    }

    public void setValeurVente(double valeurVente) {
        this.valeurVente = valeurVente;
    }
}
