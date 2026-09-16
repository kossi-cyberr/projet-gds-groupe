package com.example.versuion.controller;

import com.example.versuion.Dto.dashboard.DashboardDto;
import com.example.versuion.controller.api.DashboardApi;
import com.example.versuion.services.DashboardService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardControlleur implements DashboardApi {

    private final DashboardService dashboardService;

    public DashboardControlleur(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public DashboardDto getDashboard() {
        return dashboardService.getDashboard();
    }
}
