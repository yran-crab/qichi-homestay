package com.bnbms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 领券中心列表项：优惠券模板 + 当前用户领取状态
 */
@Data
public class CouponCenterVO {

    private Long id;
    private String name;
    private String type;
    private BigDecimal threshold;
    private BigDecimal discountAmount;
    private Integer discountPercent;
    private BigDecimal maxDiscountAmount;
    private Integer totalCount;
    private Integer receivedCount;
    private Integer perLimit;
    private String validType;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Integer validDays;

    /** 当前用户是否已领取 */
    private Boolean received;
    /** 是否已被领完（限量券库存为 0） */
    private Boolean soldOut;
}
