package com.bnbms.service;

import com.bnbms.common.PageResult;
import com.bnbms.model.vo.HomestayCardVO;

import java.util.Collection;
import java.util.Set;

public interface FavoriteService {

    /**
     * 切换收藏：未收藏→收藏（true）；已收藏→取消（false）
     */
    boolean toggle(Long userId, Long homestayId);

    /**
     * 我的收藏分页（含已下架房源标识）
     */
    PageResult<HomestayCardVO> myPage(Long userId, long page, long pageSize);

    /**
     * 查询用户对给定房源集合的收藏 id 集合
     */
    Set<Long> findFavoredIds(Long userId, Collection<Long> homestayIds);
}
