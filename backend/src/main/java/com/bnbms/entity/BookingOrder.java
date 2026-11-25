package com.bnbms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("booking_order")
public class BookingOrder {

    @TableId(type = IdType.AUTO)
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
    /** 使用的用户优惠券 ID */
    private Long couponId;
    /** 优惠券名称快照 */
    private String couponName;
    /** 优惠券抵扣金额 */
    private BigDecimal discountAmount;
    /** 实付金额（优惠后） */
    private BigDecimal payAmount;
    private String contactName;
    private String contactPhone;
    private String status;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Integer reviewed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
