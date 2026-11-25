package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.ReviewQueryDTO;
import com.bnbms.model.vo.ReviewVO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员评价管理：全量查看、删除
 */
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminReviewController {

    private final ReviewService reviewService;

    /** 全量评价分页 */
    @GetMapping
    public Result<PageResult<ReviewVO>> page(@ModelAttribute ReviewQueryDTO query) {
        return Result.ok(reviewService.adminPage(query));
    }

    /** 删除评价（同步重算房源评分） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.ok("删除成功", null);
    }
}
