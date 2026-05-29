package com.example.education.system.dashboard.controller;

import com.example.education.system.dashboard.dto.DashboardStatsResponse;
import com.example.education.system.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsResponse getStats() {
        return dashboardService.getStats();
    }
}
