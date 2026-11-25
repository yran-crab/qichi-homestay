package com.bnbms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预订确认：价格与档期试算结果
 */
@Data
public class BookingConfirmVO {

    private Long homestayId;
    private String homestayTitle;
    private String coverImg;
    private String city;
    private String address;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private Integer guestCount;
    private Integer maxGuests;
    private BigDecimal unitPrice;
    /** 订单原价总额（优惠前） */
    private BigDecimal totalAmount;
    /** 使用的用户优惠券 ID */
    private Long couponId;
    /** 优惠券名称 */
    private String couponName;
    /** 优惠券抵扣金额 */
    private BigDecimal discountAmount;
    /** 应付金额（优惠后） */
    private BigDecimal payAmount;
}
