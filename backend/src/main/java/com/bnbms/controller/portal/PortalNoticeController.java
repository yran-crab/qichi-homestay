package com.bnbms.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bnbms.common.BusinessException;
import com.bnbms.common.Result;
import com.bnbms.entity.Notice;
import com.bnbms.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告浏览（匿名可访问）
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class PortalNoticeController {

    private final NoticeMapper noticeMapper;

    @GetMapping
    public Result<List<Notice>> list() {
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getCreatedAt));
        return Result.ok(notices);
    }

    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getStatus() == null || notice.getStatus() == 0) {
            throw new BusinessException("公告不存在或已下架");
        }
        return Result.ok(notice);
    }
}
