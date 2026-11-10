package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.DashboardService;
import com.umc.pediupatrao.dto.DashboardResumoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    public DashboardResumoDTO obterResumo() {
        return dashboardService.obterResumo();
    }
}
