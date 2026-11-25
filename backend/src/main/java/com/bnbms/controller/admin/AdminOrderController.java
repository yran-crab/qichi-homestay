package com.bnbms.controller.admin;

import com.bnbms.common.Constants;
import com.bnbms.common.PageResult;
import com.bnbms.common.Result;
import com.bnbms.model.dto.OrderQueryDTO;
import com.bnbms.model.vo.OrderVO;
import com.bnbms.security.RequireRole;
import com.bnbms.security.UserContext;
import com.bnbms.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@RequireRole(Constants.ROLE_ADMIN)
public class AdminOrderController {

    private final BookingService bookingService;

    @GetMapping
    public Result<PageResult<OrderVO>> page(@ModelAttribute OrderQueryDTO query) {
        return Result.ok(bookingService.adminPage(query));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.ok(bookingService.getDetail(
                UserContext.getUserId(), Constants.ROLE_ADMIN, id));
    }
}
