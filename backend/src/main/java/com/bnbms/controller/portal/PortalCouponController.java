package com.bnbms.controller.portal;

import com.bnbms.common.Constants;
import com.bnbms.common.Result;
import com.bnbms.model.vo.CouponCenterVO;
import com.bnbms.model.vo.UserCouponVO;
import com.bnbms.security.RequireRole;
import com.bnbms.security.UserContext;
import com.bnbms.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 租客优惠券：领券中心、领取、我的券、下单可用券试算
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_TENANT)
public class PortalCouponController {

    private final CouponService couponService;

    /** 领券中心 */
    @GetMapping("/center")
    public Result<List<CouponCenterVO>> center() {
        return Result.ok(couponService.center(UserContext.getUserId()));
    }

    /** 领取优惠券 */
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        couponService.receive(UserContext.getUserId(), id);
        return Result.ok("领取成功", null);
    }

    /** 我的优惠券：status = UNUSED / USED / EXPIRED，空为全部 */
    @GetMapping("/mine")
    public Result<List<UserCouponVO>> mine(@RequestParam(required = false) String status) {
        return Result.ok(couponService.mine(UserContext.getUserId(), status));
    }

    /** 预订页可用券试算 */
    @GetMapping("/usable")
    public Result<List<UserCouponVO>> usable(@RequestParam Long homestayId,
                                             @RequestParam LocalDate checkInDate,
                                             @RequestParam LocalDate checkOutDate) {
        return Result.ok(couponService.usable(
                UserContext.getUserId(), homestayId, checkInDate, checkOutDate));
    }
}
