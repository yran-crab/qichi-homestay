package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bnbms.common.Constants;
import com.bnbms.common.RedisHelper;
import com.bnbms.entity.Banner;
import com.bnbms.entity.Homestay;
import com.bnbms.entity.Notice;
import com.bnbms.mapper.BannerMapper;
import com.bnbms.mapper.HomestayMapper;
import com.bnbms.mapper.NoticeMapper;
import com.bnbms.model.vo.HomeVO;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.service.HomeService;
import com.bnbms.service.HomestayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final BannerMapper bannerMapper;
    private final NoticeMapper noticeMapper;
    private final HomestayMapper homestayMapper;
    private final HomestayService homestayService;
    private final RedisHelper redisHelper;

    private static final long HOME_TTL_MINUTES = 10;

    @Override
    public HomeVO getHome() {
        Object cached = redisHelper.get(Constants.REDIS_CACHE_HOME);
        if (cached instanceof HomeVO) {
            return (HomeVO) cached;
        }
        HomeVO vo = buildHome();
        redisHelper.set(Constants.REDIS_CACHE_HOME, vo, HOME_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public void evictCache() {
        redisHelper.delete(Constants.REDIS_CACHE_HOME);
    }

    private HomeVO buildHome() {
        HomeVO vo = new HomeVO();

        // 启用轮播按 sort
        List<Banner> banners = bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort));
        vo.setBanners(banners.stream().map(b -> {
            HomeVO.BannerItem item = new HomeVO.BannerItem();
            item.setId(b.getId());
            item.setTitle(b.getTitle());
            item.setImageUrl(b.getImageUrl());
            item.setLink(b.getLink());
            return item;
        }).collect(Collectors.toList()));

        // 启用公告
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getCreatedAt));
        vo.setNotices(notices.stream().map(n -> {
            HomeVO.NoticeItem item = new HomeVO.NoticeItem();
            item.setId(n.getId());
            item.setTitle(n.getTitle());
            item.setContent(n.getContent());
            item.setCreatedAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
            return item;
        }).collect(Collectors.toList()));

        // 热门推荐（走 HomestayService 缓存）
        vo.setHotList(homestayService.hotList(8));

        // 最新上线 8 套
        List<Homestay> latest = homestayMapper.selectLatestList(8);
        vo.setLatestList(latest.stream().map(HomestayCardVO::of).collect(Collectors.toList()));

        return vo;
    }
}
