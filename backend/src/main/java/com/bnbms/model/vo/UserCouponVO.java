package com.bnbms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的优惠券 / 下单可用券视图
 */
@Data
public class UserCouponVO {

    /** 用户优惠券 ID（下单时传此值） */
    private Long id;
    private Long couponId;
    private String name;
    /** CASH / FULL_REDUCE / DISCOUNT */
    private String type;
    private BigDecimal threshold;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private BigDecimal maxDiscountAmount;

    /** 展示状态：UNUSED 未使用 / USED 已使用 / EXPIRED 已过期（过期动态计算） */
    private String status;
    private Long orderId;
    private LocalDateTime receiveTime;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private LocalDateTime usedTime;

    /** 下单可用券试算：本单使用该券可抵扣金额（仅 /coupons/usable 返回） */
    private BigDecimal currentDiscount;
    /** 下单可用券试算：抵扣后应付金额（仅 /coupons/usable 返回） */
    private BigDecimal currentPayAmount;
    /** 是否满足本单使用门槛（仅 /coupons/usable 返回） */
    private Boolean usable;
    /** 不可用原因（门槛不足时） */
    private String unusableReason;
}
