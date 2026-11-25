package com.bnbms.service;

import com.bnbms.common.PageResult;
import com.bnbms.entity.Coupon;
import com.bnbms.model.dto.CouponQueryDTO;
import com.bnbms.model.dto.CouponSaveDTO;
import com.bnbms.model.vo.CouponCenterVO;
import com.bnbms.model.vo.CouponRedeemVO;
import com.bnbms.model.vo.UserCouponVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 优惠券服务：模板管理、领取、查询、下单核销与退券
 */
public interface CouponService {

    /* ---------- 管理端 ---------- */

    PageResult<Coupon> adminPage(CouponQueryDTO query);

    void adminCreate(CouponSaveDTO dto);

    void adminUpdate(CouponSaveDTO dto);

    void updateStatus(Long id, Integer status);

    void adminDelete(Long id);

    /* ---------- 租客端 ---------- */

    /** 领券中心：上架中、在领取有效期内的券，附带当前用户是否已领 */
    List<CouponCenterVO> center(Long userId);

    /** 领取优惠券 */
    void receive(Long userId, Long couponId);

    /** 我的优惠券：UNUSED / USED / EXPIRED，空为全部 */
    List<UserCouponVO> mine(Long userId, String status);

    /** 预订页：当前房源 + 日期下可用券试算（含不可用原因） */
    List<UserCouponVO> usable(Long userId, Long homestayId, LocalDate checkIn, LocalDate checkOut);

    /* ---------- 下单闭环（供 BookingService 调用） ---------- */

    /**
     * 校验用户优惠券并计算本单抵扣（不改状态）。
     * 服务端是金额计算的唯一可信来源。
     */
    CouponRedeemVO redeem(Long userCouponId, Long userId, BigDecimal totalAmount);

    /** 订单落库后核销优惠券（原子操作，失败抛异常回滚订单事务） */
    void bindUsed(Long userCouponId, Long userId, Long orderId);

    /** 订单取消（待支付）时退回优惠券 */
    void refundByOrder(Long orderId);

    /** 按券规则计算优惠金额 */
    BigDecimal calcDiscount(Coupon coupon, BigDecimal totalAmount);
}
