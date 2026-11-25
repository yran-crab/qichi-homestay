package com.bnbms.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理员房源审核
 */
@Data
public class HomestayAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    /** 驳回时必填 */
    private String remark;
}
