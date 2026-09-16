package com.example.versuion.repository;

import com.example.versuion.models.LigneComandeFournisseur;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LigneCommandeFournisseurRepository extends JpaRepository<LigneComandeFournisseur, Long> {

    List<LigneComandeFournisseur> findAllByCommandeFournisseurId(Long idCommande);

    List<LigneComandeFournisseur> findAllByArticleId(Long articleId);

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    Optional<LigneComandeFournisseur> findByIdAndIdEntreprise(Long id, Integer idEntreprise);

    List<LigneComandeFournisseur> findAllByCommandeFournisseurIdAndIdEntreprise(Long idCommande, Integer idEntreprise);

    List<LigneComandeFournisseur> findAllByArticleIdAndIdEntreprise(Long articleId, Integer idEntreprise);

    default Optional<LigneComandeFournisseur> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default List<LigneComandeFournisseur> findAllByCommandeFournisseurIdTenant(Long idCommande) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByCommandeFournisseurIdAndIdEntreprise(idCommande, idEntreprise) : findAllByCommandeFournisseurId(idCommande);
    }

    default List<LigneComandeFournisseur> findAllByArticleIdTenant(Long articleId) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByArticleIdAndIdEntreprise(articleId, idEntreprise) : findAllByArticleId(articleId);
    }
}
