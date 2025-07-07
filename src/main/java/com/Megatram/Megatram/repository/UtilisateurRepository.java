package com.Megatram.Megatram.repository;

import com.Megatram.Megatram.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Utilisateur findByEmail(String email);


    Utilisateur findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

}

