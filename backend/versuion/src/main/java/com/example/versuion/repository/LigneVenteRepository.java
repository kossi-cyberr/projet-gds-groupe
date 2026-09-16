package com.example.versuion.repository;

import com.example.versuion.Dto.dashboard.TopArticleDto;
import com.example.versuion.Dto.dashboard.VentesParCategorieDto;
import com.example.versuion.Dto.dashboard.VentesParMoisDto;
import com.example.versuion.models.LigneVente;
import com.example.versuion.utiles.CurrentEntreprise;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    List<LigneVente> findAllByArticleId(Long articleId);

    List<LigneVente> findAllByVenteId(Long id);

    // --- Requêtes d'agrégation pour le tableau de bord ---
    @Query("select coalesce(sum(l.prixUnitare * l.quantie), 0) from LigneVente l where l.idEntreprise = :idEntreprise")
    BigDecimal chiffreAffairesTotal(@Param("idEntreprise") Integer idEntreprise);

    @Query("select coalesce(sum(l.prixUnitare * l.quantie), 0) from LigneVente l " +
            "where l.idEntreprise = :idEntreprise and year(l.vente.dateVente) = :annee and month(l.vente.dateVente) = :mois")
    BigDecimal chiffreAffairesMois(@Param("idEntreprise") Integer idEntreprise,
                                   @Param("annee") int annee, @Param("mois") int mois);

    @Query("select coalesce(sum(l.prixUnitare * l.quantie), 0) from LigneVente l " +
            "where l.idEntreprise = :idEntreprise and year(l.vente.dateVente) = :annee " +
            "and month(l.vente.dateVente) = :mois and day(l.vente.dateVente) = :jour")
    BigDecimal chiffreAffairesJour(@Param("idEntreprise") Integer idEntreprise,
                                   @Param("annee") int annee, @Param("mois") int mois, @Param("jour") int jour);

    @Query("select new com.example.versuion.Dto.dashboard.VentesParMoisDto(" +
            "year(l.vente.dateVente), month(l.vente.dateVente), coalesce(sum(l.prixUnitare * l.quantie), 0)) " +
            "from LigneVente l where l.idEntreprise = :idEntreprise " +
            "group by year(l.vente.dateVente), month(l.vente.dateVente) " +
            "order by year(l.vente.dateVente), month(l.vente.dateVente)")
    List<VentesParMoisDto> ventesParMois(@Param("idEntreprise") Integer idEntreprise);

    @Query("select new com.example.versuion.Dto.dashboard.VentesParCategorieDto(" +
            "c.id, c.designation, coalesce(sum(l.prixUnitare * l.quantie), 0)) " +
            "from LigneVente l join l.article a join a.category c " +
            "where l.idEntreprise = :idEntreprise " +
            "group by c.id, c.designation " +
            "order by coalesce(sum(l.prixUnitare * l.quantie), 0) desc")
    List<VentesParCategorieDto> ventesParCategorie(@Param("idEntreprise") Integer idEntreprise);

    @Query("select new com.example.versuion.Dto.dashboard.TopArticleDto(" +
            "a.id, a.designation, a.codeArticle, coalesce(sum(l.quantie), 0), coalesce(sum(l.prixUnitare * l.quantie), 0)) " +
            "from LigneVente l join l.article a " +
            "where l.idEntreprise = :idEntreprise " +
            "group by a.id, a.designation, a.codeArticle " +
            "order by coalesce(sum(l.quantie), 0) desc")
    List<TopArticleDto> topArticles(@Param("idEntreprise") Integer idEntreprise, Pageable pageable);

    // --- Méthodes multi-entreprise (filtrage par idEntreprise) ---
    List<LigneVente> findAllByArticleIdAndIdEntreprise(Long articleId, Integer idEntreprise);

    List<LigneVente> findAllByVenteIdAndIdEntreprise(Long id, Integer idEntreprise);

    default List<LigneVente> findAllByArticleIdTenant(Long articleId) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByArticleIdAndIdEntreprise(articleId, idEntreprise) : findAllByArticleId(articleId);
    }

    default List<LigneVente> findAllByVenteIdTenant(Long id) {
        Integer idEntreprise = CurrentEntreprise.getId();
        return idEntreprise != null ? findAllByVenteIdAndIdEntreprise(id, idEntreprise) : findAllByVenteId(id);
    }
}
