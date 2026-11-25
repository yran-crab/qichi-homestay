package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.vo.UserVO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireRole(Constants.ROLE_ADMIN)
@RequestMapping("/api/admin/host-applies")
@RequiredArgsConstructor
public class AdminHostApplyController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Result<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long pageSize,
                                           @RequestParam(required = false) String realName) {
        return Result.ok(adminUserService.hostApplyPage(page, pageSize, realName));
    }

    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam boolean approved) {
        adminUserService.auditHost(id, approved);
        return Result.ok(approved ? "已通过房东入驻申请" : "已驳回该申请", null);
    }
}
