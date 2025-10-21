package org.example.ootoutfitoftoday.domain.dashboard.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.admin.AdminDashboardQueryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/v1/dashboards/users")
public class AdminDashboardController {

    private final AdminDashboardQueryService adminDashboardQueryService;
}
