package com.bnbms.controller.host;

import com.bnbms.common.Constants;
import com.bnbms.common.Result;
import com.bnbms.model.vo.HostDashboardVO;
import com.bnbms.security.RequireRole;
import com.bnbms.security.UserContext;
import com.bnbms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/host/dashboard")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_HOST)
public class HostDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<HostDashboardVO> dashboard() {
        return Result.ok(dashboardService.hostDashboard(UserContext.getUserId()));
    }
}
