package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.Result;
import com.bnbms.entity.Facility;
import com.bnbms.model.dto.FacilitySaveDTO;
import com.bnbms.security.RequireRole;
import com.bnbms.service.FacilityService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员设施字典管理
 */
@RestController
@RequestMapping("/api/admin/facilities")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminFacilityController {

    private final FacilityService facilityService;

    @GetMapping
    public Result<List<Facility>> list() {
        return Result.ok(facilityService.listAll());
    }

    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody FacilitySaveDTO dto) {
        Long id = facilityService.create(dto);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.ok("新增成功", data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody FacilitySaveDTO dto) {
        facilityService.update(id, dto);
        return Result.ok("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        facilityService.remove(id);
        return Result.ok("删除成功", null);
    }
}
