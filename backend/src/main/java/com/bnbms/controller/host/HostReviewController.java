package com.bnbms.controller.host;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.ReplyCreateDTO;
import com.bnbms.model.dto.ReviewQueryDTO;
import com.bnbms.model.vo.ReviewVO;
import com.bnbms.security.RequireRole;
import com.bnbms.security.UserContext;
import com.bnbms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 房东评价管理：查看收到的评价、回复评价
 */
@RestController
@RequestMapping("/api/host/reviews")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_HOST)
public class HostReviewController {

    private final ReviewService reviewService;

    /** 房东收到的评价分页（本人房源） */
    @GetMapping
    public Result<PageResult<ReviewVO>> page(@ModelAttribute ReviewQueryDTO query) {
        return Result.ok(reviewService.hostPage(UserContext.getUserId(), query));
    }

    /** 回复评价（仅该房源房东，仅一次） */
    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @Valid @RequestBody ReplyCreateDTO dto) {
        reviewService.reply(UserContext.getUserId(), id, dto.getContent());
        return Result.ok("回复成功", null);
    }
}
