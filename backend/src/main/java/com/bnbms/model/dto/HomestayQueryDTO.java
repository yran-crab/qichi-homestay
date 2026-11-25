package com.bnbms.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 门户房源搜索条件
 */
@Data
public class HomestayQueryDTO {

    private String keyword;
    private String city;
    private String type;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minGuests;
    private LocalDate checkIn;
    private LocalDate checkOut;
    /** newest / price_asc / price_desc / hot */
    private String sort = "newest";

    private Long page = 1L;
    private Long pageSize = 12L;
}
