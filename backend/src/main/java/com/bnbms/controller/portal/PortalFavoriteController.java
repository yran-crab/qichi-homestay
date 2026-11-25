package com.bnbms.controller.portal;

import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.vo.HomestayCardVO;
import com.bnbms.security.UserContext;
import com.bnbms.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏（登录即可，不限角色）
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class PortalFavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏 / 取消收藏，返回 true=收藏后状态，false=取消后状态 */
    @PostMapping("/{homestayId}/toggle")
    public Result<Boolean> toggle(@PathVariable Long homestayId) {
        return Result.ok(favoriteService.toggle(UserContext.getUserId(), homestayId));
    }

    /** 我的收藏分页 */
    @GetMapping("/my")
    public Result<PageResult<HomestayCardVO>> myFavorites(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long pageSize) {
        return Result.ok(favoriteService.myPage(UserContext.getUserId(), page, pageSize));
    }
}
