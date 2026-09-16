package com.example.versuion.repository;

import com.example.versuion.models.LigneComandeClient;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LigneCommandeClientRepository extends JpaRepository<LigneComandeClient, Long> {

    List<LigneComandeClient> findAllByCommandeClientId(Long id);

    List<LigneComandeClient> findAllByArticleId(Long articleId);

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    Optional<LigneComandeClient> findByIdAndIdEntreprise(Long id, Integer idEntreprise);

    List<LigneComandeClient> findAllByCommandeClientIdAndIdEntreprise(Long id, Integer idEntreprise);

    List<LigneComandeClient> findAllByArticleIdAndIdEntreprise(Long articleId, Integer idEntreprise);

    default Optional<LigneComandeClient> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default List<LigneComandeClient> findAllByCommandeClientIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByCommandeClientIdAndIdEntreprise(id, idEntreprise) : findAllByCommandeClientId(id);
    }

    default List<LigneComandeClient> findAllByArticleIdTenant(Long articleId) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByArticleIdAndIdEntreprise(articleId, idEntreprise) : findAllByArticleId(articleId);
    }
}
