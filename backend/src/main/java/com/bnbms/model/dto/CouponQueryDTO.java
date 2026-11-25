package com.bnbms.model.dto;

import lombok.Data;

/**
 * 管理端优惠券分页查询
 */
@Data
public class CouponQueryDTO {

    private Long page = 1L;
    private Long pageSize = 10L;
    /** 名称模糊搜索 */
    private String name;
    /** 状态：0 下架 / 1 上架，空为全部 */
    private Integer status;
    /** 类型筛选 */
    private String type;
}
