package com.bnbms.controller.host;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.entity.Homestay;
import com.bnbms.model.dto.HomestaySaveDTO;
import com.bnbms.model.vo.HomestayDetailVO;
import com.bnbms.security.RequireRole;
import com.bnbms.security.UserContext;
import com.bnbms.service.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 房东房源管理
 */
@RestController
@RequestMapping("/api/host/homestays")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_HOST)
public class HostHomestayController {

    private final HomestayService homestayService;

    @GetMapping
    public Result<PageResult<Homestay>> page(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(homestayService.hostPage(UserContext.getUserId(), status, keyword, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<HomestayDetailVO> detail(@PathVariable Long id) {
        return Result.ok(homestayService.getHostDetail(id, UserContext.getUserId()));
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody HomestaySaveDTO dto) {
        Long id = homestayService.createHomestay(UserContext.getUserId(), dto);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("发布成功，等待平台审核", data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HomestaySaveDTO dto) {
        homestayService.updateHomestay(UserContext.getUserId(), id, dto);
        return Result.ok("保存成功", null);
    }

    @PutMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        homestayService.submitAudit(UserContext.getUserId(), id);
        return Result.ok("已提交审核", null);
    }

    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        homestayService.offline(UserContext.getUserId(), id);
        return Result.ok("房源已下架", null);
    }

    @PutMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id) {
        homestayService.online(UserContext.getUserId(), id);
        return Result.ok("房源已重新上架", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        homestayService.removeHomestay(UserContext.getUserId(), id);
        return Result.ok("删除成功", null);
    }
}
