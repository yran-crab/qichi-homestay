package com.bnbms.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {

    private Long id;
    private Long orderId;
    private Long homestayId;
    private Long tenantId;
    private Integer rating;
    private String content;
    /** 图片原始逗号串（SQL 别名映射，不直接输出） */
    private String imagesRaw;
    /** 图片 URL 列表（Service 层由逗号字符串拆分） */
    private List<String> images;
    private String reply;
    private LocalDateTime replyAt;
    private LocalDateTime createdAt;

    private String tenantName;
    private String tenantAvatar;
    private String homestayTitle;
    private String city;
    private String orderNo;
}
