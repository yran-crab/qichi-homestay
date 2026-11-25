package com.bnbms.model.dto;

import lombok.Data;

/**
 * 订单分页查询（三端共用，keyword 仅管理员使用）
 */
@Data
public class OrderQueryDTO {

    private String status;
    private String keyword;
    private Long page = 1L;
    private Long pageSize = 10L;
}
