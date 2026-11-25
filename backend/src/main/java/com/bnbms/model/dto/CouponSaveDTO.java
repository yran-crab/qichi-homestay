package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员创建/编辑优惠券入参
 */
@Data
public class CouponSaveDTO {

    private Long id;

    @NotBlank(message = "请填写优惠券名称")
    private String name;

    /** CASH 代金券 / FULL_REDUCE 满减券 / DISCOUNT 折扣券 */
    @NotBlank(message = "请选择优惠券类型")
    private String type;

    @NotNull(message = "请填写使用门槛")
    @DecimalMin(value = "0", message = "使用门槛不能为负")
    private BigDecimal threshold;

    /** 代金/满减券优惠金额 */
    private BigDecimal discountAmount;
    /** 折扣券折扣率：88 = 8.8 折 */
    @Min(value = 1, message = "折扣率不合法")
    @Max(value = 99, message = "折扣率不合法")
    private Integer discountPercent;
    /** 折扣券封顶优惠 */
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "请填写发行总量")
    @Min(value = -1, message = "发行总量不合法")
    private Integer totalCount;

    @NotNull(message = "请填写每人限领数量")
    @Min(value = 1, message = "每人至少限领 1 张")
    private Integer perLimit;

    /** FIXED 固定有效期 / DAYS 领取后 N 天 */
    @NotBlank(message = "请选择有效期类型")
    private String validType;

    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Integer validDays;

    private Integer status;
}
