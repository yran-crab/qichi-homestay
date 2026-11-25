package com.bnbms.model.vo;

import com.bnbms.entity.Facility;
import com.bnbms.entity.Homestay;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房源详情（门户 + 房东复用）
 */
@Data
public class HomestayDetailVO {

    private Long id;
    private Long hostId;
    private String title;
    private String city;
    private String address;
    private String coverImg;
    private List<String> images;
    private String type;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal area;
    private Integer bedrooms;
    private Integer livingRooms;
    private Integer bathrooms;
    private Integer beds;
    private Integer maxGuests;
    private String floorInfo;
    private String description;
    private String rules;
    private String status;
    private String auditRemark;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer orderCount;
    private BigDecimal rating;
    private Integer reviewCount;
    private LocalDateTime createdAt;

    private List<Facility> facilities;

    /** 房东公开信息 */
    private HostBrief host;

    /** 当前用户是否收藏 */
    private Boolean favored = false;

    @Data
    public static class HostBrief {
        private Long id;
        private String realName;
        private String avatar;
    }

    public static HomestayDetailVO of(Homestay h) {
        HomestayDetailVO vo = new HomestayDetailVO();
        vo.setId(h.getId());
        vo.setHostId(h.getHostId());
        vo.setTitle(h.getTitle());
        vo.setCity(h.getCity());
        vo.setAddress(h.getAddress());
        vo.setCoverImg(h.getCoverImg());
        if (h.getImages() != null && !h.getImages().isEmpty()) {
            java.util.List<String> imgs = new java.util.ArrayList<>();
            for (String s : h.getImages().split(",")) {
                if (!s.trim().isEmpty()) {
                    imgs.add(s.trim());
                }
            }
            vo.setImages(imgs);
        } else {
            vo.setImages(new java.util.ArrayList<>());
        }
        vo.setType(h.getType());
        vo.setPrice(h.getPrice());
        vo.setOriginalPrice(h.getOriginalPrice());
        vo.setArea(h.getArea());
        vo.setBedrooms(h.getBedrooms());
        vo.setLivingRooms(h.getLivingRooms());
        vo.setBathrooms(h.getBathrooms());
        vo.setBeds(h.getBeds());
        vo.setMaxGuests(h.getMaxGuests());
        vo.setFloorInfo(h.getFloorInfo());
        vo.setDescription(h.getDescription());
        vo.setRules(h.getRules());
        vo.setStatus(h.getStatus());
        vo.setAuditRemark(h.getAuditRemark());
        vo.setViewCount(h.getViewCount());
        vo.setFavoriteCount(h.getFavoriteCount());
        vo.setOrderCount(h.getOrderCount());
        vo.setRating(h.getRating());
        vo.setReviewCount(h.getReviewCount());
        vo.setCreatedAt(h.getCreatedAt());
        return vo;
    }
}
