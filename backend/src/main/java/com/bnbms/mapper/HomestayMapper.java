package com.bnbms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bnbms.entity.Homestay;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface HomestayMapper extends BaseMapper<Homestay> {

    /**
     * 门户房源分页：仅已上架，支持关键词/城市/价格/类型/可住人数/入住档期过滤与排序
     */
    IPage<Homestay> selectPortalPage(IPage<Homestay> page,
                                     @Param("keyword") String keyword,
                                     @Param("city") String city,
                                     @Param("type") String type,
                                     @Param("minPrice") java.math.BigDecimal minPrice,
                                     @Param("maxPrice") java.math.BigDecimal maxPrice,
                                     @Param("minGuests") Integer minGuests,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut,
                                     @Param("sort") String sort);

    /**
     * 热门房源（已上架，按浏览量+收藏权重排序）
     */
    List<Homestay> selectHotList(@Param("limit") int limit);

    /**
     * 最新上线房源（已上架，按创建时间倒序）
     */
    @org.apache.ibatis.annotations.Select("SELECT * FROM homestay WHERE status = 'APPROVED' ORDER BY created_at DESC LIMIT #{limit}")
    List<Homestay> selectLatestList(@Param("limit") int limit);

    /**
     * 统计房源关联订单数（删除房源前校验）
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(1) FROM booking_order WHERE homestay_id = #{id}")
    long countOrders(@Param("id") Long id);

    /**
     * 原子自增浏览量
     */
    @org.apache.ibatis.annotations.Update("UPDATE homestay SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 收藏数 +1
     */
    @org.apache.ibatis.annotations.Update("UPDATE homestay SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    int incrementFavoriteCount(@Param("id") Long id);

    /**
     * 收藏数 -1（不为负）
     */
    @org.apache.ibatis.annotations.Update("UPDATE homestay SET favorite_count = favorite_count - 1 " +
            "WHERE id = #{id} AND favorite_count > 0")
    int decrementFavoriteCount(@Param("id") Long id);

    /**
     * 重算房源平均评分与评价数（评价增删后调用）
     */
    @org.apache.ibatis.annotations.Update("UPDATE homestay h SET h.rating = " +
            "COALESCE((SELECT ROUND(AVG(r.rating), 1) FROM review r WHERE r.homestay_id = h.id), 0), " +
            "h.review_count = (SELECT COUNT(1) FROM review r WHERE r.homestay_id = h.id) " +
            "WHERE h.id = #{id}")
    int recalcRating(@Param("id") Long id);

    /**
     * 智能推荐：按偏好城市/房型/价格区间查询已上架房源（排除已收藏/已下单的）
     */
    List<Homestay> selectRecommend(@Param("cities") List<String> cities,
                                   @Param("types") List<String> types,
                                   @Param("minPrice") java.math.BigDecimal minPrice,
                                   @Param("maxPrice") java.math.BigDecimal maxPrice,
                                   @Param("excludeIds") List<Long> excludeIds,
                                   @Param("limit") int limit);

    /**
     * 按 ID 批量查询房源（用于构建用户偏好画像）
     */
    List<Homestay> selectByIds(@Param("ids") List<Long> ids);
}
