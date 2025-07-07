//package com.Megatram.Megatram.config;          // ↔ mieux de mettre dans un package config
//
//import com.Megatram.Megatram.Entity.Utilisateur;
//import com.Megatram.Megatram.enums.Role;
//import com.Megatram.Megatram.service.UtilisateurService;
//import jakarta.annotation.PostConstruct;
//import jakarta.validation.constraints.Email;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AdminInitializer {
//
//    @Autowired
//    private UtilisateurService utilisateurService;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @PostConstruct
//    public void init() {
//        @Email(message = "Email invalide")
//        String emailAdmin = "admin@gmail.com".trim().toLowerCase();
//
//        System.out.println("⏩ Tentative de création admin avec email = [" + emailAdmin + "]");
//
//        if (!utilisateurService.existsByEmail(emailAdmin)) {
//            Utilisateur admin = new Utilisateur();
//            admin.setEmail(emailAdmin);
//            admin.setPassword(passwordEncoder.encode("admin"));
//            admin.setRole(Role.ADMIN);
//
//            utilisateurService.saveUtilisateur(admin);
//            System.out.println("✅ Admin par défaut créé !");
//        } else {
//            System.out.println("ℹ️ Admin déjà existant, aucune action.");
//        }
//    }
//}
