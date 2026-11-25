package com.bnbms.service;

import com.bnbms.model.vo.AdminDashboardVO;
import com.bnbms.model.vo.HostDashboardVO;

/**
 * 数据看板服务
 */
public interface DashboardService {

    /** 管理员看板（Redis 缓存 5 分钟） */
    AdminDashboardVO adminDashboard();

    /** 房东看板（Redis 缓存 5 分钟，按 hostId 隔离） */
    HostDashboardVO hostDashboard(Long hostId);

    /** 清除管理员看板缓存 */
    void evictAdminCache();

    /** 清除房东看板缓存 */
    void evictHostCache(Long hostId);
}
