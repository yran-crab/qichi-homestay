package com.bnbms.controller.portal;

import com.bnbms.common.Result;
import com.bnbms.entity.Facility;
import com.bnbms.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设施字典（公开只读）
 */
@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class PortalFacilityController {

    private final FacilityService facilityService;

    @GetMapping
    public Result<List<Facility>> list() {
        return Result.ok(facilityService.listAll());
    }
}
