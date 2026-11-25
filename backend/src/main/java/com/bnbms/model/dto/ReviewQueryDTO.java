package com.bnbms.model.dto;

import lombok.Data;

@Data
public class ReviewQueryDTO {

    /** 房源筛选（房东端可选） */
    private Long homestayId;
    /** 关键词（房源标题/评价内容/租客名） */
    private String keyword;
    /** 房东端：是否只看待回复 */
    private Boolean onlyUnreplied;

    private Long page = 1L;
    private Long pageSize = 10L;
}
