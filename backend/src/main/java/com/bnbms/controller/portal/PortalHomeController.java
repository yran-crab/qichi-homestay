package com.bnbms.controller.portal;

import com.bnbms.common.Result;
import com.bnbms.model.vo.HomeVO;
import com.bnbms.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页聚合接口（匿名可访问）
 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class PortalHomeController {

    private final HomeService homeService;

    @GetMapping
    public Result<HomeVO> home() {
        return Result.ok(homeService.getHome());
    }
}
