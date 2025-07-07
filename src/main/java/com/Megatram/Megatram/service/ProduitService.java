package com.Megatram.Megatram.service;


import com.Megatram.Megatram.Dto.AssignationProduitsDTO;
import com.Megatram.Megatram.Dto.ProduitDto;

import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.Entrepot;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.CategorieRep;
import com.Megatram.Megatram.repository.EntrepotRepository;

import com.Megatram.Megatram.repository.ProduitRepos;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProduitService {

    @Autowired
    private ProduitRepos produitRepository;

    @Autowired
    private CategorieRep categorieRepository;

    @Autowired
    private EntrepotRepository entrepotRepository;




    public ProduitDto createProduit(ProduitDto dto) {
        Produit produit = new Produit();
        produit.setNom(dto.getNom());
        produit.setRef(dto.getRef());
        produit.setQte(dto.getQte());
        produit.setPrix(dto.getPrix());
        produit.setQteMin(dto.getQteMin());

        // Générer un code-barres automatiquement (ex: à partir de ref + timestamp)
        String generatedCode = "BAR-" + dto.getRef() + "-" + System.currentTimeMillis();
        produit.setCodeBarre(generatedCode);

        if (dto.getCategorieId() != null) {
            Categorie cat = categorieRepository.findById(dto.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
            produit.setCategorie(cat);
        }

        if (dto.getEntrepotId() != null) {
            Entrepot ent = entrepotRepository.findById(dto.getEntrepotId())
                    .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
            produit.setEntrepot(ent);
        }

        Produit saved = produitRepository.save(produit);

        // Génération de l'image du code-barres automatiquement
        try {
            generateBarcodeImage(generatedCode, generatedCode + ".png");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du code-barres : " + e.getMessage());
        }

        // Retour DTO
        ProduitDto result = new ProduitDto();
        result.setId(saved.getId());
        result.setNom(saved.getNom());
        result.setRef(saved.getRef());
        result.setQte(saved.getQte());
        result.setPrix(saved.getPrix());
        result.setCodeBarre(saved.getCodeBarre());
        result.setCategorieId(saved.getCategorie() != null ? saved.getCategorie().getId() : null);
        result.setEntrepotId(saved.getEntrepot() != null ? saved.getEntrepot().getId() : null);
        result.setQteMin(saved.getQteMin());

        return result;
    }



    public List<ProduitDto> getAllProduits() {
        return produitRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }



    public ProduitDto getProduitById(Long id) {
        return produitRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + id));
    }


    public ProduitDto updateProduit(Long id, ProduitDto dto) {
        Produit p = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + id));

        p.setNom(dto.getNom());
        p.setRef(dto.getRef());
        p.setQte(dto.getQte());
        p.setPrix(dto.getPrix());
        p.setQteMin(dto.getQteMin());

        if (dto.getCategorieId() != null)
            categorieRepository.findById(dto.getCategorieId()).ifPresent(p::setCategorie);
        if (dto.getEntrepotId()  != null)
            entrepotRepository.findById(dto.getEntrepotId()).ifPresent(p::setEntrepot);

        Produit updated = produitRepository.save(p);
        return mapToDto(updated);
    }


    public void assignerCategorieEtEntrepot(AssignationProduitsDTO dto) {
        List<Produit> produits = produitRepository.findAllById(dto.getProduitIds());

        Categorie categorie = null;
        Entrepot entrepot = null;

        if (dto.getCategorieId() != null) {
            categorie = categorieRepository.findById(dto.getCategorieId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
        }

        if (dto.getEntrepotId() != null) {
            entrepot = entrepotRepository.findById(dto.getEntrepotId())
                    .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
        }

        for (Produit produit : produits) {
            if (categorie != null) {
                produit.setCategorie(categorie);
            }
            if (entrepot != null) {
                produit.setEntrepot(entrepot);
            }
        }

        produitRepository.saveAll(produits);
    }

//    public void deleteProduit(Long id) {
//        produitRepository.deleteById(id);
//    }
//
//    public void deleteProduitsByIds(List<Long> ids) {
//        produitRepository.deleteAllById(ids);
//    }
//
//    public void deleteAllProduits() {
//        produitRepository.deleteAll();
//    }

    private static final String BARCODE_DIR = "barcodes/"; // À adapter selon ton chemin réel

    public void deleteProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        deleteBarcodeFile(produit.getCodeBarre());

        produitRepository.deleteById(id);
    }

    public void deleteProduitsByIds(List<Long> ids) {
        List<Produit> produits = produitRepository.findAllById(ids);
        for (Produit p : produits) {
            deleteBarcodeFile(p.getCodeBarre());
        }
        produitRepository.deleteAllById(ids);
    }

    public void deleteAllProduits() {
        List<Produit> produits = produitRepository.findAll();
        for (Produit p : produits) {
            deleteBarcodeFile(p.getCodeBarre());
        }
        produitRepository.deleteAll();
    }

    private void deleteBarcodeFile(String codeBarre) {
        if (codeBarre != null && !codeBarre.isBlank()) {
            File file = new File(BARCODE_DIR + codeBarre + ".png");
            if (file.exists()) {
                file.delete();
            }
        }
    }


    private ProduitDto mapToDto(Produit p) {
        ProduitDto dto = new ProduitDto();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setRef(p.getRef());
        dto.setQte(p.getQte());
        dto.setPrix(p.getPrix());
        dto.setCodeBarre(p.getCodeBarre());
        dto.setQteMin(p.getQteMin());
        dto.setCategorieId(p.getCategorie()  != null ? p.getCategorie().getId() : null);
        dto.setEntrepotId(p.getEntrepot()    != null ? p.getEntrepot().getId()   : null);
        return dto;
    }




    private final Path dossierBarcodes = Paths.get("barcodes");

    private void generateBarcodeImage(String barcodeText, String filename) throws Exception {
        if (!Files.exists(dossierBarcodes)) {
            Files.createDirectories(dossierBarcodes);
        }

        Path outputPath = dossierBarcodes.resolve(filename);
        BitMatrix bitMatrix = new MultiFormatWriter()
                .encode(barcodeText, BarcodeFormat.CODE_128, 300, 100);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath);
    }

    public List<Produit> importerProduitsDepuisExcel(MultipartFile file) {
        List<Produit> produits = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            Map<String, Integer> colIndex = new HashMap<>();
            for (Cell c : header) {
                colIndex.put(formatter.formatCellValue(c).toLowerCase(), c.getColumnIndex());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Produit p = new Produit();
                p.setNom(formatter.formatCellValue(row.getCell(colIndex.get("nom"))));
                p.setRef(formatter.formatCellValue(row.getCell(colIndex.get("ref"))));
                p.setQte((int) row.getCell(colIndex.get("qte")).getNumericCellValue());
                p.setQteMin((int) row.getCell(colIndex.get("qtemin")).getNumericCellValue());
                p.setPrix(row.getCell(colIndex.get("prix")).getNumericCellValue());

//                if (colIndex.containsKey("categorieid")) {
//                    String catVal = formatter.formatCellValue(row.getCell(colIndex.get("categorieid")));
//                    if (!catVal.isBlank()) {
//                        Long catId = Long.valueOf(catVal);
//                        categorieRepository.findById(catId).ifPresent(p::setCategorie);
//                    }
//                }

                if (colIndex.containsKey("categorie")) {
                    String catNom = formatter.formatCellValue(row.getCell(colIndex.get("categorie")));
                    if (!catNom.isBlank()) {
                        categorieRepository.findByNomIgnoreCase(catNom.trim())
                                .ifPresent(p::setCategorie);
                    }
                }

//
//                if (colIndex.containsKey("entrepotid")) {
//                    String entVal = formatter.formatCellValue(row.getCell(colIndex.get("entrepotid")));
//                    if (!entVal.isBlank()) {
//                        Long entId = Long.valueOf(entVal);
//                        entrepotRepository.findById(entId).ifPresent(p::setEntrepot);
//                    }
//                }

                if (colIndex.containsKey("entrepôt")) { // attention à l’accent
                    String entNom = formatter.formatCellValue(row.getCell(colIndex.get("entrepôt")));
                    if (!entNom.isBlank()) {
                        entrepotRepository.findByNomIgnoreCase(entNom.trim())
                                .ifPresent(p::setEntrepot);
                    }
                }


                produits.add(p);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture Excel", e);
        }

        for (Produit produit : produits) {
            String code = "PROD-" + produit.getRef() + "-" + System.currentTimeMillis();
            produit.setCodeBarre(code);
            try {
                generateBarcodeImage(code, code + ".png");
            } catch (Exception ignored) { }
        }

        return produitRepository.saveAll(produits);
    }










}

