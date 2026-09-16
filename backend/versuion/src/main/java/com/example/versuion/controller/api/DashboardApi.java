package com.example.versuion.controller.api;

import com.example.versuion.Dto.dashboard.DashboardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import static com.example.versuion.utiles.Constants.APP_ROOT;

@Tag(name = "Dashboard", description = "Statistiques et tableau de bord")
public interface DashboardApi {

    @GetMapping(APP_ROOT + "/dashboard")
    @Operation(summary = "Statistiques du tableau de bord",
            description = "KPIs (CA, compteurs, stock) et données de graphiques de l'entreprise courante")
    DashboardDto getDashboard();
}
