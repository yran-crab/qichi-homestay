package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.Result;
import com.bnbms.model.vo.AdminDashboardVO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<AdminDashboardVO> dashboard() {
        return Result.ok(dashboardService.adminDashboard());
    }
}
