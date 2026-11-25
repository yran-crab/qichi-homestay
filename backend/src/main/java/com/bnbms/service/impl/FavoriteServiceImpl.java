package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.RedisHelper;
import com.bnbms.entity.Favorite;
import com.bnbms.entity.Homestay;
import com.bnbms.mapper.FavoriteMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final HomestayMapper homestayMapper;
    private final RedisHelper redisHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long userId, Long homestayId) {
        Homestay homestay = homestayMapper.selectById(homestayId);
        if (homestay == null) {
            throw new BusinessException("房源不存在");
        }
        Favorite existing = favoriteMapper.selectOne(new QueryWrapper<Favorite>()
                .eq("user_id", userId)
                .eq("homestay_id", homestayId));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            homestayMapper.decrementFavoriteCount(homestayId);
            evictHotCache();
            return false;
        }
        if (!Constants.STAY_APPROVED.equals(homestay.getStatus())) {
            throw new BusinessException("房源未上架，暂时无法收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setHomestayId(homestayId);
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            // 并发重复收藏，幂等视为已收藏，不重复计数
            return true;
        }
        homestayMapper.incrementFavoriteCount(homestayId);
        evictHotCache();
        return true;
    }

    @Override
    public PageResult<HomestayCardVO> myPage(Long userId, long page, long pageSize) {
        com.baomidou.mybatisplus.core.metadata.IPage<HomestayCardVO> p = favoriteMapper.selectMyFavoritePage(
                new Page<>(page, pageSize), userId);
        p.getRecords().forEach(card -> card.setFavored(true));
        return new PageResult<>(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords());
    }

    @Override
    public Set<Long> findFavoredIds(Long userId, Collection<Long> homestayIds) {
        if (userId == null || CollectionUtils.isEmpty(homestayIds)) {
            return Collections.emptySet();
        }
        List<Favorite> favorites = favoriteMapper.selectList(new QueryWrapper<Favorite>()
                .select("homestay_id")
                .eq("user_id", userId)
                .in("homestay_id", homestayIds));
        return favorites.stream()
                .map(Favorite::getHomestayId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void evictHotCache() {
        redisHelper.delete(Constants.REDIS_CACHE_HOT);
    }
}
