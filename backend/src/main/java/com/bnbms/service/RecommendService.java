package com.bnbms.service;

import com.bnbms.model.vo.HomestayCardVO;

import java.util.List;

/**
 * 智能推荐服务 — 基于用户收藏 / 订单历史的协同过滤推荐
 */
public interface RecommendService {

    /**
     * 为指定用户生成个性化推荐房源列表。
     * <p>算法思路：
     * <ol>
     *   <li>收集用户已收藏 + 已下单的房源作为"偏好种子"</li>
     *   <li>从种子房源中提取偏好城市、房型、价格区间</li>
     *   <li>在已审核房源中按城市/房型/价格匹配度打分排序，排除种子本身</li>
     *   <li>不足时用热门房源补齐</li>
     * </ol>
     * 未登录或无历史数据时返回全局热门推荐。
     */
    List<HomestayCardVO> recommend(Long userId, int limit);
}
