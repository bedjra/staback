package com.Megatram.Megatram.Controller;


import com.Megatram.Megatram.Dto.EntrepotDto;
import com.Megatram.Megatram.Entity.Entrepot;
import com.Megatram.Megatram.service.EntrepotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entrepot")
@Tag(name = "Entrepot", description = "Gestion des entrepot")
@CrossOrigin(origins = "http://localhost:3000")

public class EntrepotController {

    @Autowired
    private EntrepotService entrepotService;

    @Operation(summary = "all ")
    @GetMapping
    public List<EntrepotDto> getAll() {
        return entrepotService.getAll();
    }

    @Operation(summary = "get by id ")
    @GetMapping("/{id}")
    public ResponseEntity<EntrepotDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entrepotService.getById(id));
    }

    @Operation(summary = "add ")
    @PostMapping
    public ResponseEntity<EntrepotDto> create(@RequestBody EntrepotDto dto) {
        Entrepot saved = entrepotService.create(dto);
        return ResponseEntity.status(201).body(entrepotService.getById(saved.getId()));
    }

    @Operation(summary = "put ")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EntrepotDto> update(@PathVariable Long id, @RequestBody EntrepotDto dto) {
        entrepotService.update(id, dto);
        return ResponseEntity.ok(entrepotService.getById(id));
    }

    @Operation(summary = "delete ")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        entrepotService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer plusieurs entrepôts ou tous")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<Void> deleteByIdsOrAll(@RequestBody List<Long> ids) {
        if (ids == null) {
            return ResponseEntity.badRequest().build();
        }
        if (ids.isEmpty()) {
            entrepotService.deleteAllEntrepots();
        } else {
            entrepotService.deleteEntrepotsByIds(ids);
        }
        return ResponseEntity.noContent().build();
    }
}