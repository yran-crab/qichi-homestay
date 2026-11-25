package com.bnbms.service;

import com.bnbms.common.PageResult;
import com.bnbms.model.dto.ReviewCreateDTO;
import com.bnbms.model.dto.ReviewQueryDTO;
import com.bnbms.model.vo.ReviewVO;

/**
 * 评价与回复服务
 */
public interface ReviewService {

    /**
     * 租客发表评价（仅已完成且未评价订单，每单唯一）
     */
    ReviewVO create(Long tenantId, ReviewCreateDTO dto);

    /**
     * 房东回复评价（仅该房源房东，仅可回复一次）
     */
    void reply(Long hostId, Long reviewId, String content);

    /**
     * 房源详情页公开评价分页
     */
    PageResult<ReviewVO> homestayPage(Long homestayId, long page, long pageSize);

    /**
     * 租客我的评价分页
     */
    PageResult<ReviewVO> myPage(Long tenantId, long page, long pageSize);

    /**
     * 房东收到的评价分页（本人房源）
     */
    PageResult<ReviewVO> hostPage(Long hostId, ReviewQueryDTO query);

    /**
     * 管理员全量评价分页
     */
    PageResult<ReviewVO> adminPage(ReviewQueryDTO query);

    /**
     * 管理员删除评价（同步重算房源评分）
     */
    void delete(Long reviewId);
}
