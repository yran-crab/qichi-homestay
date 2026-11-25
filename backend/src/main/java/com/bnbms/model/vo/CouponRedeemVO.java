package com.bnbms.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 下单时优惠券核销校验结果
 */
@Data
@AllArgsConstructor
public class CouponRedeemVO {

    /** 用户优惠券 ID */
    private Long userCouponId;
    /** 优惠券名称（写入订单快照） */
    private String couponName;
    /** 服务端计算的本单抵扣金额 */
    private BigDecimal discountAmount;
}
