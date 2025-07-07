package com.Megatram.Megatram.Controller;


import com.Megatram.Megatram.Dto.VenteDto;
import com.Megatram.Megatram.Entity.Vente;
import com.Megatram.Megatram.service.VenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vente")
@CrossOrigin(origins = "http://localhost:3000")


@Tag(name = "Vente Controller", description = "Gestion des opérations de vente")
public class VenteController {

    @Autowired
    private VenteService venteService;

    @Operation(summary = "Créer une nouvelle vente")
    @PostMapping
    public ResponseEntity<Vente> creerVente(@RequestBody VenteDto venteDto) {
        Vente venteCree = venteService.creerVente(venteDto);
        return ResponseEntity.ok(venteCree);
    }

    @Operation(summary = "Récupérer toutes les ventes")
    @GetMapping
    public ResponseEntity<List<VenteDto>> getAllVentes() {
        return ResponseEntity.ok(venteService.getAllVenteDtos());
    }


    @Operation(summary = "Récupérer une vente par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<Vente> getVenteById(@PathVariable Long id) {
        return venteService.getVenteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @Operation(summary = "Mettre à jour une vente par son ID")
//    @PutMapping("/{id}")
//    public ResponseEntity<Vente> updateVente(@PathVariable Long id, @RequestBody VenteDto venteDto) {
//        try {
//            Vente venteModifiee = venteService.updateVente(id, venteDto);
//            return ResponseEntity.ok(venteModifiee);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    @Operation(summary = "Supprimer une vente par son ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVente(@PathVariable Long id) {
        try {
            venteService.deleteVente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }    }

}
