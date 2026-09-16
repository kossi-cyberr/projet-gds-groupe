package com.example.versuion.repository;

import com.example.versuion.models.MvtStk;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MvtStkRepository extends JpaRepository<MvtStk, Long> {

    @Query("select sum(m.quantite) from MvtStk m where m.article.id = :idArticle")
    BigDecimal stockReelArticle(@Param("idArticle") Long idArticle);

    List<MvtStk> findAllByArticleId(Long idArticle);

    List<MvtStk> findAllByOrderByIdDesc();

    List<MvtStk> findAllByIdEntrepriseOrderByIdDesc(Integer idEntreprise);

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    @Query("select sum(m.quantite) from MvtStk m where m.article.id = :idArticle and m.idEntreprise = :idEntreprise")
    BigDecimal stockReelArticle(@Param("idArticle") Long idArticle, @Param("idEntreprise") Integer idEntreprise);

    List<MvtStk> findAllByArticleIdAndIdEntreprise(Long idArticle, Integer idEntreprise);

    default BigDecimal stockReelArticleTenant(Long idArticle) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? stockReelArticle(idArticle, idEntreprise) : stockReelArticle(idArticle);
    }

    default List<MvtStk> findAllByArticleIdTenant(Long idArticle) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByArticleIdAndIdEntreprise(idArticle, idEntreprise) : findAllByArticleId(idArticle);
    }

    default List<MvtStk> findAllTenantOrderByIdDesc() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntrepriseOrderByIdDesc(idEntreprise) : findAllByOrderByIdDesc();
    }
}
