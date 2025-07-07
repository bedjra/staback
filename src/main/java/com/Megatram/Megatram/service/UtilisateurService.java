package com.Megatram.Megatram.service;



import com.Megatram.Megatram.Entity.Utilisateur;
import com.Megatram.Megatram.repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public String getRoleByEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur != null && utilisateur.getRole() != null) {
            return utilisateur.getRole().name();
        }
        return null;
    }


    public Utilisateur saveUtilisateur(Utilisateur utilisateur) {
        String rawPassword = utilisateur.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        utilisateur.setPassword(encodedPassword);
        return utilisateurRepository.save(utilisateur);
    }


    @Transactional
    public Utilisateur updateUtilisateur(Long id, Utilisateur updatedData) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l’ID: " + id));

        utilisateur.setEmail(updatedData.getEmail());

        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updatedData.getPassword());
            utilisateur.setPassword(encodedPassword);
        }

        utilisateur.setRole(updatedData.getRole());

        return utilisateurRepository.save(utilisateur);
    }


    public void deleteUtilisateur(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new EntityNotFoundException("Utilisateur non trouvé avec l’ID: " + id);
        }
        utilisateurRepository.deleteById(id);
    }


    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }


    public Utilisateur getUtilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String email = authentication.getName();

        return utilisateurRepository.findByEmail(email);
    }

    public Utilisateur findByEmailAndPassword(String email, String rawPassword) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur != null && passwordEncoder.matches(rawPassword, utilisateur.getPassword())) {
            return utilisateur;
        }
        return null;
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }


    public boolean existsByEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

}
