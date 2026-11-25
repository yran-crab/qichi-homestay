package com.bnbms.controller.portal;

import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.ReviewCreateDTO;
import com.bnbms.model.vo.ReviewVO;
import com.bnbms.security.UserContext;
import com.bnbms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 租客评价：发表评价、我的评价
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class PortalReviewController {

    private final ReviewService reviewService;

    /** 发表评价（仅已完成订单，每单唯一） */
    @PostMapping
    public Result<ReviewVO> create(@Valid @RequestBody ReviewCreateDTO dto) {
        return Result.ok("评价发表成功", reviewService.create(UserContext.getUserId(), dto));
    }

    /** 我的评价分页 */
    @GetMapping("/my")
    public Result<PageResult<ReviewVO>> myReviews(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(reviewService.myPage(UserContext.getUserId(), page, pageSize));
    }
}
