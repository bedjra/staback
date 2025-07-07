package com.Megatram.Megatram.service;


import com.Megatram.Megatram.Dto.LigneVenteDto;
import com.Megatram.Megatram.Dto.VenteDto;
import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.LigneVente;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.Vente;
import com.Megatram.Megatram.repository.ClientRepository;
import com.Megatram.Megatram.repository.LigneVenteRepo;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.VenteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VenteService {

    @Autowired
    private VenteRepository venteRepository;

    @Autowired
    private ProduitRepos produitRepository;

    @Autowired
    private LigneVenteRepo ligneVenteRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Transactional
    public Vente creerVente(VenteDto venteDto) {
        // Vérifier chaque ligne avant création
        for (LigneVenteDto ligneDto : venteDto.getLignes()) {
            Produit produit = produitRepository.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable: id=" + ligneDto.getProduitId()));

            if (produit.getQte() < ligneDto.getQteVendu()) {
                throw new RuntimeException("Stock insuffisant pour le produit : " + produit.getNom());
            }
        }

        // Tout est ok -> Création de la vente
        Vente vente = new Vente();
        vente.setRef(venteDto.getRef());
        vente.setCaissier(venteDto.getCaissier());


        Client client = clientRepository.findById(venteDto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable : ID = " + venteDto.getClientId()));
        vente.setClient(client);


        // Création des lignes de vente
        List<LigneVente> lignes = venteDto.getLignes().stream().map(dto -> {
            LigneVente ligne = new LigneVente();
            ligne.setProduitId(dto.getProduitId());
            ligne.setQteVendu(dto.getQteVendu());
            ligne.setProduitPrix(dto.getProduitPrix());
            ligne.setVente(vente);
            // total sera calculé dans @PrePersist
            return ligne;
        }).collect(Collectors.toList());

        vente.setLignes(lignes);

        // Sauvegarder la vente + lignes (cascade)
        Vente venteEnregistree = venteRepository.save(vente);

        // Mettre à jour le stock des produits (décrémenter)
        for (LigneVente ligne : venteEnregistree.getLignes()) {
            Produit produit = produitRepository.findById(ligne.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable lors mise à jour stock"));

            int nouvelleQte = produit.getQte() - ligne.getQteVendu();
            produit.setQte(nouvelleQte);
            produitRepository.save(produit);
        }

        return venteEnregistree;
    }

    // ----- Méthodes CRUD classiques -----

    public List<VenteDto> getAllVenteDtos() {
        List<Vente> ventes = venteRepository.findAll();

        return ventes.stream().map(this::mapToDto).toList();
    }


    private VenteDto mapToDto(Vente vente) {
        VenteDto dto = new VenteDto();
        dto.setId(vente.getId());
        dto.setRef(vente.getRef());
        dto.setCaissier(vente.getCaissier());
        dto.setClientNom(vente.getClient().getNom());
        dto.setPaiement(vente.getPaiement());
        dto.setDate(vente.getDate());

        // On charge tous les produits d’un coup
        List<Long> produitIds = vente.getLignes().stream()
                .map(LigneVente::getProduitId)
                .distinct()
                .toList();

        Map<Long, String> nomProduitsMap = produitRepository.findAllById(produitIds).stream()
                .collect(Collectors.toMap(Produit::getId, Produit::getNom));

        List<LigneVenteDto> lignes = vente.getLignes().stream().map(ligne -> {
            LigneVenteDto l = new LigneVenteDto();
            l.setId(ligne.getId());
            l.setProduitId(ligne.getProduitId());
            l.setQteVendu(ligne.getQteVendu());
            l.setProduitPrix(ligne.getProduitPrix());
            l.setTotal(ligne.getTotal());
            l.setProduitNom(nomProduitsMap.getOrDefault(ligne.getProduitId(), "Inconnu"));
            return l;
        }).toList();

        dto.setLignes(lignes);
        return dto;
    }


    public Optional<Vente> getVenteById(Long id) {
        return venteRepository.findById(id);
    }

    @Transactional
    public void deleteVente(Long id) {
        // Avant suppression, on peut éventuellement remettre le stock à jour en rajoutant les quantités des lignes
        Optional<Vente> optVente = venteRepository.findById(id);
        if (optVente.isPresent()) {
            Vente vente = optVente.get();
            // Remise à jour stock (retour des produits)
            for (LigneVente ligne : vente.getLignes()) {
                Produit produit = produitRepository.findById(ligne.getProduitId())
                        .orElseThrow(() -> new RuntimeException("Produit introuvable lors mise à jour stock suppression"));
                produit.setQte(produit.getQte() + ligne.getQteVendu());
                produitRepository.save(produit);
            }

            venteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Vente non trouvée avec id: " + id);
        }
    }



    }
