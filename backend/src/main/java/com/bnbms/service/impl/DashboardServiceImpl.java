package com.bnbms.service.impl;

import com.bnbms.common.Constants;
import com.bnbms.common.RedisHelper;
import com.bnbms.mapper.DashboardMapper;
import com.bnbms.model.vo.AdminDashboardVO;
import com.bnbms.model.vo.HostDashboardVO;
import com.bnbms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;
    private final RedisHelper redisHelper;

    private static final long TTL_MINUTES = 5;

    @Override
    public AdminDashboardVO adminDashboard() {
        Object cached = redisHelper.get(Constants.REDIS_CACHE_ADMIN_DASHBOARD);
        if (cached instanceof AdminDashboardVO) {
            return (AdminDashboardVO) cached;
        }
        AdminDashboardVO vo = new AdminDashboardVO();
        vo.setTotalUsers(dashboardMapper.countUsers());
        vo.setTotalHomestays(dashboardMapper.countHomestays());
        vo.setTotalOrders(dashboardMapper.countOrders());
        vo.setTotalRevenue(dashboardMapper.sumRevenue());
        vo.setNewUsersThisWeek(dashboardMapper.countNewUsersThisWeek());
        vo.setNewHomestaysThisWeek(dashboardMapper.countNewHomestaysThisWeek());
        vo.setNewOrdersThisWeek(dashboardMapper.countOrdersThisWeek());
        vo.setRevenueThisWeek(dashboardMapper.sumRevenueThisWeek());
        vo.setDailyStats(dashboardMapper.dailyOrderStats());
        vo.setTopCities(dashboardMapper.topCities());
        vo.setTypeDist(dashboardMapper.typeDistribution());
        vo.setPendingHomestays(dashboardMapper.countPendingHomestays());
        vo.setPendingHosts(dashboardMapper.countPendingHosts());
        redisHelper.set(Constants.REDIS_CACHE_ADMIN_DASHBOARD, vo, TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public HostDashboardVO hostDashboard(Long hostId) {
        String key = Constants.REDIS_CACHE_HOST_DASHBOARD_PREFIX + hostId;
        Object cached = redisHelper.get(key);
        if (cached instanceof HostDashboardVO) {
            return (HostDashboardVO) cached;
        }
        HostDashboardVO vo = new HostDashboardVO();
        vo.setTotalHomestays(dashboardMapper.countHostHomestays(hostId));
        vo.setTotalOrders(dashboardMapper.countHostOrders(hostId));
        vo.setTotalRevenue(dashboardMapper.sumHostRevenue(hostId));
        vo.setRevenueThisMonth(dashboardMapper.sumHostRevenueThisMonth(hostId));
        vo.setOrdersThisMonth(dashboardMapper.countHostOrdersThisMonth(hostId));
        vo.setDailyRevenue(dashboardMapper.hostDailyRevenue(hostId));
        vo.setOrderStatusDist(dashboardMapper.hostOrderStatusDist(hostId));
        vo.setHomestayRank(dashboardMapper.hostHomestayRank(hostId));
        redisHelper.set(key, vo, TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public void evictAdminCache() {
        redisHelper.delete(Constants.REDIS_CACHE_ADMIN_DASHBOARD);
    }

    @Override
    public void evictHostCache(Long hostId) {
        redisHelper.delete(Constants.REDIS_CACHE_HOST_DASHBOARD_PREFIX + hostId);
    }
}
