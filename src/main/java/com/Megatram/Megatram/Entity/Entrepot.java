package com.Megatram.Megatram.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Entrepot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String ref; // Référence fonctionnelle de l'entrepôt

    @OneToMany(mappedBy = "entrepot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produit> produits = new ArrayList<>();

//     🔸 Champ calculé dynamiquement, pas stocké en base
    @Transient
    public int getQuantite() {
        return produits.stream()
                .mapToInt(Produit::getQte)
                .sum();
    }

    @Transient
    public double getValeurVente() {
        return produits.stream()
                .mapToDouble(p -> p.getQte() * p.getPrix())
                .sum();
    }

    // Getters & Setters

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

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }
}
