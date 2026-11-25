package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.entity.Homestay;
import com.bnbms.model.dto.HomestayAuditDTO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 管理员房源审核与监管
 */
@RestController
@RequestMapping("/api/admin/homestays")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminHomestayController {

    private final HomestayService homestayService;

    @GetMapping
    public Result<PageResult<Homestay>> page(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String city,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(homestayService.adminPage(status, city, keyword, page, pageSize));
    }

    @GetMapping("/audits")
    public Result<PageResult<Homestay>> auditPage(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(homestayService.auditPage(page, pageSize));
    }

    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody HomestayAuditDTO dto) {
        homestayService.audit(id, dto.getApproved(), dto.getRemark());
        return Result.ok(dto.getApproved() ? "已通过审核" : "已驳回", null);
    }

    @PutMapping("/{id}/offline")
    public Result<Void> forceOffline(@PathVariable Long id) {
        homestayService.forceOffline(id);
        return Result.ok("已强制下架", null);
    }
}
