package com.Megatram.Megatram.Controller;

import com.Megatram.Megatram.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facture")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @GetMapping(value = "/{venteId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generer(@PathVariable Long venteId) {
        byte[] pdf = factureService.genererFacturePdf(venteId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=facture.pdf")
                .body(pdf);
    }
}
