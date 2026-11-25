package com.bnbms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 房东看板数据
 */
@Data
public class HostDashboardVO {
    private long totalHomestays;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private long ordersThisMonth;
    private List<Map<String, Object>> dailyRevenue;
    private List<Map<String, Object>> orderStatusDist;
    private List<Map<String, Object>> homestayRank;
}
