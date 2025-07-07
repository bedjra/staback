package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Entity.LigneVente;
import com.Megatram.Megatram.Entity.Organisation;
import com.Megatram.Megatram.Entity.Vente;
import com.Megatram.Megatram.repository.OrganisationRepository;
import com.Megatram.Megatram.repository.VenteRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Date;

@Service
public class FactureService {

    @Autowired
    private VenteRepository venteRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    public byte[] genererFacturePdf(Long venteId) {
        Vente vente = venteRepository.findById(venteId)
                .orElseThrow(() -> new RuntimeException("Vente non trouvée"));

        Organisation orga = organisationRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // LOGO
        try {
            Image logo = Image.getInstance("chemin/vers/logo.png");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_LEFT);
            document.add(logo);
        } catch (Exception e) {
            // rien si logo absent
        }

        // TITRE FACTURE
        Font fontTitre = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph titre = new Paragraph("FACTURE", fontTitre);
        titre.setAlignment(Element.ALIGN_RIGHT);
        document.add(titre);

        // INFOS ENTREPRISE
        document.add(new Paragraph(orga.getNom()));
        document.add(new Paragraph(orga.getAdresse()));
        document.add(new Paragraph(orga.getVille()));
        document.add(new Paragraph("Tel : " + orga.getTelephone()));
        document.add(new Paragraph("Email : " + orga.getEmail()));
        document.add(new Paragraph("Date : " + new Date()));
        document.add(new Paragraph(" "));

        // INFOS CLIENT
        document.add(new Paragraph("Client : " + vente.getClient().getNom()));
        document.add(new Paragraph("Téléphone : " + vente.getClient().getTel()));
        document.add(new Paragraph(" "));

        // TABLE PRODUITS
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{40, 20, 20, 20});

        table.addCell("Produit");
        table.addCell("Quantité");
        table.addCell("Prix Unitaire");
        table.addCell("Total");

        double totalFacture = 0;

        for (LigneVente ligne : vente.getLignes()) {
            Long produit = ligne.getProduitId();
            int qte = ligne.getQteVendu();
            double prixU = ligne.getProduitPrix();
            double totalLigne = qte * prixU;

            table.addCell(String.valueOf(qte));
            table.addCell(prixU + " FCFA");
            table.addCell(totalLigne + " FCFA");

            totalFacture += totalLigne;
        }

        document.add(table);

        // TOTAL GÉNÉRAL
        Paragraph total = new Paragraph("\nTOTAL : " + totalFacture + " FCFA", fontTitre);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        document.add(new Paragraph("\nMerci de votre confiance", new Font(Font.HELVETICA, 10)));
        document.close();

        return baos.toByteArray();
    }
}
