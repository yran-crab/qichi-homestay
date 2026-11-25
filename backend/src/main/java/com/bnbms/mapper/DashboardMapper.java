package com.bnbms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据看板统计 Mapper
 */
@Mapper
public interface DashboardMapper {

    /* ============================ 管理员看板 ============================ */

    @Select("SELECT COUNT(1) FROM sys_user WHERE status = 1")
    long countUsers();

    @Select("SELECT COUNT(1) FROM homestay")
    long countHomestays();

    @Select("SELECT COUNT(1) FROM booking_order")
    long countOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM booking_order WHERE status IN ('PAID','CHECKED_IN','COMPLETED')")
    BigDecimal sumRevenue();

    @Select("SELECT COUNT(1) FROM sys_user WHERE status = 1 AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    long countNewUsersThisWeek();

    @Select("SELECT COUNT(1) FROM homestay WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    long countNewHomestaysThisWeek();

    @Select("SELECT COUNT(1) FROM booking_order WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    long countOrdersThisWeek();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM booking_order WHERE status IN ('PAID','CHECKED_IN','COMPLETED') AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    BigDecimal sumRevenueThisWeek();

    /** 近 30 天每日订单量与交易额 */
    @Select("SELECT DATE(created_at) AS d, COUNT(1) AS cnt, COALESCE(SUM(total_amount), 0) AS amt " +
            "FROM booking_order WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 29 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY d")
    List<Map<String, Object>> dailyOrderStats();

    /** 热销城市 TOP10 */
    @Select("SELECT h.city AS name, COUNT(o.id) AS cnt " +
            "FROM booking_order o JOIN homestay h ON h.id = o.homestay_id " +
            "WHERE o.status IN ('PAID','CHECKED_IN','COMPLETED') " +
            "GROUP BY h.city ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> topCities();

    /** 房源类型占比 */
    @Select("SELECT type AS name, COUNT(1) AS cnt FROM homestay WHERE status = 'APPROVED' GROUP BY type")
    List<Map<String, Object>> typeDistribution();

    @Select("SELECT COUNT(1) FROM homestay WHERE status = 'PENDING'")
    long countPendingHomestays();

    @Select("SELECT COUNT(1) FROM sys_user WHERE role = 'TENANT' AND host_status = 'PENDING'")
    long countPendingHosts();

    /* ============================ 房东看板 ============================ */

    @Select("SELECT COUNT(1) FROM homestay WHERE host_id = #{hostId}")
    long countHostHomestays(@Param("hostId") Long hostId);

    @Select("SELECT COUNT(1) FROM booking_order WHERE host_id = #{hostId} AND status IN ('PAID','CHECKED_IN','COMPLETED')")
    long countHostOrders(@Param("hostId") Long hostId);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM booking_order WHERE host_id = #{hostId} AND status IN ('PAID','CHECKED_IN','COMPLETED')")
    BigDecimal sumHostRevenue(@Param("hostId") Long hostId);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM booking_order WHERE host_id = #{hostId} " +
            "AND status IN ('PAID','CHECKED_IN','COMPLETED') " +
            "AND created_at >= DATE_FORMAT(NOW(), '%Y-%m-01')")
    BigDecimal sumHostRevenueThisMonth(@Param("hostId") Long hostId);

    @Select("SELECT COUNT(1) FROM booking_order WHERE host_id = #{hostId} " +
            "AND created_at >= DATE_FORMAT(NOW(), '%Y-%m-01')")
    long countHostOrdersThisMonth(@Param("hostId") Long hostId);

    /** 房东近 30 天每日收入 */
    @Select("SELECT DATE(created_at) AS d, COALESCE(SUM(total_amount), 0) AS amt " +
            "FROM booking_order WHERE host_id = #{hostId} " +
            "AND status IN ('PAID','CHECKED_IN','COMPLETED') " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL 29 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY d")
    List<Map<String, Object>> hostDailyRevenue(@Param("hostId") Long hostId);

    /** 房东订单状态分布 */
    @Select("SELECT status AS name, COUNT(1) AS cnt FROM booking_order WHERE host_id = #{hostId} GROUP BY status")
    List<Map<String, Object>> hostOrderStatusDist(@Param("hostId") Long hostId);

    /** 房东房源热度排行 TOP10 */
    @Select("SELECT title AS name, view_count AS views, order_count AS orders " +
            "FROM homestay WHERE host_id = #{hostId} ORDER BY (view_count + order_count * 10) DESC LIMIT 10")
    List<Map<String, Object>> hostHomestayRank(@Param("hostId") Long hostId);
}
