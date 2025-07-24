package com.Megatram.Megatram.config;

import com.Megatram.Megatram.Entity.Client;
import com.Megatram.Megatram.Entity.Permission;
import com.Megatram.Megatram.Entity.Role;
import com.Megatram.Megatram.Entity.Utilisateur;
import com.Megatram.Megatram.enums.PermissionType;
import com.Megatram.Megatram.repository.ClientRepository;
import com.Megatram.Megatram.repository.PermissionRepository;
import com.Megatram.Megatram.repository.RoleRepository;
import com.Megatram.Megatram.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder,
                           ClientRepository clientRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // --- 1. Création des Rôles (si nécessaire) ---

        // A-ADMIN : Toutes les permissions
        Role adminRole = createRoleIfNotFound("ADMIN", Arrays.asList(PermissionType.values()));


        // B-BOUTIQUIER : Permissions pour les boutiques
        Role boutiquierRole = createRoleIfNotFound("BOUTIQUIER",
                List.of(
                        // --- Gestion des Produits ---
                        PermissionType.PRODUIT_READ,

                        // --- Gestion des Commandes ---
                        PermissionType.COMMANDE_CREATE,
                        PermissionType.COMMANDE_READ,

                        // --- Ventes ---
                        PermissionType.VENTE_READ
                )
        );

        // C-CONTROLEUR : Toutes les permissions (comme admin)
        Role controleurRole = createRoleIfNotFound("CONTROLEUR", Arrays.asList(PermissionType.values()));

        // D-SECRETAIRE : Permissions spécifiques
        Role secretaireRole = createRoleIfNotFound("SECRETARIAT",
                List.of(
                        // --- Produits ---
                        PermissionType.PRODUIT_READ,

                        // --- Commandes ---
                        PermissionType.COMMANDE_CREATE,
                        PermissionType.COMMANDE_READ,
                        PermissionType.COMMANDE_VALIDATE,
                        PermissionType.COMMANDE_CANCEL,

                        // --- Livraisons ---
                        PermissionType.LIVRAISON_GENERATE,
                        PermissionType.LIVRAISON_READ,
                        PermissionType.LIVRAISON_VALIDATE,

                        // --- Ventes & Factures ---
                        PermissionType.FACTURE_GENERATE,
                        PermissionType.VENTE_READ
                )
        );

        // E-MAGASINIER : Permissions pour la gestion des stocks
        Role magasinierRole = createRoleIfNotFound("MAGASINIER",
                List.of(
                        // --- Produits ---
                        PermissionType.PRODUIT_READ,

                        // --- Livraisons ---
                        PermissionType.LIVRAISON_READ,
                        PermissionType.LIVRAISON_VALIDATE
                )
        );

        // --- 2. Création des Utilisateurs et Clients (si nécessaire) ---
        String defaultPassword = "123";
        createUserAndClient("admin@megatram.biz", defaultPassword, adminRole);
//        createUserAndClient("secretaire@megatram.biz", defaultPassword, secretaireRole);
//        createUserAndClient("magasinier@megatram.biz", defaultPassword, magasinierRole);
//        createUserAndClient("boutiquier@megatram.biz", defaultPassword, boutiquierRole);
//        createUserAndClient("controleur@megatram.biz", defaultPassword, controleurRole);
    }

    /**
     * Crée un rôle et ses permissions SEULEMENT s'il n'existe pas déjà.
     */
    private Role createRoleIfNotFound(String roleName, List<PermissionType> permissions) {
        Optional<Role> roleOpt = roleRepository.findByNom(roleName);

        if (roleOpt.isPresent()) {
            // Le rôle existe déjà, on le retourne sans modification
            return roleOpt.get();
        } else {
            // Créer un nouveau rôle avec toutes ses permissions
            Role role = new Role();
            role.setNom(roleName);
            role = roleRepository.save(role);

            // Ajouter toutes les permissions
            addPermissionsToRole(role, permissions);

            return role;
        }
    }

    /**
     * Ajoute toutes les permissions à un nouveau rôle.
     */
    private void addPermissionsToRole(Role role, List<PermissionType> permissions) {
        for (PermissionType permType : permissions) {
            Permission permission = new Permission();
            permission.setAction(permType.name());
            permission.setAutorise(true);
            permission.setRole(role);
            permissionRepository.save(permission);
        }
    }

    /**
     * Crée un utilisateur ET un client associé s'ils n'existent pas.
     */
    private void createUserAndClient(String email, String password, Role role) {
        // Créer l'utilisateur s'il n'existe pas
        if (!utilisateurRepository.existsByEmail(email)) {
            Utilisateur user = new Utilisateur();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            utilisateurRepository.save(user);

            System.out.println("Utilisateur créé : " + email + " avec le rôle : " + role.getNom());
        }

        // Créer le client s'il n'existe pas
        if (!clientRepository.existsByNom(email)) {
            Client clientInterne = new Client();
            clientInterne.setNom(email);
            clientInterne.setTel("00000000");
            clientRepository.save(clientInterne);

            System.out.println("Client créé : " + email);
        }
    }
}