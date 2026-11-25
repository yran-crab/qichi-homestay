package com.bnbms.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页聚合数据
 */
@Data
public class HomeVO {

    private List<BannerItem> banners;
    private List<NoticeItem> notices;
    private List<HomestayCardVO> hotList;
    private List<HomestayCardVO> latestList;

    @Data
    public static class BannerItem {
        private Long id;
        private String title;
        private String imageUrl;
        private String link;
    }

    @Data
    public static class NoticeItem {
        private Long id;
        private String title;
        private String content;
        private String createdAt;
    }
}
