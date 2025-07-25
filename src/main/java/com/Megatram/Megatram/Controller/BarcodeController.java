package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.Dto.BarcodePrintRequestDto;
import com.Megatram.Megatram.Entity.Produit;
import com.Megatram.Megatram.repository.ProduitRepos;
import com.Megatram.Megatram.service.BarcodeService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource; // ✅ Bon import
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/barcode")
@Tag(name = "Barcode", description = "Gestion des Barcodes")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN') ")

public class BarcodeController {

    private final ProduitRepos produitRepository;
    private final BarcodeService barcodeService;

    private final Path dossierBarcodes = Paths.get("barcodes");


    public BarcodeController(ProduitRepos produitRepository, BarcodeService barcodeService) {
        this.produitRepository = produitRepository;
        this.barcodeService = barcodeService;
    }


    @Operation(summary = "Récupérer l'image du code-barres d'un produit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image trouvée et retournée"),
            @ApiResponse(responseCode = "404", description = "Produit ou image du code-barres non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/{produitId}")
    public ResponseEntity<Resource> getBarcodeImage(@PathVariable Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + produitId));

        String codeBarre = produit.getCodeBarre();
        if (codeBarre == null || codeBarre.isBlank()) {
            throw new RuntimeException("Pas de code-barres défini pour ce produit.");
        }

        Path imagePath = dossierBarcodes.resolve(codeBarre + ".png");

        if (!Files.exists(imagePath)) {
            throw new RuntimeException("Image du code-barres introuvable.");
        }

        try {
            Resource file = new UrlResource(imagePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(file);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur de chargement de l'image du code-barres.", e);
        }
    }

    @Operation(summary = "Imprimer un PDF contenant les codes-barres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'impression des codes-barres")
    })
    @PostMapping(value = "/print", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> imprimerBarcodes(@RequestBody BarcodePrintRequestDto requestDto) {
        List<Produit> produits = produitRepository.findByNom(requestDto.getProduitNom());

        if (produits == null || produits.isEmpty()) {
            throw new RuntimeException("Aucun produit trouvé avec le nom : " + requestDto.getProduitNom());
        }

        Produit produit = produits.get(0); // ✅ On prend simplement le premier

        try {
            Resource pdfFile = genererPdfBarcodes(produit, requestDto.getQuantite());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfFile);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'impression des codes-barres", e);
        }
    }



//
//    private Resource genererPdfBarcodes(Produit produit, int quantite) throws Exception {
//    String codeBarre = produit.getCodeBarre();
//
//    if (codeBarre == null || codeBarre.isBlank()) {
//        throw new RuntimeException("Ce produit n’a pas de code-barres.");
//    }
//
//    Path barcodePath = dossierBarcodes.resolve(codeBarre + ".png");
//    if (!Files.exists(barcodePath)) {
//        throw new RuntimeException("Image du code-barres introuvable.");
//    }
//
//    Path pdfPath = dossierBarcodes.resolve("print_" + produit.getId() + ".pdf");
//
//    Document document = new Document(PageSize.A4, 36, 35, 15, 5);
//    PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
//    document.open();
//
//    int columns = 3;
//    int perPage = 21;
//    int count = 0;
//
//    PdfPTable table = createNewTable(columns);
//    Font petitePolice = new Font(Font.FontFamily.HELVETICA, 6);
//
//    for (int i = 0; i < quantite; i++) {
//        // ✅ On recrée une nouvelle image à chaque itération
//        Image barcodeImage = Image.getInstance(barcodePath.toAbsolutePath().toString());
//        barcodeImage.scaleToFit(180f, 60f); // pour un bon équilibre taille/lecture
//
//        PdfPTable innerTable = new PdfPTable(1);
//        innerTable.setWidthPercentage(100);
//
//        PdfPCell imageCell = new PdfPCell(barcodeImage, true);
//        imageCell.setBorder(Rectangle.NO_BORDER);
//        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//
//        Phrase phraseNom = new Phrase(produit.getNom() + " - " + (i + 1), petitePolice);
//        Phrase phrasePrix = new Phrase(String.format("%.2f €", produit.getPrix()), petitePolice);
//
//        Paragraph paraNom = new Paragraph(phraseNom);
//        paraNom.setAlignment(Element.ALIGN_CENTER);
//
//        Paragraph paraPrix = new Paragraph(phrasePrix);
//        paraPrix.setAlignment(Element.ALIGN_CENTER);
//
//        PdfPCell nameCell = new PdfPCell();
//        nameCell.setBorder(Rectangle.NO_BORDER);
//        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//        nameCell.addElement(paraNom);
//        nameCell.addElement(paraPrix);
//
//        innerTable.addCell(imageCell);
//        innerTable.addCell(nameCell);
//
//        PdfPCell outerCell = new PdfPCell(innerTable);
//        outerCell.setFixedHeight(120f); // Augmenté
//        outerCell.setBorder(Rectangle.NO_BORDER);
//        outerCell.setPadding(5f);
//
//        table.addCell(outerCell);
//        count++;
//
//        if (count % perPage == 0) {
//            document.add(table);
//            document.newPage();
//            table = createNewTable(columns);
//        }
//    }
//
//    if (count % perPage != 0) {
//        int reste = perPage - (count % perPage);
//        for (int i = 0; i < reste; i++) {
//            PdfPCell empty = new PdfPCell();
//            empty.setFixedHeight(140f);
//            empty.setBorder(Rectangle.NO_BORDER);
//            table.addCell(empty);
//        }
//        document.add(table);
//    }
//
//    document.close();
//
//    return new FileSystemResource(pdfPath.toFile());
//}

    private Resource genererPdfBarcodes(Produit produit, int quantite) throws Exception {
        String codeBarre = produit.getCodeBarre();
        if (codeBarre == null || codeBarre.isBlank()) {
            throw new RuntimeException("Ce produit n'a pas de code-barres.");
        }

        Path barcodePath = dossierBarcodes.resolve(codeBarre + ".png");
        if (!Files.exists(barcodePath)) {
            throw new RuntimeException("Image du code-barres introuvable.");
        }

        Path pdfPath = dossierBarcodes.resolve("print_" + produit.getId() + ".pdf");

        // ✅ Marges symétriques pour centrage parfait
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, Files.newOutputStream(pdfPath));
        document.open();

        final int COLUMNS = 3;
        final int PER_PAGE = 18;
        final float CELL_HEIGHT = 120f;
        final float CELL_PADDING = 5f;

        int count = 0;
        PdfPTable mainTable = createCenteredTable(COLUMNS);
        Font petitePolice = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL);

        for (int i = 0; i < quantite; i++) {
            // Créer le contenu de chaque code-barres
            PdfPCell barcodeCell = createBarcodeCell(barcodePath, produit, petitePolice, CELL_HEIGHT, CELL_PADDING);
            mainTable.addCell(barcodeCell);
            count++;

            // Nouvelle page si nécessaire (mais pas après le dernier élément)
            if (count % PER_PAGE == 0 && i < quantite - 1) {
                document.add(mainTable);
                document.newPage();
                mainTable = createCenteredTable(COLUMNS);
            }
        }

        // Remplir les cellules vides restantes pour maintenir l'alignement
        if (count % PER_PAGE != 0) {
            int cellulesRestantes = PER_PAGE - (count % PER_PAGE);
            for (int i = 0; i < cellulesRestantes; i++) {
                PdfPCell emptyCell = createEmptyCell(CELL_HEIGHT);
                mainTable.addCell(emptyCell);
            }
        }

        // Ajouter la table finale avec centrage vertical
        addCenteredTable(document, mainTable);

        document.close();
        return new FileSystemResource(pdfPath.toFile());
    }

    /**
     * Crée une table parfaitement centrée
     */
    private PdfPTable createCenteredTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(95); // ✅ Légèrement réduit pour plus de centrage
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(0f);
        table.setSpacingAfter(0f);

        // Configuration par défaut des cellules
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        return table;
    }

    /**
     * Crée une cellule contenant un code-barres avec son nom
     */
    private PdfPCell createBarcodeCell(Path barcodePath, Produit produit, Font font,
                                       float cellHeight, float padding) throws Exception {

        // Table interne pour organiser image + texte
        PdfPTable innerTable = new PdfPTable(1);
        innerTable.setWidthPercentage(100);
        innerTable.setSpacingBefore(0f);
        innerTable.setSpacingAfter(0f);

        // Image du code-barres
        Image barcodeImage = Image.getInstance(barcodePath.toAbsolutePath().toString());
        barcodeImage.scaleToFit(110f, 35f);
        barcodeImage.setAlignment(Element.ALIGN_CENTER);

        PdfPCell imageCell = new PdfPCell(barcodeImage, true);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imageCell.setPadding(2f);

        // Nom du produit
        Paragraph nomProduit = new Paragraph(produit.getNom(), font);
        nomProduit.setAlignment(Element.ALIGN_CENTER);
        nomProduit.setSpacingBefore(3f);
        nomProduit.setSpacingAfter(0f);

        PdfPCell textCell = new PdfPCell();
        textCell.addElement(nomProduit);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        textCell.setVerticalAlignment(Element.ALIGN_TOP);
        textCell.setPadding(2f);

        innerTable.addCell(imageCell);
        innerTable.addCell(textCell);

        // Cellule conteneur
        PdfPCell outerCell = new PdfPCell(innerTable);
        outerCell.setFixedHeight(cellHeight);
        outerCell.setBorder(Rectangle.NO_BORDER);
        outerCell.setPadding(padding);
        outerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        outerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return outerCell;
    }

    /**
     * Crée une cellule vide avec la même hauteur
     */
    private PdfPCell createEmptyCell(float cellHeight) {
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setFixedHeight(cellHeight);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setPadding(0f);
        return emptyCell;
    }

    /**
     * Ajoute la table au document avec centrage vertical optimal
     */
    private void addCenteredTable(Document document, PdfPTable table) throws DocumentException {
        // Calculer l'espace disponible et centrer verticalement
        float pageHeight = document.getPageSize().getHeight();
        float margins = document.topMargin() + document.bottomMargin();
        float availableHeight = pageHeight - margins;

        // Estimation de la hauteur de la table
        float tableHeight = table.getTotalHeight();

        // Si la table est plus petite que la page, ajouter un espacement pour centrer
        if (tableHeight < availableHeight) {
            float spacingBefore = (availableHeight - tableHeight) / 2;
            table.setSpacingBefore(spacingBefore);
        }

        document.add(table);
    }

//   //  Méthode utilitaire pour créer une nouvelle table bien configurée
//    private PdfPTable createNewTable(int columns) {
//        PdfPTable table = new PdfPTable(columns);
//        table.setWidthPercentage(100);
//        table.setSpacingBefore(05f);
//        table.setSpacingAfter(10f);
//        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
//        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
//        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
//        return table;
//    }






}
