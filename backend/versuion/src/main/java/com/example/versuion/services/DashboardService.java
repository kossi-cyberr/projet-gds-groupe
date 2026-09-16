package com.example.versuion.services;

import com.example.versuion.Dto.dashboard.DashboardDto;

public interface DashboardService {

    /**
     * Agrège toutes les statistiques de l'entreprise courante
     * (KPI + données de graphiques).
     */
    DashboardDto getDashboard();
}
