package com.bnbms.service;

import com.bnbms.model.vo.HomeVO;

/**
 * 首页聚合服务
 */
public interface HomeService {

    /**
     * 获取首页聚合数据（缓存 10 分钟，写操作 evict）
     */
    HomeVO getHome();

    /** 清除首页缓存（轮播/公告写操作后调用） */
    void evictCache();
}
