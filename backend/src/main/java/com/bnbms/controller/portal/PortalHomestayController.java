package com.bnbms.controller.portal;

import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.HomestayQueryDTO;
import com.bnbms.model.vo.BookedDateVO;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.model.vo.HomestayDetailVO;
import com.bnbms.model.vo.ReviewVO;
import com.bnbms.security.OptionalAuth;
import com.bnbms.security.UserContext;
import com.bnbms.service.BookingService;
import com.bnbms.service.HomestayService;
import com.bnbms.service.RecommendService;
import com.bnbms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门户房源浏览（匿名可访问，登录用户携带个性化收藏态）
 */
@OptionalAuth
@RestController
@RequestMapping("/api/homestays")
@RequiredArgsConstructor
public class PortalHomestayController {

    private final HomestayService homestayService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final RecommendService recommendService;

    @GetMapping
    public Result<PageResult<HomestayCardVO>> page(@ModelAttribute HomestayQueryDTO query) {
        return Result.ok(homestayService.portalPage(query));
    }

    @GetMapping("/hot")
    public Result<List<HomestayCardVO>> hot(@RequestParam(defaultValue = "8") int limit) {
        return Result.ok(homestayService.hotList(limit));
    }

    /** 个性化推荐（基于收藏/订单的协同过滤，未登录返回热门） */
    @GetMapping("/recommend")
    public Result<List<HomestayCardVO>> recommend(@RequestParam(defaultValue = "8") int limit) {
        return Result.ok(recommendService.recommend(UserContext.getUserIdOrNull(), limit));
    }

    @GetMapping("/{id}")
    public Result<HomestayDetailVO> detail(@PathVariable Long id) {
        return Result.ok(homestayService.getPortalDetail(id, UserContext.getUserIdOrNull()));
    }

    /** 房源已被预订的日期区间（公开，供日历禁用） */
    @GetMapping("/{id}/booked-dates")
    public Result<List<BookedDateVO>> bookedDates(@PathVariable Long id) {
        return Result.ok(bookingService.bookedDates(id));
    }

    /** 房源详情页公开评价分页 */
    @GetMapping("/{id}/reviews")
    public Result<PageResult<ReviewVO>> reviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(reviewService.homestayPage(id, page, pageSize));
    }
}
