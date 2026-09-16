package com.example.versuion.Dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Agrégation de toutes les statistiques affichées sur le tableau de bord.
 */
@Data
@Builder
public class DashboardDto {

    // --- KPI chiffre d'affaires ---
    private BigDecimal chiffreAffairesTotal;

    private BigDecimal chiffreAffairesMoisEnCours;

    private BigDecimal chiffreAffairesJourEnCours;

    // --- KPI compteurs ---
    private long nombreClients;

    private long nombreFournisseurs;

    private long nombreArticles;

    private long nombreCommandesClient;

    private long nombreCommandesFournisseur;

    private long nombreVentes;

    // --- KPI stock ---
    private long stockSousSeuil;

    private BigDecimal valeurStock;

    private BigDecimal margeMoyenne;

    // --- Graphiques ---
    private List<TopArticleDto> topArticles;

    private List<VentesParMoisDto> ventesParMois;

    private List<VentesParCategorieDto> ventesParCategorie;

    private List<CommandesParClientDto> commandesParClient;
}
