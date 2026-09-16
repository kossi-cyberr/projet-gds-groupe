package com.example.versuion.repository;

import com.example.versuion.models.Article;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByCodeArticle(String codeArticle);

    List<Article> findAllByCategoryId(Long idCategory);

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    Optional<Article> findByIdAndIdEntreprise(Long id, Integer idEntreprise);

    Optional<Article> findByCodeArticleAndIdEntreprise(String codeArticle, Integer idEntreprise);

    List<Article> findAllByIdEntreprise(Integer idEntreprise);

    List<Article> findAllByCategoryIdAndIdEntreprise(Long idCategory, Integer idEntreprise);

    // --- Pagination + recherche ---
    Page<Article> findAllByIdEntreprise(Integer idEntreprise, Pageable pageable);

    @Query("select a from Article a where a.idEntreprise = :idEntreprise " +
            "and (lower(a.designation) like lower(concat('%', :search, '%')) " +
            "or lower(a.codeArticle) like lower(concat('%', :search, '%')))")
    Page<Article> searchByIdEntreprise(@Param("idEntreprise") Integer idEntreprise, @Param("search") String search, Pageable pageable);

    @Query("select a from Article a where " +
            "lower(a.designation) like lower(concat('%', :search, '%')) " +
            "or lower(a.codeArticle) like lower(concat('%', :search, '%'))")
    Page<Article> search(@Param("search") String search, Pageable pageable);

    long countByIdEntreprise(Integer idEntreprise);

    // --- Alertes de stock ---
    @Query("select a from Article a where a.idEntreprise = :idEntreprise and a.seuilAlerte is not null and " +
            "(select coalesce(sum(m.quantite), 0) from MvtStk m where m.article.id = a.id and m.idEntreprise = :idEntreprise) < a.seuilAlerte " +
            "order by a.designation asc")
    List<Article> findArticlesSousSeuil(@Param("idEntreprise") Integer idEntreprise);

    @Query("select count(a) from Article a where a.idEntreprise = :idEntreprise and a.seuilAlerte is not null and " +
            "(select coalesce(sum(m.quantite), 0) from MvtStk m where m.article.id = a.id and m.idEntreprise = :idEntreprise) < a.seuilAlerte")
    long countArticlesSousSeuil(@Param("idEntreprise") Integer idEntreprise);

    @Query("select coalesce(sum(a.prixUnitaireTTc * " +
            "(select coalesce(sum(m.quantite), 0) from MvtStk m where m.article.id = a.id and m.idEntreprise = :idEntreprise)), 0) " +
            "from Article a where a.idEntreprise = :idEntreprise")
    BigDecimal valeurStock(@Param("idEntreprise") Integer idEntreprise);

    @Query("select coalesce(avg(a.prixUnitaireTTc - a.prixUnitaire), 0) from Article a where a.idEntreprise = :idEntreprise")
    BigDecimal margeMoyenne(@Param("idEntreprise") Integer idEntreprise);

    default Optional<Article> findByIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByIdAndIdEntreprise(id, idEntreprise) : findById(id);
    }

    default Optional<Article> findByCodeArticleTenant(String codeArticle) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findByCodeArticleAndIdEntreprise(codeArticle, idEntreprise) : findByCodeArticle(codeArticle);
    }

    default List<Article> findAllTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise) : findAll();
    }

    default List<Article> findAllByCategoryIdTenant(Long idCategory) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByCategoryIdAndIdEntreprise(idCategory, idEntreprise) : findAllByCategoryId(idCategory);
    }

    default Page<Article> findAllTenant(Pageable pageable) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByIdEntreprise(idEntreprise, pageable) : findAll(pageable);
    }

    default Page<Article> findAllTenant(String search, Pageable pageable) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null
                ? searchByIdEntreprise(idEntreprise, search, pageable)
                : search(search, pageable);
    }

    default List<Article> findArticlesSousSeuilTenant() {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findArticlesSousSeuil(idEntreprise) : List.of();
    }
}
