package com.Megatram.Megatram.service;

import com.Megatram.Megatram.Dto.ClientDto;
import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    private ClientDto mapToDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setTel(client.getTel());
        return dto;
    }

    private Client mapToEntity(ClientDto dto) {
        Client client = new Client();
        client.setId(dto.getId());
        client.setNom(dto.getNom());
        client.setTel(dto.getTel());
        return client;
    }

    public ClientDto save(ClientDto dto) {
        Client saved = clientRepository.save(mapToEntity(dto));
        return mapToDto(saved);
    }

    public List<ClientDto> findAll() {
        return clientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ClientDto findById(Long id) {
        return clientRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    public void delete(Long id) {
        clientRepository.deleteById(id);
    }

    public ClientDto update(Long id, ClientDto dto) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        existing.setNom(dto.getNom());
        existing.setTel(dto.getTel());

        return mapToDto(clientRepository.save(existing));
    }}
