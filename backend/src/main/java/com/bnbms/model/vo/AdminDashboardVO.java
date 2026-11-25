package com.bnbms.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员看板数据
 */
@Data
public class AdminDashboardVO {
    private long totalUsers;
    private long totalHomestays;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long newUsersThisWeek;
    private long newHomestaysThisWeek;
    private long newOrdersThisWeek;
    private BigDecimal revenueThisWeek;
    private List<Map<String, Object>> dailyStats;
    private List<Map<String, Object>> topCities;
    private List<Map<String, Object>> typeDist;
    private long pendingHomestays;
    private long pendingHosts;
}
