package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bnbms.entity.BookingOrder;
import com.bnbms.entity.Favorite;
import com.bnbms.entity.Homestay;
import com.bnbms.mapper.BookingOrderMapper;
import com.bnbms.mapper.FavoriteMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 智能推荐服务实现 — 基于用户收藏/订单历史的协同过滤
 *
 * <p>算法流程：
 * <ol>
 *   <li>收集用户已收藏 + 已下单的房源 ID 集合（种子）</li>
 *   <li>按种子 ID 批量查询房源完整信息，提取偏好画像：
 *       偏好城市集合、偏好房型集合、价格区间 [avg*0.7, avg*1.3]</li>
 *   <li>调用 selectRecommend 查询匹配画像的房源（排除种子），按热度排序</li>
 *   <li>不足 limit 时用全局热门补齐（仍排除种子）</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final HomestayMapper homestayMapper;
    private final FavoriteMapper favoriteMapper;
    private final BookingOrderMapper bookingOrderMapper;

    @Override
    public List<HomestayCardVO> recommend(Long userId, int limit) {
        // 未登录或无偏好数据 → 返回全局热门
        if (userId == null) {
            return toCardList(homestayMapper.selectHotList(limit));
        }

        // 1. 收集种子：收藏 + 订单
        Set<Long> seedIds = new HashSet<>();

        List<Favorite> favs = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
        favs.forEach(f -> seedIds.add(f.getHomestayId()));

        List<BookingOrder> orders = bookingOrderMapper.selectList(
                new LambdaQueryWrapper<BookingOrder>().eq(BookingOrder::getTenantId, userId)
                        .select(BookingOrder::getHomestayId));
        orders.forEach(o -> seedIds.add(o.getHomestayId()));

        if (seedIds.isEmpty()) {
            // 无历史数据 → 热门推荐
            return toCardList(homestayMapper.selectHotList(limit));
        }

        // 2. 构建偏好画像
        List<Homestay> seeds = homestayMapper.selectByIds(new ArrayList<>(seedIds));
        if (seeds.isEmpty()) {
            return toCardList(homestayMapper.selectHotList(limit));
        }

        Set<String> prefCities = seeds.stream()
                .map(Homestay::getCity)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());

        Set<String> prefTypes = seeds.stream()
                .map(Homestay::getType)
                .filter(t -> t != null && !t.isEmpty())
                .collect(Collectors.toSet());

        // 价格区间：均值的 0.7~1.3 倍
        BigDecimal avgPrice = seeds.stream()
                .map(Homestay::getPrice)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(seeds.size()), 2, RoundingMode.HALF_UP);
        BigDecimal minPrice = avgPrice.multiply(new BigDecimal("0.7")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxPrice = avgPrice.multiply(new BigDecimal("1.3")).setScale(2, RoundingMode.HALF_UP);

        // 3. 查询匹配推荐（排除种子）
        List<Long> excludeIds = new ArrayList<>(seedIds);
        List<Homestay> recommended = homestayMapper.selectRecommend(
                new ArrayList<>(prefCities),
                new ArrayList<>(prefTypes),
                minPrice,
                maxPrice,
                excludeIds,
                limit);

        // 4. 不足时用热门补齐（仍排除种子 + 已推荐）
        if (recommended.size() < limit) {
            Set<Long> recommendedIds = new HashSet<>();
            recommended.forEach(h -> recommendedIds.add(h.getId()));
            excludeIds.addAll(recommendedIds);
            List<Homestay> hot = homestayMapper.selectHotList(limit * 2);
            for (Homestay h : hot) {
                if (recommended.size() >= limit) break;
                if (!excludeIds.contains(h.getId())) {
                    recommended.add(h);
                    excludeIds.add(h.getId());
                }
            }
        }

        log.debug("用户{}推荐结果: 种子{}个, 城市偏好{}, 房型{}, 价格区间[{},{}], 推荐{}个",
                userId, seedIds.size(), prefCities, prefTypes, minPrice, maxPrice, recommended.size());

        return toCardList(recommended);
    }

    private List<HomestayCardVO> toCardList(List<Homestay> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(HomestayCardVO::of).collect(Collectors.toList());
    }
}
