package com.bnbms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bnbms.common.Constants;
import com.bnbms.common.Result;
import com.bnbms.entity.Banner;
import com.bnbms.mapper.BannerMapper;
import com.bnbms.model.dto.BannerSaveDTO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员轮播图 CRUD
 */
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminBannerController {

    private final BannerMapper bannerMapper;
    private final HomeService homeService;

    @GetMapping
    public Result<List<Banner>> list() {
        List<Banner> banners = bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSort));
        return Result.ok(banners);
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody BannerSaveDTO dto) {
        Banner banner = new Banner();
        banner.setTitle(dto.getTitle());
        banner.setImageUrl(dto.getImageUrl());
        banner.setLink(dto.getLink());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        bannerMapper.insert(banner);
        homeService.evictCache();
        return Result.ok("创建成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BannerSaveDTO dto) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return Result.fail("轮播图不存在");
        }
        banner.setTitle(dto.getTitle());
        banner.setImageUrl(dto.getImageUrl());
        banner.setLink(dto.getLink());
        banner.setSort(dto.getSort());
        banner.setStatus(dto.getStatus() != null ? dto.getStatus() : banner.getStatus());
        bannerMapper.updateById(banner);
        homeService.evictCache();
        return Result.ok("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        homeService.evictCache();
        return Result.ok("删除成功", null);
    }
}
