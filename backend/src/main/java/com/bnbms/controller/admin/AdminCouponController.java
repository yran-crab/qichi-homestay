package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.entity.Coupon;
import com.bnbms.model.dto.CouponQueryDTO;
import com.bnbms.model.dto.CouponSaveDTO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * 管理员优惠券管理
 */
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public Result<PageResult<Coupon>> page(@ModelAttribute CouponQueryDTO query) {
        return Result.ok(couponService.adminPage(query));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CouponSaveDTO dto) {
        couponService.adminCreate(dto);
        return Result.ok("创建成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CouponSaveDTO dto) {
        dto.setId(id);
        couponService.adminUpdate(dto);
        return Result.ok("更新成功", null);
    }

    /** 上架 / 下架 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        couponService.updateStatus(id, body.get("status"));
        return Result.ok("操作成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.adminDelete(id);
        return Result.ok("删除成功", null);
    }
}
