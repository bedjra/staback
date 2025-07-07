package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.*;
import com.Megatram.Megatram.Entity.LigneReapprovisionnement;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.Entity.Reapprovisionnement;
import com.Megatram.Megatram.repository.LigneReapprovisionnementRepository;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.repository.ReapprovisionnementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReapprovisionnementService {

    @Autowired
    private ReapprovisionnementRepository reapproRepo;
    @Autowired
    private LigneReapprovisionnementRepository ligneRepo;
    @Autowired
    private ProduitRepos produitRepo;

    public ReapprovisionnementResponseDto enregistrerReapprovisionnement(ReapprovisionnementRequestDto request) {
        Reapprovisionnement reapprovisionnement = new Reapprovisionnement();
        reapprovisionnement.setSource(request.source);
        reapprovisionnement.setAgent(request.agent);

        // Enregistrer pour obtenir l'ID
        reapprovisionnement = reapproRepo.save(reapprovisionnement);

        if (request.lignes == null || request.lignes.isEmpty()) {
            throw new IllegalArgumentException("Aucune ligne de réapprovisionnement reçue.");
        }

        ReapprovisionnementResponseDto response = new ReapprovisionnementResponseDto();
        response.id = reapprovisionnement.getId(); // ici on met "id" et non "reapprovisionnementId"
        response.source = reapprovisionnement.getSource();
        response.agent = reapprovisionnement.getAgent();
        response.lignes = new ArrayList<>();

        for (LigneReapprovisionnementDto dto : request.lignes) {
            Produit produit = produitRepo.findById(dto.produitId)
                    .orElseThrow(() -> new RuntimeException("Produit introuvable"));

            // Mise à jour de la quantité du produit
            produit.setQte(produit.getQte() + dto.qteAjoutee);
            produitRepo.save(produit);

            // Création et sauvegarde de la ligne
            LigneReapprovisionnement ligne = new LigneReapprovisionnement();
            ligne.setReapprovisionnement(reapprovisionnement);
            ligne.setProduit(produit);
            ligne.setQteAjoutee(dto.qteAjoutee);
            ligne.setEntrepotNom(dto.entrepotNom);
            ligne = ligneRepo.save(ligne); // pour avoir l'ID

            // Préparation de la réponse ligne par ligne
            LigneReapprovisionnementResponseDto ligneResponse = new LigneReapprovisionnementResponseDto();
            ligneResponse.id = ligne.getId();
            ligneResponse.produitId = produit.getId();
            ligneResponse.produitNom = produit.getNom();
            ligneResponse.qteAjoutee = dto.qteAjoutee;
            ligneResponse.entrepotNom = dto.entrepotNom;

            response.lignes.add(ligneResponse);
        }

        return response;
    }



    public List<ReapprovisionnementResponseDto> getAllReapprovisionnements() {
        List<Reapprovisionnement> reapprovisionnements = reapproRepo.findAll();

        return reapprovisionnements.stream().map(reappro -> {
            ReapprovisionnementResponseDto dto = new ReapprovisionnementResponseDto();
            dto.source = reappro.getSource();
            dto.agent = reappro.getAgent();
            dto.date = reappro.getDate();
            dto.id = reappro.getId();

            List<LigneReapprovisionnement> lignes = ligneRepo.findByReapprovisionnement(reappro);
            dto.lignes = lignes.stream().map(ligne -> {
                LigneReapprovisionnementResponseDto ligneDto = new LigneReapprovisionnementResponseDto();
                ligneDto.id = ligne.getId();
                ligneDto.produitId = ligne.getProduit().getId();
                ligneDto.produitNom = ligne.getProduit().getNom();
                ligneDto.qteAjoutee = ligne.getQteAjoutee();
                ligneDto.entrepotNom = ligne.getEntrepotNom();
                ligneDto.entrepotNom = ligne.getEntrepotNom();
                return ligneDto;
            }).toList();

            return dto;
        }).toList();
    }


    public ReapprovisionnementDetailsDto getDetails(Long id) {
        Reapprovisionnement r = reapproRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réapprovisionnement introuvable"));

        List<LigneReapprovisionnement> lignes = ligneRepo.findByReapprovisionnement(r);

        ReapprovisionnementDetailsDto dto = new ReapprovisionnementDetailsDto();
        dto.id = r.getId();
        dto.source = r.getSource();
        dto.agent = r.getAgent();
        dto.date = r.getDate();
        dto.lignes = lignes.stream().map(l -> {
            LigneReapprovisionnementDto ligneDto = new LigneReapprovisionnementDto();
            ligneDto.produitId = l.getProduit().getId();
            ligneDto.qteAjoutee = l.getQteAjoutee();
            ligneDto.entrepotNom = l.getEntrepotNom();
            ligneDto.produitNom = l.getProduit().getNom();
            return ligneDto;
        }).collect(Collectors.toList());

        return dto;
    }
}