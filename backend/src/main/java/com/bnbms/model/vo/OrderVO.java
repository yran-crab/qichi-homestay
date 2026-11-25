package com.bnbms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单列表/详情视图（关联房源与对方信息）
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long tenantId;
    private Long hostId;
    private Long homestayId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private Integer guestCount;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Long couponId;
    private String couponName;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String contactName;
    private String contactPhone;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Integer reviewed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 房源快照信息 */
    private String homestayTitle;
    private String coverImg;
    private String city;
    private String address;

    /** 管理员/房东视图：租客账号名 */
    private String tenantName;
    /** 管理员视图：房东姓名 */
    private String hostName;
}
