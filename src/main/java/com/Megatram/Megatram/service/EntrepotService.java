package com.Megatram.Megatram.service;


import com.Megatram.Megatram.Dto.EntrepotDto;
import com.Megatram.Megatram.Entity.Entrepot;
import com.Megatram.Megatram.repository.EntrepotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntrepotService {

    @Autowired
    private EntrepotRepository entrepotRepository;

    public Entrepot create(EntrepotDto dto) {
        Entrepot entrepot = new Entrepot();
        entrepot.setNom(dto.getNom());
        entrepot.setRef(dto.getRef());
        return entrepotRepository.save(entrepot);
    }

    public List<EntrepotDto> getAll() {
        return entrepotRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EntrepotDto getById(Long id) {
        Entrepot entrepot = entrepotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
        return mapToDto(entrepot);
    }

    public Entrepot update(Long id, EntrepotDto dto) {
        Entrepot entrepot = entrepotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));
        entrepot.setNom(dto.getNom());
        entrepot.setRef(dto.getRef());
        return entrepotRepository.save(entrepot);
    }

    public void delete(Long id) {
        entrepotRepository.deleteById(id);
    }

    public void deleteEntrepotsByIds(List<Long> ids) {
        entrepotRepository.deleteAllById(ids);
    }

    public void deleteAllEntrepots() {
        entrepotRepository.deleteAll();
    }

    private EntrepotDto mapToDto(Entrepot entrepot) {
        EntrepotDto dto = new EntrepotDto();
        dto.setId(entrepot.getId());
        dto.setNom(entrepot.getNom());
        dto.setRef(entrepot.getRef());
        dto.setQuantite(entrepot.getQuantite()); // appel au getter @Transient
        dto.setValeurVente(entrepot.getValeurVente());
        return dto;
    }
}
