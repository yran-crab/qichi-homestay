package com.bnbms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bnbms.common.Constants;
import com.bnbms.common.Result;
import com.bnbms.entity.Notice;
import com.bnbms.mapper.NoticeMapper;
import com.bnbms.model.dto.NoticeSaveDTO;
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
 * 管理员公告 CRUD
 */
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminNoticeController {

    private final NoticeMapper noticeMapper;
    private final HomeService homeService;

    @GetMapping
    public Result<List<Notice>> list() {
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getCreatedAt));
        return Result.ok(notices);
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody NoticeSaveDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        noticeMapper.insert(notice);
        homeService.evictCache();
        return Result.ok("创建成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveDTO dto) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return Result.fail("公告不存在");
        }
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus() != null ? dto.getStatus() : notice.getStatus());
        noticeMapper.updateById(notice);
        homeService.evictCache();
        return Result.ok("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        homeService.evictCache();
        return Result.ok("删除成功", null);
    }
}
