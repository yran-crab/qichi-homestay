package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 预订确认 / 创建订单入参
 */
@Data
public class BookingCreateDTO {

    @NotNull(message = "请选择房源")
    private Long homestayId;

    @NotNull(message = "请选择入住日期")
    @FutureOrPresent(message = "入住日期不能早于今天")
    private LocalDate checkInDate;

    @NotNull(message = "请选择退房日期")
    private LocalDate checkOutDate;

    @NotNull(message = "请填写入住人数")
    @Min(value = 1, message = "入住人数至少 1 人")
    private Integer guestCount;

    @NotBlank(message = "请填写入住人姓名")
    @Size(max = 50, message = "入住人姓名过长")
    private String contactName;

    @NotBlank(message = "请填写联系电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String contactPhone;

    /** 用户优惠券 ID（user_coupon.id），为空表示不使用优惠券；最终金额以服务端重算为准 */
    private Long couponId;
}
