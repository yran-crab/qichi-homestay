package com.bnbms.model.vo;

import com.bnbms.entity.Homestay;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房源列表卡片
 */
@Data
public class HomestayCardVO {

    private Long id;
    private Long hostId;
    private String title;
    private String city;
    private String address;
    private String coverImg;
    private String type;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer beds;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer favoriteCount;

    /** 房源状态（我的收藏场景用于下架标识） */
    private String status;

    /** 当前用户是否收藏（未登录为 false） */
    private Boolean favored = false;

    public static HomestayCardVO of(Homestay h) {
        HomestayCardVO vo = new HomestayCardVO();
        vo.setId(h.getId());
        vo.setHostId(h.getHostId());
        vo.setTitle(h.getTitle());
        vo.setCity(h.getCity());
        vo.setAddress(h.getAddress());
        vo.setCoverImg(h.getCoverImg());
        vo.setType(h.getType());
        vo.setPrice(h.getPrice());
        vo.setOriginalPrice(h.getOriginalPrice());
        vo.setMaxGuests(h.getMaxGuests());
        vo.setBedrooms(h.getBedrooms());
        vo.setBeds(h.getBeds());
        vo.setRating(h.getRating());
        vo.setReviewCount(h.getReviewCount());
        vo.setFavoriteCount(h.getFavoriteCount());
        vo.setStatus(h.getStatus());
        return vo;
    }
}
