package com.bnbms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板（管理员发布）
 */
@Data
@TableName("coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;
    /** CASH 代金券 / FULL_REDUCE 满减券 / DISCOUNT 折扣券 */
    private String type;
    /** 使用门槛：订单满多少元可用，0 为无门槛 */
    private BigDecimal threshold;
    /** 代金/满减券的优惠金额 */
    private BigDecimal discountAmount;
    /** 折扣券折扣率：88 表示 8.8 折 */
    private Integer discountPercent;
    /** 折扣券优惠金额封顶，NULL 不封顶 */
    private BigDecimal maxDiscountAmount;
    /** 发行总量，-1 表示不限量 */
    private Integer totalCount;
    /** 累计已领取 */
    private Integer receivedCount;
    /** 累计已使用 */
    private Integer usedCount;
    /** 每人限领数量 */
    private Integer perLimit;
    /** FIXED 固定有效期 / DAYS 领取后 N 天有效 */
    private String validType;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Integer validDays;
    /** 0 下架 1 上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
