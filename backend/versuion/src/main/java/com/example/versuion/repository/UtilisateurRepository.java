package com.example.versuion.repository;

import com.example.versuion.models.Utilisateurs;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateurs, Long> {

    Optional<Utilisateurs> findByEmail(String email);

    Optional<Utilisateurs> findByEmailAndEntrepriseId(String email, Integer idEntreprise);

    // --- Méthodes multi-entreprise (filtrage par entreprise de l'utilisateur) ---
    Optional<Utilisateurs> findByIdAndEntrepriseId(Long id, Integer idEntreprise);

    List<Utilisateurs> findAllByEntrepriseId(Integer idEntreprise);

    default Optional<Utilisateurs> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndEntrepriseId(id, idEntreprise) : findById(id);
    }

    default List<Utilisateurs> findAllTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByEntrepriseId(idEntreprise) : findAll();
    }

    /**
     * Recherche par email restreinte a l'entreprise courante (anti-enumeration
     * inter-entreprises). Hors contexte authentifie (JWT sans entreprise), retombe
     * sur findByEmail pour l'authentification elle-meme.
     */
    default Optional<Utilisateurs> findByEmailTenant(String email) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByEmailAndEntrepriseId(email, idEntreprise) : findByEmail(email);
    }
}
