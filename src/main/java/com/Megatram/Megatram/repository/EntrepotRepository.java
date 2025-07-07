package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Categorie;
import com.Megatram.Megatram.Entity.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntrepotRepository extends JpaRepository<Entrepot, Long> {
    Entrepot findByNom(String nom);
    Optional<Entrepot> findByNomIgnoreCase(String nom);

}
